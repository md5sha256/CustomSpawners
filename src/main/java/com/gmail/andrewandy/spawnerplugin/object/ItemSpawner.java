package com.gmail.andrewandy.spawnerplugin.object;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;

public interface ItemSpawner {

    NBTItem getBase();

    void setGlowing(boolean glowing);

    Location getLocation();

}
