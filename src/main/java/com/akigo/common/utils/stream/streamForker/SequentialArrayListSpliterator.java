package com.akigo.common.utils.stream.streamForker;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class SequentialArrayListSpliterator<T> implements AddableSequentialSpliterator<T> {

    private final List<T> q = new ArrayList<>();
    private int currIndex = 0;

    public SequentialArrayListSpliterator() {
        System.out.println("ArrayListSpliterator created");
    }

    @Override
    public void add(T t) {
        q.add(t);
    }


    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        T t;
        while (true) {
            if (q.size() > currIndex) {
                t = q.get(currIndex++);
                break;
            }
        }
        if (t != ForkingStreamConsumer.END_OF_STREAM) {
            action.accept(t);
            return true;
        }
        return false;
    }


    @Override
    public Spliterator<T> trySplit() {
        return null;
    }


    @Override
    public long estimateSize() {
        return 0;
    }


    @Override
    public int characteristics() {
        return 0;
    }

}
