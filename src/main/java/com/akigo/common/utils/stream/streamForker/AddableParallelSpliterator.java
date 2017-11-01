package com.akigo.common.utils.stream.streamForker;

import java.util.Spliterator;
import java.util.function.Supplier;

public interface AddableParallelSpliterator<T> extends Spliterator<T>, Addable<T> {
    int DEFAULT_PARALLELISM = 2;

    static <T> Supplier<AddableParallelSpliterator<T>> getDefaultSpliterator() {
        return () -> new ParallelBlockingQueueSpliterator<>(DEFAULT_PARALLELISM);
    }

    static <T> Supplier<AddableParallelSpliterator<T>> getDefaultSpliterator(int parallelism) {
        return () -> new ParallelBlockingQueueSpliterator<>(parallelism);
    }
}
