package com.gmail.andrewandy.spawnerplugin.event;

import com.gmail.andrewandy.spawnerplugin.spawner.Spawner;
import org.bukkit.event.HandlerList;

/**
 * Represents an event when a spawner is loaded from the Bukkit world. (i.e on chunk reloads).
 */

public class SpawnerLoadEvent extends SpawnerEvent{

    public SpawnerLoadEvent(Spawner spawner, boolean async) {
        super(spawner, async);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
