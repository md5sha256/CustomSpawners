package com.gmail.andrewandy.spawnerplugin.spawner.data;

import com.gmail.andrewandy.corelib.util.Common;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public final class SpawnerData {

    private static YamlConfiguration data;
    private static Collection<Location> locations = new HashSet<>();

    public static void writeData(Location spawner) {
        Objects.requireNonNull(Objects.requireNonNull(spawner).getWorld());
        locations.add(spawner);
    }

    public static void removeData(Location location) {
        Objects.requireNonNull(Objects.requireNonNull(location).getWorld());
        locations.remove(location);
    }

    public static boolean isRegistered(Location location) {
        return locations.contains(location);
    }

    public static BukkitTask saveToDisk(boolean async) {
        Collection<Location> locations = new HashSet<>(SpawnerData.locations);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Common.getLogger(SpawnerPlugin.getInstance(), "&b[SpawnerData] Saving Spawner Data...");
                File folder = SpawnerPlugin.getInstance().getDataFolder();
                File dataFile = new File(folder.getPath(), "data.yml");
                try {
                    dataFile.createNewFile();
                } catch (IOException ex) {
                    Common.getLogger(SpawnerPlugin.getInstance(), "&c&l[Critical] Unable to save spawner data to disk!");
                    this.cancel();
                    return;
                }
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);
                ConfigurationSection section = yml.getConfigurationSection("Locations");
                if (section == null) {
                    section = yml.createSection("Locations");
                }
                for (Location location : locations) {
                    section.set(UUID.randomUUID().toString(), location);
                }
                this.cancel();
            }
        };
        if (async) {
            return runnable.runTaskAsynchronously(SpawnerPlugin.getInstance());
        } else {
            return runnable.runTask(SpawnerPlugin.getInstance());
        }
    }

    private static BukkitTask loadDataFile(boolean async) throws IllegalStateException {
        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (data == null) {
                        File folder = SpawnerPlugin.getInstance().getDataFolder();
                        File dataFile = new File(folder.getPath(), "data.yml");
                        if (!dataFile.isFile()) {
                            if (!dataFile.createNewFile()) {
                                throw new IOException("Unable to create data file!");
                            }
                        }
                        data = YamlConfiguration.loadConfiguration(dataFile);
                    }
                    ConfigurationSection section = data.getConfigurationSection("Locations");
                    if (section == null) {
                        data.createSection("Locations");
                        this.cancel();
                        loadDataFile(async);
                        return;
                    }
                    for (String key : section.getKeys(false)) {
                        ConfigurationSection keySection = section.getConfigurationSection(key);
                        if (keySection == null) {
                            //Shouldn't happen
                            continue;
                        }
                        if (!section.isLocation(key)) {
                            continue;
                        }
                        Location location = section.getLocation(key);
                        if (location == null) {
                            throw new IllegalStateException("Null location");
                        }
                        locations.add(location);
                    }
                } catch (IOException ex) {
                    IllegalStateException exception = new IllegalStateException();
                    exception.addSuppressed(ex);
                    this.cancel();
                    throw exception;
                }
                this.cancel();
            }
        };
        if (async) {
            return bukkitRunnable.runTaskAsynchronously(SpawnerPlugin.getInstance());
        } else {
            return bukkitRunnable.runTask(SpawnerPlugin.getInstance());
        }
    }
}
