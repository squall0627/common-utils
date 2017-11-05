/*
 * SequentialBlockingQueueSpliterator.java
 * Created on  2017/11/3 0:55
 *
 * Copyright (c) 2017-2099. AkiGo科技有限公司 版权所有
 * AkiGo TECHNOLOGY CO.,LTD. All Rights Reserved.
 *
 * Date          Author     Version    Discription
 * 2017/11/3     浩         V1.0.0     InitVer
 */
package com.akigo.common.utils.stream.streamForker;

import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class SequentialBlockingQueueSpliterator<T> implements AddableSequentialSpliterator<T> {

    private final BlockingQueue<T> q = new LinkedBlockingQueue<>();

    public SequentialBlockingQueueSpliterator() {
        System.out.println("BlockingQueueSpliterator ->created");
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
            } catch (InterruptedException ignored) {
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
