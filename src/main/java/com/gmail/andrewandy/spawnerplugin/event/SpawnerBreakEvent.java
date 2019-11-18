package com.gmail.andrewandy.spawnerplugin.event;

import com.gmail.andrewandy.spawnerplugin.spawner.Spawner;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class SpawnerBreakEvent extends SpawnerEvent implements Cancellable {

    private boolean cancel;

    public SpawnerBreakEvent(Spawner spawner) {
        super(spawner);
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
