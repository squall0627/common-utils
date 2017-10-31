package com.akigo.common.utils.stream.streamForker;

import java.util.Spliterator;

public interface AddableParallelSpliterator<T> extends Spliterator<T>, Addable<T> {
    static final int DEFAULT_PARALLELISM = 2;

    static <T> AddableParallelSpliterator<T> getDefaultSpliterator() {
        return new ParallelBlockingQueueSpliterator<T>(DEFAULT_PARALLELISM);
    }

    static <T> AddableParallelSpliterator<T> getDefaultSpliterator(int parallelism) {
        return new ParallelBlockingQueueSpliterator<T>(parallelism);
    }
}
