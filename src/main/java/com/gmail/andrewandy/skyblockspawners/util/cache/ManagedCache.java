package com.gmail.andrewandy.skyblockspawners.util.cache;

import java.util.concurrent.TimeUnit;

public interface ManagedCache<T> extends Cache<T> {

    Runnable getClearTask();

    void forceClear();

    void setDelay(TimeUnit timeUnit, int duration);

    long getDelay(TimeUnit timeUnit);

}
