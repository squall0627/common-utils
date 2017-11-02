/*
 * StreamForkerTest.java
 * Created on  2017/11/3 0:55
 *
 * Copyright (c) 2017-2099. AkiGo科技有限公司 版权所有
 * AkiGo TECHNOLOGY CO.,LTD. All Rights Reserved.
 *
 * Date          Author     Version    Discription
 * 2017/11/3     浩         V1.0.1     InitVer
 */
package com.akigo.common.utils.stream.streamForker;

import org.junit.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamForkerTest {

    @Test
    public void getResults_1() throws Exception {
        Stream<Integer> stream = IntStream.rangeClosed(1, 100).boxed();
        Results results = new StreamForker<>(stream)
                .fork("key1", s -> s.reduce(0, Integer::sum))
                .fork("key2", s -> s.reduce(0, Integer::max))
                .fork("key3", s -> s.reduce(0, Integer::min))
                .getResults();

        Integer v1 = results.get("key1");
        Integer v2 = results.get("key2");
        Integer v3 = results.get("key3");

        System.out.println(v1);
        System.out.println(v2);
        System.out.println(v3);
    }

    @Test
    public void getResults_2() throws Exception {
        Stream<Integer> stream = IntStream.rangeClosed(1, 100).boxed();
        Results results = new StreamForker<>(stream, SequentialArrayListSpliterator<Integer>::new)
                .fork("key1", s -> s.reduce(0, Integer::sum))
                .fork("key2", s -> s.reduce(0, Integer::max))
                .fork("key3", s -> s.reduce(0, Integer::min))
                .getResults();

        Integer v1 = results.get("key1");
        Integer v2 = results.get("key2");
        Integer v3 = results.get("key3");

        System.out.println(v1);
        System.out.println(v2);
        System.out.println(v3);
    }

    @Test
    public void getResults_3() throws Exception {
        Stream<Integer> stream = IntStream.rangeClosed(1, 100).boxed();
        Results results = new StreamForker<>(stream, SequentialArrayListSpliterator<Integer>::new)
                .fork("key1", s -> s.reduce(0, Integer::sum), 100)
                .fork("key2", s -> s.reduce(0, Integer::max), 3)
                .fork("key3", s -> s.reduce(0, Integer::min), 3)
                .getResults();

        Integer v1 = results.get("key1");
        Integer v2 = results.get("key2");
        Integer v3 = results.get("key3");

        System.out.println(v1);
        System.out.println(v2);
        System.out.println(v3);
    }
}