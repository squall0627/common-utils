package com.akigo.common.utils.stream.streamForker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamForker<T> {

    private final Stream<T> stream;
    private final Class<? extends AddableSequentialSpliterator> spliteratorClass;
    private final Map<Object, Function<Stream<T>, ?>> forks = new HashMap<>();
    private final Map<Object, Integer> parallelspliteratorMap = new HashMap<>();

    public StreamForker(Stream<T> stream) {
        this.stream = stream;
        this.spliteratorClass = null;
    }

    public StreamForker(Stream<T> stream, Class<? extends AddableSequentialSpliterator> spliteratorClass) {
        this.stream = stream;
        this.spliteratorClass = spliteratorClass;
    }

    public StreamForker<T> fork(Object key, Function<Stream<T>, ?> function) {
        forks.put(key, function);
        return this;
    }


    public StreamForker<T> fork(Object key, Function<Stream<T>, ?> function,
                                int parallelism) {
        fork(key, function);
        this.parallelspliteratorMap.put(key, parallelism);
        return this;
    }


    public Results getResults() {
        ForkingStreamConsumer<T> consumer = buildConsumer();
        try {
            this.stream.sequential().forEach(consumer);
        } finally {
            consumer.finish();
        }

        return consumer;
    }

    private ForkingStreamConsumer<T> buildConsumer() {
        List<Addable<T>> spliterators = new ArrayList<>();
        Map<Object, Future<?>> actions = this.forks.entrySet().stream().reduce(new HashMap<Object, Future<?>>(),
                (map, en) -> {
                    map.put(en.getKey(), createAsyncTask(spliterators, en.getKey(), en.getValue()));
                    return map;
                },
                (map1, map2) -> {
                    map1.putAll(map2);
                    return map1;
                });
        return new ForkingStreamConsumer<T>(spliterators, actions);
    }

    private Future<?> createAsyncTask(List<Addable<T>> spliterators, Object key, Function<Stream<T>, ?> function) {
        final Stream<T> newStream;
        if (!this.parallelspliteratorMap.containsKey(key)) {
            AddableSequentialSpliterator<T> spliterator = createSequentialSpliterator();
            spliterators.add(spliterator);
            newStream = StreamSupport.stream(spliterator, false);
        } else {
            AddableParallelSpliterator spliterator = createParallelSpliterator(this.parallelspliteratorMap.get(key));
            spliterators.add(spliterator);
            newStream = StreamSupport.stream(spliterator, true);
        }
        return CompletableFuture.supplyAsync(() -> function.apply(newStream));
    }

    private AddableSequentialSpliterator<T> createSequentialSpliterator() {
        if (this.spliteratorClass == null) {
            return AddableSequentialSpliterator.<T>getDefaultSpliterator();
        } else {
            try {
                return this.spliteratorClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private AddableParallelSpliterator<T> createParallelSpliterator(int parallelism) {
        if (parallelism > 0) {
            return AddableParallelSpliterator.<T>getDefaultSpliterator(parallelism);
        } else {
            return AddableParallelSpliterator.<T>getDefaultSpliterator();
        }
    }
}
