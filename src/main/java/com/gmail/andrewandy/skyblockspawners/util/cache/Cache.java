package com.gmail.andrewandy.skyblockspawners.util.cache;

import java.util.Collection;

public interface Cache<T> {

    Collection<T> getCached();

    void cache(T t);

    void purge(T t);

    void clear();

    boolean isCached(T t);
}
