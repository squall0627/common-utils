package com.akigo.common.utils.stream.streamForker;

import java.util.Spliterator;
import java.util.function.Supplier;

public interface AddableSequentialSpliterator<T> extends Spliterator<T>, Addable<T> {
    static <T> Supplier<AddableSequentialSpliterator<T>> getDefaultSpliterator() {
        return SequentialBlockingQueueSpliterator::new;
    }
}
