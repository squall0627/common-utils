package com.akigo.common.utils.stream.streamForker;

import java.util.Spliterator;

public interface AddableSequentialSpliterator<T> extends Spliterator<T>, Addable<T> {
    static <T> AddableSequentialSpliterator<T> getDefaultSpliterator() {
        return new SequentialBlockingQueueSpliterator<T>();
    }
}
