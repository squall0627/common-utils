package com.akigo.common.utils.bigdata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.co.jfe_steel.jax01.core.exception.SystemException;
import jp.co.jfe_steel.jax01.core.report.ReportException;
import jp.co.jfe_steel.jax01.core.report.text.csv.CSVReader;
import jp.co.jfe_steel.jax01.core.report.text.csv.CSVWriter;

public class AsyncSortBlock<T> implements Comparable<AsyncSortBlock<T>> {

    private Logger logger = LoggerFactory.getLogger("watch");

//    private final Object memoryCacheLock = new Object();

    //    private static final int RECOVERING = -1;
    private static final int NO_FLUSHED = -1;
    private static final int INITIALIIZED = 0;
    private static final int FLUSHING = 1;
    private static final int FLUSHED = 2;

    /** CSVファイルのエンコード */
    private static final String CSV_ENCODE = "MS932";

    private final TreeMap<T, T> memoryCache;
    private final List<T> waitQueue;

    private final AsyncSortPool<T> sortPool;
    private final Comparator<T> comparator;
    private final int blockThreshold;
    private final int waitQueueThreshold;
    private final Path fileCachePath;

    private T first;
    private T last;

    private volatile int flushStatus = INITIALIIZED;

    private Future<List<T>> flushTask;

    AsyncSortBlock(AsyncSortPool<T> sortPool, Comparator<T> comparator,
                                 String cacheFileRootDir, int blockThreshold, int blockIndex) {
        this(sortPool, comparator, cacheFileRootDir, blockThreshold, blockIndex, null);
    }

    AsyncSortBlock(AsyncSortPool<T> sortPool,
                                 Comparator<T> comparator, String cacheFileRootDir, int blockThreshold, int blockIndex, List<
            T> sortTargets) {
        this.sortPool = sortPool;
        this.comparator = comparator;
        this.blockThreshold = blockThreshold;
        this.waitQueueThreshold = this.blockThreshold;
        this.waitQueue = new ArrayList<>();
        this.fileCachePath = Paths.get(cacheFileRootDir, "fileCache_" + String.valueOf(blockIndex));
        this.memoryCache = new TreeMap<>(comparator);
        if (sortTargets != null && !sortTargets.isEmpty()) {
            this.first = sortTargets.get(0);
            this.last = sortTargets.get(sortTargets.size() - 1);
            for (T t : sortTargets) {
                this.memoryCache.put(t, t);
//                System.out.println(t);
            }
        }
    }

    T getFirst() {
        return this.first;
    }

    T getLast() {
        return this.last;
    }

    Path getFileCachePath() {
        return this.fileCachePath;
    }

    TreeMap<T, T> getMemoryCache() {
        return this.memoryCache;
    }

    void add(T t) {
//        synchronized (this.memoryCacheLock) {
        if (isMemoryCacheFull()) {
            addToWaitQueue(t);
        } else {
            addToMemoryCache(t);
        }
//        }
    }

    boolean isMemoryCacheFull() {
        return INITIALIIZED < flushStatus || this.memoryCache.size() >= this.blockThreshold;
    }

    boolean isMemoryCacheEmpty() {
        return INITIALIIZED >= flushStatus && this.memoryCache.size() == 0;
    }

    private boolean isWaitQueueFull() {
        return this.waitQueue.size() >= this.waitQueueThreshold;
    }

    boolean isBeforeFrom(T t) {
        return this.comparator.compare(
                this.getLast() == null ? this.getFirst() : this.getLast(),
                t) < 0;
    }

    boolean isAfterFrom(T t) {
        return this.comparator.compare(t, this.getFirst()) <= 0;
    }

    boolean isInBound(T t) {
        return isInBound(t, null);
    }

    boolean isInBound(T t, AsyncSortBlock<T> nextBlock) {
        return (this.last == null && this.isBeforeFrom(t) &&
                (nextBlock == null || (nextBlock != null && nextBlock.isAfterFrom(t))))
                || (this.first != null && this.last != null
                && this.comparator.compare(t, this.first) >= 0
                && this.comparator.compare(t, this.last) <= 0);
//        return (this.last == null &&
//            (nextBlock == null || (nextBlock != null && nextBlock.isAfterFrom(t))))
//            || (this.first != null && this.last != null
//                && this.comparator.compare(t, this.first) >= 0
//                && this.comparator.compare(t, this.last) <= 0);
    }

    private void addToMemoryCache(T t) {
        this.memoryCache.put(t, t);
        if (this.first == null && this.last == null) {
            this.first = t;
        } else if (this.first != null && this.last == null) {
            if (this.comparator.compare(t, this.first) >= 0) {
                this.last = t;
            } else {
                this.last = this.first;
                this.first = t;
            }

            // ※※※※lastが変更したので、SortBlockChainの順番の維持するために、SortBlockChainから外して、もう一回入れる必要がある
            this.sortPool.reentrySortBlockChain(this);
        }

        tryFlush();
    }

    private void addToWaitQueue(T t) {
//        if ("18".equals(((JAT03B2240Z01_BDto) t).getJhopeGempinKey()) && "6".equals(
//            ((JAT03B2240Z01_BDto) t).getHikakuKeyJhope())) {
//            System.out.println("Stop");
//        }
        this.waitQueue.add(t);
        trySwap();
    }

    void tryFlush() {
//        synchronized (this.memoryCacheLock) {
        if (isMemoryCacheFull()) {
            flush();
        }
//        }
    }

    @SuppressWarnings("unchecked")
    private void flush() {

        // 前回のFlush処理の終わらない場合、キャンセルする
        tryCancelFlushTask();

//        flushStatus = NO_FLUSHED;

        final List<T> flushTargets = this.memoryCache.keySet().stream().collect(
                Collectors.toList());

        // 非同期
        this.flushTask = CompletableFuture.supplyAsync(() -> {

            // TODO
            logger.debug("FileCache:{}の書き込むが開始。", this.fileCachePath.toString());
//            System.out.println(this.fileCachePath.toString() + " write start");

            try (OutputStream os = Files.newOutputStream(this.fileCachePath);
                 CSVWriter<T> writer = new CSVWriter<T>(os, (Class<
                         T>) flushTargets.get(0).getClass(), false, Charset
                         .forName(CSV_ENCODE));) {
                writer.write(flushTargets);
            } catch (IOException | ReportException e) {
                logger.warn(
                        "ファイルキャッシュ:{}のflush処理が失敗しました。\r\n{}",
                        this.fileCachePath.toString(),
                        e);
                throw new SystemException(e);
            }

//            System.out.println(this.fileCachePath.toString() + " write end");

            return flushTargets;
        }).whenComplete((list, exception) -> {
            if (exception != null) {
                // Flush失敗の場合
                logger.warn(
                        "{}のFlushが失敗しましたので、MemoryCacheを回復する。\r\n{}",
                        this.fileCachePath.toString(),
                        exception);

//                System.out.println(
//                    this.fileCachePath.toString() + "のFlushが失敗しましたので、MemoryCacheを回復する。");

//                synchronized (this.memoryCacheLock) {
//                for (T t : list) {
//                    this.memoryCache.put(t, t);
//                }
//                flushStatus = NO_FLUSHED;
//                }
            } else {
                int preFlushStatus = flushStatus;
                flushStatus = FLUSHING;
//                synchronized (this.memoryCacheLock) {
                this.memoryCache.clear();
                if (INITIALIIZED == preFlushStatus) {
                    this.sortPool.decrementSortTargetCount(list.size());
                }
                this.flushStatus = FLUSHED;
//                }

                logger.debug("FileCache:{}の書き込むが終了。", this.fileCachePath.toString());
            }
        });
    }

    void trySwap() {
        trySwap(false);
    }

    void trySwap(boolean enforce) {
//        synchronized (this.memoryCacheLock) {
        if (isWaitQueueFull() || (enforce && !this.waitQueue.isEmpty())) {
            if (swap()) {
                flush();
            }
        }
//        }
    }

    private Boolean swap() {

        // TODO
        logger.debug("FileCache:{}のswapが開始。", this.fileCachePath.toString());
//      System.out.println(this.fileCachePath.toString() + " swap start");

        // 前回のFlush処理の終わらない場合、キャンセルする
        tryCancelFlushTask();

        try {
            List<T> dtoList = null;
            if (FLUSHED == flushStatus) {
                dtoList = read();
            } else {
                dtoList = this.memoryCache.keySet().stream().collect(Collectors.toList());
            }

            dtoList.addAll(this.waitQueue);

            Collections.sort(dtoList, this.comparator);

            List<T> newMemoryCacheList = dtoList.subList(0, this.blockThreshold);

            this.memoryCache.clear();
            for (T t : newMemoryCacheList) {
                this.memoryCache.put(t, t);
//              System.out.println(t);
            }

            this.waitQueue.clear();

            this.first = newMemoryCacheList.get(0);
            this.last = newMemoryCacheList.get(newMemoryCacheList.size() - 1);

            // ※※※※lastが変更したので、SortBlockChainの順番の維持するために、SortBlockChainから外して、もう一回入れる必要がある
            this.sortPool.reentrySortBlockChain(this);

            flushStatus = NO_FLUSHED;

            List<T> backToPoolList = dtoList.subList(this.blockThreshold, dtoList.size());
            if (!backToPoolList.isEmpty()) {
                this.sortPool.backToPool(backToPoolList);
            }

            // TODO
            logger.debug("FileCache:{}のswapが終了。", this.fileCachePath.toString());
//          System.out.println(this.fileCachePath.toString() + " swap end");

        } catch (IOException | ReportException e) {
            logger.warn(
                    "ファイルキャッシュ:{}のswap処理が失敗しました。\r\n{}",
                    this.fileCachePath.toString(),
                    e);
            return false;
        }
        return true;
    }

    private void tryCancelFlushTask() {
        if (this.flushTask != null && !this.flushTask.isDone() && !this.flushTask.isCancelled()) {
            boolean cancelled = this.flushTask.cancel(false);
            if (!cancelled) {

                logger.debug(
                        "ファイルキャッシュ:{}のflush処理がキャンセルできませんでしたので、終了するまで待つ。",
                        this.fileCachePath.toString());

                // タスクがキャンセルできない場合、終わるまで待つ
                try {
                    this.flushTask.get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.warn(
                            "ファイルキャッシュ:{}のflush処理が失敗しました。\r\n{}",
                            this.fileCachePath.toString(),
                            e);
                }
            } else {
//                flushStatus = NO_FLUSHED;
                logger.debug(
                        "ファイルキャッシュ:{}のflush処理がキャンセルされました。",
                        this.fileCachePath.toString());
            }
        }
    }

    void deleteFileCache() {
        // 前回のFlush処理の終わらない場合、キャンセルする
        tryCancelFlushTask();

//        // TODO
//        File fileCache = this.fileCachePath.toFile();
//        if (FLUSHED == flushStatus && fileCache.exists()) {
//            this.fileCachePath.toFile().delete();
//        }

        CompletableFuture.supplyAsync(() -> {
            File fileCache = this.fileCachePath.toFile();
            if (fileCache.exists()) {
                return this.fileCachePath.toFile().delete();
            }
            return true;
        });
    }

    boolean merge(AsyncSortBlock<T> other) {

        // 前回のFlush処理の終わらない場合、キャンセルする
        tryCancelFlushTask();

//        synchronized (this.memoryCacheLock) {
        Iterator<T> it = other.memoryCache.keySet().iterator();

        T t;
        while (!isMemoryCacheFull() && it.hasNext()) {
            t = it.next();
            this.memoryCache.put(t, t);
            it.remove();
        }

        syncBound();

        tryFlush();

        if (other.isMemoryCacheEmpty()) {
            return true;
        } else {
            other.syncBound();
            return false;
        }
//        }
    }

    private void syncBound() {
        if (this.memoryCache.size() > 0) {
            this.first = this.memoryCache.firstKey();
            this.last = this.memoryCache.lastKey();
        }
    }

    @SuppressWarnings("unchecked")
    List<T> read() throws IOException, ReportException {
        // 前回のFlush処理の終わらない場合、キャンセルする
        tryCancelFlushTask();

        if (FLUSHED == flushStatus) {
            try (InputStream is = Files.newInputStream(this.fileCachePath);
                 CSVReader<T> reader = new CSVReader<T>(is, (Class<
                         T>) this.first.getClass(), false, Charset.forName(
                         CSV_ENCODE));) {

                return reader.read();
            }
        } else {
            return this.memoryCache.keySet().stream().collect(Collectors.toList());
        }
    }

    List<T> readAll() throws IOException, ReportException {
        List<T> all = new ArrayList<>();
        all.addAll(read());
        all.addAll(this.waitQueue);
        Collections.sort(all, this.comparator);
        return all;
    }

    void completeFlush() {
        if (this.flushTask != null) {
            try {
                this.flushTask.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.warn(
                        "ファイルキャッシュ:{}のflush処理が失敗しました。\r\n{}",
                        this.fileCachePath.toString(),
                        e);
            }
        }
    }

    @Override
    public int compareTo(AsyncSortBlock<T> o) {
        return this.comparator.compare(
                this.getLast() == null ? this.getFirst() : this.getLast(),
                o.getFirst());
    }

}