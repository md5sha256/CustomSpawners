package com.gmail.andrewandy.spawnerplugin.event;

import com.gmail.andrewandy.spawnerplugin.betaobjects.Spawner;
import org.bukkit.event.Cancellable;

public interface SpawnerEvent extends Cancellable {

    Spawner getSpawner();

}
