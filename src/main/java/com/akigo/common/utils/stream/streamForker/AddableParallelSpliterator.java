/*
 * AddableParallelSpliterator.java
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
