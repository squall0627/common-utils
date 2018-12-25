package com.akigo.common.utils.bigdata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.co.jfe_steel.jax01.core.exception.SystemException;
import jp.co.jfe_steel.jax01.core.report.ReportException;

public class AsyncSortPool<T> implements Iterable<T> {

    private Logger logger = LoggerFactory.getLogger("watch");

    private volatile AtomicLong countSortPool = new AtomicLong(0l);

    private int blockIndex = 0;

    private final Comparator<T> comparator;
    private final String cacheFileRootDir;

    private final int sortPoolThreshold;
    private final int sortBlockChainMinSize;
    private final int sortBlockChainMaxSize;
    private final int blockThreshold;

    private final TreeMap<AsyncSortBlock<T>, AsyncSortBlock<
            T>> sortBlockChain;

    AsyncSortPool(Comparator<T> comparator, String cacheFileRootDir,
                                int sortPoolThreshold, int sortBlockChainThreshold) {
        this.sortPoolThreshold = sortPoolThreshold;
        this.sortBlockChainMinSize = sortBlockChainThreshold;
        System.out.println("sortBlockChainMinSize:" + this.sortBlockChainMinSize);
        this.sortBlockChainMaxSize = this.sortBlockChainMinSize * 10;
        this.blockThreshold = this.sortPoolThreshold / this.sortBlockChainMinSize;
        System.out.println("blockThreshold:" + this.blockThreshold);
        this.cacheFileRootDir = cacheFileRootDir + "_" + String.valueOf(System.currentTimeMillis());

        try {
            Files.createDirectories(Paths.get(this.cacheFileRootDir));
        } catch (IOException e) {
            throw new SystemException("キャッシュファイル一時ディレクトリの作成が失敗しました。", e);
        }

        this.comparator = comparator;
        this.sortBlockChain = new TreeMap<>((o1, o2) -> o1.compareTo(o2));
    }

    void accept(T t) {

        Iterator<AsyncSortBlock<T>> it = sortBlockChain.keySet().iterator();

        AsyncSortBlock<T> targetBlock = null;
        AsyncSortBlock<T> curr = null;
        AsyncSortBlock<T> next = null;

        if (sortBlockChain.size() > 0) {
            // ※※※※※※※※※1件目と比較※※※※※※※
            if (sortBlockChain.firstKey().isAfterFrom(t)) {
                // 1件目より小さい場合、SortBlockChainの一番最初に新しいSortBlockを追加
                targetBlock = null;
            } else {
                while (it.hasNext()) {
                    if (curr == null) {
                        curr = it.next();
                    }
                    if (it.hasNext()) {
                        next = it.next();
                        if (curr.isInBound(t, next)) {
                            targetBlock = curr;
                            break;
                        } else {
                            curr = next;
                            if (it.hasNext()) {
                                continue;
                            } else {
                                if (curr.isInBound(t, null)) {
                                    targetBlock = curr;
                                    break;
                                }
                            }
                        }
                    } else {
                        if (curr.isInBound(t, null)) {
                            targetBlock = curr;
                            break;
                        }
                    }
                }
            }
        }

        if (targetBlock != null) {
            targetBlock.add(t);
        } else {
            AsyncSortBlock<T> newSortBlock = new AsyncSortBlock<>(this,
                    this.comparator, this.cacheFileRootDir, this.blockThreshold, blockIndex++);
            newSortBlock.add(t);
            addToSortBlockChain(newSortBlock);

//            if (logger.isDebugEnabled()) {

//            Iterator<AsyncSortBlock<T>> it2 = this.sortBlockChain.keySet().iterator();
//            while (it2.hasNext()) {
//                AsyncSortBlock<T> tt = it2.next();
//                JAT03B2240Z01_BDto dtoFirst = ((JAT03B2240Z01_BDto) tt.getFirst());
//                System.out.print(
//                    "FIRST:" + dtoFirst.getJhopeGempinKey() + "," + dtoFirst.getHikakuKeyJhope()
//                        + "," + dtoFirst.getErrorCode());
//                if (tt.getLast() != null) {
//                    JAT03B2240Z01_BDto dtoLast = ((JAT03B2240Z01_BDto) tt.getLast());
//                    System.out.println(
//                        "  END:" + dtoLast.getJhopeGempinKey() + "," + dtoLast.getHikakuKeyJhope()
//                            + "," + dtoLast.getErrorCode());
//                } else {
//                    System.out.println(" ");
//                }
//            }
//            }
        }

        incrementSortTargetCount(1);
        tryMergeSortBlock();
    }

    void incrementSortTargetCount(long delta) {
        this.countSortPool.addAndGet(delta);
    }

    void decrementSortTargetCount(long delta) {
        this.countSortPool.addAndGet(-delta);
    }

    void backToPool(List<T> sortTargets) {

        logger.debug(
                "backToPoolが開始されました。SortPoolにあるDTO個数:{}、SortBlock個数:{}",
                this.countSortPool,
                this.sortBlockChain.size());

        AsyncSortBlock<T> sortBlock = new AsyncSortBlock<>(this,
                this.comparator, this.cacheFileRootDir, this.blockThreshold, blockIndex++, sortTargets);
        sortBlock.tryFlush();
        addToSortBlockChain(sortBlock);

        logger.debug(
                "backToPoolが終了されました。SortPoolにあるDTO個数:{}、SortBlock個数:{}",
                this.countSortPool,
                this.sortBlockChain.size());
    }

    void addToSortBlockChain(AsyncSortBlock<T> sortBlock) {
        this.sortBlockChain.put(sortBlock, sortBlock);
    }

    void removeFromSortBlockChain(AsyncSortBlock<T> sortBlock) {

//        Map<AsyncSortBlock<T>, AsyncSortBlock<T>> tmp =
//            new LinkedHashMap<>();
//        for (Entry<AsyncSortBlock<T>, AsyncSortBlock<
//                        T>> en : this.sortBlockChain.entrySet()) {
//            if (sortBlock != en.getKey()) {
//                tmp.put(en.getKey(), en.getValue());
//            }
//        }
//
//
//        this.sortBlockChain.clear();
//        this.sortBlockChain.putAll(tmp);
        Iterator<AsyncSortBlock<T>> it = this.sortBlockChain.keySet().iterator();
        while (it.hasNext()) {
            if (sortBlock == it.next()) {
                it.remove();
                break;
            }
        }
    }

    void reentrySortBlockChain(AsyncSortBlock<T> sortBlock) {
        removeFromSortBlockChain(sortBlock);
        addToSortBlockChain(sortBlock);
    }

    private void tryMergeSortBlock() {
        if (this.countSortPool.get() >= this.sortPoolThreshold
                || this.sortBlockChain.size() >= this.sortBlockChainMaxSize) {
            logger.debug(
                    "MergeSortBlockが開始されました。SortPoolにあるDTO個数:{}、SortBlock個数:{}",
                    this.countSortPool,
                    this.sortBlockChain.size());
            mergeSortBlock();
            logger.debug(
                    "MergeSortBlockが終了されました。SortPoolにあるDTO個数:{}、SortBlock個数:{}",
                    this.countSortPool,
                    this.sortBlockChain.size());
        }
    }

    private void mergeSortBlock() {
        Iterator<AsyncSortBlock<T>> it = sortBlockChain.keySet().iterator();

        List<AsyncSortBlock<T>> temp = new ArrayList<>();

        AsyncSortBlock<T> currSortBlock;
        AsyncSortBlock<T> nextSortBlock;
        while (it.hasNext()) {
            currSortBlock = it.next();
            if (!temp.contains(currSortBlock)) {
                temp.add(currSortBlock);
            }
            while (!currSortBlock.isMemoryCacheFull()) {
                if (it.hasNext()) {
                    nextSortBlock = it.next();
                    if (!nextSortBlock.isMemoryCacheFull()) {
                        if (currSortBlock.merge(nextSortBlock)) {
                            nextSortBlock.deleteFileCache();
                            it.remove();

                            if (!temp.contains(currSortBlock)) {
                                temp.add(currSortBlock);
                            }

                            continue;
                        } else {
                            if (!temp.contains(currSortBlock)) {
                                temp.add(currSortBlock);
                            }
                            currSortBlock = nextSortBlock;
                            continue;
                        }
                    } else {
                        if (!temp.contains(nextSortBlock)) {
                            temp.add(nextSortBlock);
                        }
                        break;
                    }
                } else {
                    if (!temp.contains(currSortBlock)) {
                        temp.add(currSortBlock);
                    }
                    break;
                }
            }
        }

        sortBlockChain.clear();
        for (AsyncSortBlock<T> block : temp) {
            // Flushタスクの終了まで待つ
            block.completeFlush();
            sortBlockChain.put(block, block);
        }
    }

//    void clearWaitQueue() {
//        System.out.println(sortBlockChain.size());
//
//        List<AsyncSortBlock<T>> sortBlockChainView = sortBlockChain.keySet().stream()
//                        .collect(Collectors.toList());
//
//        Iterator<AsyncSortBlock<T>> it = sortBlockChainView.iterator();
//        AsyncSortBlock<T> t;
//        while (it.hasNext()) {
//            t = it.next();
//            t.trySwap(true);
//        }
//
//        // TODO
//        it = sortBlockChain.keySet().iterator();
//        while (it.hasNext()) {
//            t = it.next();
//            JAT03B2240Z01_BDto dtoFirst = ((JAT03B2240Z01_BDto) t.getFirst());
//            System.out.print("FLIE:" + t.getFileCachePath().toString());
//            System.out.print(" MEMORY_CACHE:" + t.getMemoryCache().size());
//            System.out.print(
//                " FIRST:" + dtoFirst.getJhopeGempinKey() + "," + dtoFirst.getHikakuKeyJhope()
//                    + "," + dtoFirst.getErrorCode());
//            if (t.getLast() != null) {
//                JAT03B2240Z01_BDto dtoLast = ((JAT03B2240Z01_BDto) t.getLast());
//                System.out.println(
//                    "  END:" + dtoLast.getJhopeGempinKey() + "," + dtoLast.getHikakuKeyJhope()
//                        + "," + dtoLast.getErrorCode());
//            } else {
//                System.out.println(" ");
//            }
//        }
//    }

    void deleteCacheFolder() {
        try {
            Files.delete(Paths.get(this.cacheFileRootDir));
        } catch (IOException e) {
            logger.warn("キャッシュファイルルートフォルダ「{}」が削除失敗しました。\r\n{}", this.cacheFileRootDir, e);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<T> {
        private Iterator<T> currBlockIterator;
        private Future<List<T>> nextBlockReadTask;

        Itr() {
            Entry<AsyncSortBlock<T>, AsyncSortBlock<T>> currEn =
                    sortBlockChain.pollFirstEntry();
            if (currEn != null) {
                AsyncSortBlock<T> currBlock = currEn.getKey();
                try {
                    this.currBlockIterator = currBlock.readAll().iterator();
                } catch (ReportException | IOException e) {
                    logger.error("SortBlockの読込が失敗しました。{}", e);
                    throw new SystemException(e);
                }
                currBlock.deleteFileCache();

                readNextBlock();
            }
        }

        @Override
        public boolean hasNext() {
            if (!this.currBlockIterator.hasNext() && this.nextBlockReadTask != null) {
                List<T> list;
                try {
                    list = this.nextBlockReadTask.get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("SortBlockの読込が失敗しました。{}", e);
                    throw new SystemException(e);
                }

                readNextBlock();

                if (list != null && !list.isEmpty()) {
                    this.currBlockIterator = list.iterator();
                } else {
                    return hasNext();
                }
            }
            return this.currBlockIterator.hasNext();
        }

        @Override
        public T next() {
            return this.currBlockIterator.next();
        }

        private void readNextBlock() {
            Entry<AsyncSortBlock<T>, AsyncSortBlock<T>> nextEn =
                    sortBlockChain.pollFirstEntry();
            if (nextEn != null) {
                AsyncSortBlock<T> nextBlock = nextEn.getKey();
                this.nextBlockReadTask = CompletableFuture.supplyAsync(() -> {
                    List<T> list = null;
                    try {
                        list = nextBlock.readAll();
                    } catch (ReportException | IOException e) {
                        logger.error("SortBlockの読込が失敗しました。{}", e);
                        throw new SystemException(e);
                    }
                    nextBlock.deleteFileCache();
                    return list;
                });
            } else {
                this.nextBlockReadTask = null;
            }
        }
    }

}