package com.akigo.common.utils.stream.streamForker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamForker<T> {

    private final Stream<T> stream;
    private final Supplier<AddableSequentialSpliterator<T>> spSupplier;
    private final Map<Object, Function<Stream<T>, ?>> forks = new HashMap<>();
    private final Map<Object, Integer> parallelSpliteratorMap = new HashMap<>();

    public StreamForker(Stream<T> stream) {
        this.stream = stream;
        this.spSupplier = null;
    }

    public StreamForker(Stream<T> stream, Supplier<AddableSequentialSpliterator<T>> spSupplier) {
        this.stream = stream;
        this.spSupplier = spSupplier;
    }

    public StreamForker<T> fork(Object key, Function<Stream<T>, ?> function) {
        forks.put(key, function);
        return this;
    }


    public StreamForker<T> fork(Object key, Function<Stream<T>, ?> function,
                                int parallelism) {
        fork(key, function);
        this.parallelSpliteratorMap.put(key, parallelism);
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
        return new ForkingStreamConsumer<>(spliterators, actions);
    }

    private Future<?> createAsyncTask(List<Addable<T>> spList, Object key, Function<Stream<T>, ?> function) {
        final Stream<T> newStream;
        if (!this.parallelSpliteratorMap.containsKey(key)) {
            AddableSequentialSpliterator<T> sp = createSequentialSpliterator();
            spList.add(sp);
            newStream = StreamSupport.stream(sp, false);
        } else {
            AddableParallelSpliterator<T> sp = createParallelSpliterator(this.parallelSpliteratorMap.get(key));
            spList.add(sp);
            newStream = StreamSupport.stream(sp, true);
        }
        return CompletableFuture.supplyAsync(() -> function.apply(newStream));
    }

    private AddableSequentialSpliterator<T> createSequentialSpliterator() {
        if (this.spSupplier == null) {
            return (AddableSequentialSpliterator.<T>getDefaultSpliterator()).get();
        } else {
            return this.spSupplier.get();
        }
    }

    private AddableParallelSpliterator<T> createParallelSpliterator(int parallelism) {
        if (parallelism > 0) {
            return AddableParallelSpliterator.<T>getDefaultSpliterator(parallelism).get();
        } else {
            return AddableParallelSpliterator.<T>getDefaultSpliterator().get();
        }
    }
}
