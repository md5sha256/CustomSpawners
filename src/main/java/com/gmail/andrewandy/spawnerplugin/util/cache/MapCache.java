package com.gmail.andrewandy.spawnerplugin.util.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class MapCache<T> implements Cache<T> {

    private final Function<T, String> function;
    private Map<String, T> registered;

    public MapCache(Function<T, String> identifier) {
        this.function = Objects.requireNonNull(identifier);
        registered = new HashMap<>();
    }

    public MapCache(Function<T, String> identifier, Map<String, T> map) {
        this.function = Objects.requireNonNull(identifier);
        this.registered = Objects.requireNonNull(map);
    }

    public Function<T, String> getFunction() {
        return function;
    }

    @Override
    public Collection<T> getCached() {
        return registered.values();
    }

    @Override
    public void cache(T t) {
        Objects.requireNonNull(t);
        this.registered.put(function.apply(t), t);
    }

    @Override
    public void purge(T t) {
        Objects.requireNonNull(t);
        this.registered.remove(function.apply(t));
    }

    public T getFromCache(String identifier) {
        Objects.requireNonNull(identifier);
        return registered.get(identifier);
    }

    @Override
    public void clear() {
        this.registered.clear();
    }

    @Override
    public boolean isCached(T t) {
        if (!this.registered.containsValue(Objects.requireNonNull(t))) {
            return false;
        } else {
            return registered.containsKey(function.apply(t));
        }
    }
}
