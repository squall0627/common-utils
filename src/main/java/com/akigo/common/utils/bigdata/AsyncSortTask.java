package com.akigo.common.utils.bigdata;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncSortTask<T> {

    private Logger logger = LoggerFactory.getLogger("watch");

    private static final int DEFAULT_SORT_POOL_THRESHOLD = 100000;

    private final Object END = new Object();

    private Future<Boolean> sortTask;

    private Consumer<Iterator<T>> onFinishedCallback;

    private final BlockingQueue<T> sortTargetQueue;
    private final AsyncSortPool<T> sortPool;

//    private final Comparator<T> comparator;
//    private final String cacheFileRootDir;
//    private final int sortPoolThreshold;

    public AsyncSortTask(Comparator<T> comparator, String cacheFileDir) {
        this(comparator, cacheFileDir, DEFAULT_SORT_POOL_THRESHOLD);
    }

    public AsyncSortTask(Comparator<T> comparator, String cacheFileRootDir,
                         int memoryThreshold) {
//        this.comparator = comparator;
//        this.cacheFileRootDir = cacheFileRootDir;
//        this.sortPoolThreshold = memoryThreshold;
        this.sortTargetQueue = new LinkedBlockingQueue<>();
        this.sortPool = new AsyncSortPool<>(comparator, cacheFileRootDir,
            memoryThreshold);
    }

    public void start() {
        this.sortTask = CompletableFuture.supplyAsync(() -> this.sort()).whenComplete(
            (result, exception) -> {
                this.sortPool.clearWaitQueue();
                if (onFinishedCallback != null) {
                    doFinishedCallback();
                }
            });
    }

    public void send(T t) {
//        logger.debug("send {}, ", t);
        this.sortTargetQueue.add(t);
    }

    @SuppressWarnings("unchecked")
    public void finish() {
        this.sortTargetQueue.add((T) END);
    }

    public Boolean sort() {
        logger.debug("AsyncTask sort() Started.");
        T t;
        for (;;) {
            try {
                t = this.sortTargetQueue.take();
//                logger.debug("Get Dto:{}", t);
//                System.out.println("Get Dto:" + t);
            } catch (InterruptedException e) {
                continue;
            }
            if (END != t) {
                this.sortPool.accept(t);
            } else {
                break;
            }
        }
        logger.debug("AsyncTask sort() Finished.");
        return true;
    }

    public void onFinished(Consumer<Iterator<T>> callback) {
        this.onFinishedCallback = callback;
    }

    private void doFinishedCallback() {
//        logger.debug("doWhenFinished. SortTask status isDone:{}", this.sortTask.isDone());
//        try {
//            if (this.sortTask.get()) {
        this.onFinishedCallback.accept(this.sortPool.iterator());
//            }
//        } catch (InterruptedException | ExecutionException e) {
//            logger.error("非同期ソートタスクが失敗しました。", e);
//        }
    }

//    // TODO
//    public Boolean getResult() {
//        this.sortPool.clearWaitQueue();
//        if (onFinishedCallback != null) {
//            doFinishedCallback();
//        }
//        return true;
//    }

    public Boolean getResult() {
        try {
            Boolean result = this.sortTask.get();
            this.sortPool.deleteCacheFolder();
            return result;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("非同期ソートタスクが失敗しました。", e);
            return false;
        }
    }

}
