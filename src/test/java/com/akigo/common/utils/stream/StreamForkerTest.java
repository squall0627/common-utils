package com.akigo.common.utils.stream;

import com.akigo.common.utils.stream.streamForker.*;
import junit.framework.TestCase;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamForkerTest extends TestCase {


    public void testGetResults_1() throws Exception {
        Stream<Integer> stream = IntStream.rangeClosed(1, 100).boxed();
        Results results = new StreamForker<Integer>(stream)
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

    public void testGetResults_2() throws Exception {
        Stream<Integer> stream = IntStream.rangeClosed(1, 100).boxed();
        Results results = new StreamForker<Integer>(stream, SequentialArrayListSpliterator.class)
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

    public void testGetResults_3() throws Exception {
        Stream<Integer> stream = IntStream.rangeClosed(1, 100).boxed();
        Results results = new StreamForker<Integer>(stream, SequentialArrayListSpliterator.class)
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