package com.gmail.andrewandy.skyblockspawners.data;

import com.gmail.andrewandy.skyblockspawners.object.Spawner;
import com.gmail.andrewandy.skyblockspawners.util.cache.ConcurrentCache;

public class SpawnerCache extends ConcurrentCache<Spawner> {

    public SpawnerCache() {
        super((Spawner::getUniqueIdentifier));
    }
}
