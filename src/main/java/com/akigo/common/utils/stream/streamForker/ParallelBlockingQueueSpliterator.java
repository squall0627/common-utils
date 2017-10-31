package com.akigo.common.utils.stream.streamForker;

import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class ParallelBlockingQueueSpliterator<T> implements AddableParallelSpliterator<T> {

    //    private final Queue<T> q;
    private final BlockingQueue<T> q;
    private int parallelism = 0;

    public ParallelBlockingQueueSpliterator(int parallelism) {
        System.out.println("ParallelBlockingQueueSpliterator created");
        this.parallelism = parallelism;
//        q = new ConcurrentLinkedQueue<T>();
        q = new LinkedBlockingQueue<>();
    }

    private ParallelBlockingQueueSpliterator(int parallelism, BlockingQueue<T> q) {
        this.parallelism = parallelism;
        this.q = q;
    }

    @Override
    public void add(T t) {
        q.add(t);
    }


    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        T t;
        while (true) {

//            t = q.poll();
//            if (t != null) {
//                break;
//            }
//        }
            try {
                t = q.take();
                break;
            } catch (InterruptedException e) {
            }
        }
        if (t != ForkingStreamConsumer.END_OF_STREAM) {
            action.accept(t);
            return true;
        } else {
            q.add((T) ForkingStreamConsumer.END_OF_STREAM);
            return false;
        }
    }


    @Override
    public Spliterator<T> trySplit() {
        if (this.parallelism > 0) {
            this.parallelism--;
            return new ParallelBlockingQueueSpliterator<>(0, this.q);
        } else {
            return null;
        }
    }


    @Override
    public long estimateSize() {
        return this.parallelism;
    }


    @Override
    public int characteristics() {
        return 0;
    }

}
