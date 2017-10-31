package com.akigo.common.utils.stream.streamForker;

import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class SequentialBlockingQueueSpliterator<T> implements AddableSequentialSpliterator<T> {

    private final BlockingQueue<T> q = new LinkedBlockingQueue<>();

    public SequentialBlockingQueueSpliterator() {
        System.out.println("BlockingQueueSpliterator created");
    }

    @Override
    public void add(T t) {
        q.add(t);
    }


    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        T t;
        while (true) {
            try {
                t = q.take();
                break;
            } catch (InterruptedException e) {
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
