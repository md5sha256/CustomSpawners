package com.gmail.andrewandy.spawnerplugin.spawner;

import java.util.Collection;

public interface Stackable {

    Collection<OfflineSpawner<? extends Stackable>> getStacked();
    void stack(Stackable stackable);
    boolean canStack(Stackable stackable);
    void remove(Stackable stackable);

}
