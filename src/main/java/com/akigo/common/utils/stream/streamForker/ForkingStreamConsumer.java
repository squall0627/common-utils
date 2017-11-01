package com.akigo.common.utils.stream.streamForker;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;


public class ForkingStreamConsumer<T> implements Consumer<T>, Results {
    static final Object END_OF_STREAM = new Object();

    private final List<Addable<T>> spliterators;
    private final Map<Object, Future<?>> actions;

    ForkingStreamConsumer(List<Addable<T>> spliterators, Map<Object, Future<?>> actions) {
        this.spliterators = spliterators;
        this.actions = actions;
    }

    @Override
    public void accept(T t) {
        spliterators.forEach(spliterator -> spliterator.add(t));
    }

    void finish() {
        accept((T) END_OF_STREAM);
    }

    @Override
    public <R> R get(Object key) {
        if (!actions.containsKey(key)) {
            throw new RuntimeException("key: " + key + " not exists");
        }
        try {
            return ((Future<R>) actions.get(key)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
