package com.gmail.andrewandy.spawnerplugin.data;

import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.object.Spawner;
import com.gmail.andrewandy.spawnerplugin.util.Common;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class DataUtil {

    private static YamlConfiguration dataFile;
    private static File data;

    private static void save() {
        if (data == null) {
            return;
        }
        try {
            dataFile.save(data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void setupUtil() throws IOException {
        File pluginFolder = SpawnerPlugin.getInstance().getDataFolder();
        File yaml = new File(pluginFolder, "data.yml");
        if (!yaml.exists()) {
            yaml.createNewFile();
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(yaml);
        if (config.getConfigurationSection("Data") == null) {
            config.createSection("Data");
        }
        dataFile = config;
        data = yaml;
        save();
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    public static void saveData(Spawner spawner) {
        final Spawner cloned = spawner.clone();
        Common.asBukkitRunnable(() -> {
            synchronized (dataFile) {
                ConfigurationSection section = dataFile.getConfigurationSection("Data");
                assert section != null;
                ConfigurationSection section1 = section.createSection(cloned.getIdentifier());
                section1.set("level", cloned.getLevel());
                section1.set("maxLevel", cloned.getMaxLevel());
                section1.set("spawnType", cloned.getSpawnedType().name());
                section1.set("delay", cloned.getDelay());
                save();
            }
        }).runTaskAsynchronously(SpawnerPlugin.getInstance());
    }

    public static void saveDataOnDisable(Spawner spawner) {
        ConfigurationSection section = dataFile.getConfigurationSection("Data");
        assert section != null;
        ConfigurationSection section1 = section.createSection(spawner.getIdentifier());
        section1.set("level", spawner.getLevel());
        section1.set("maxLevel", spawner.getMaxLevel());
        section1.set("spawnType", spawner.getSpawnedType().name());
        section1.set("delay", spawner.getDelay());
        save();
    }

    public static Spawner loadData(Location location) {
        String identifier = Spawner.asIdentifier(location);
        ConfigurationSection section = dataFile.getConfigurationSection("Data");
        assert section != null;
        ConfigurationSection section1 = section.getConfigurationSection(identifier);
        if (section1 == null) {
            return null;
        }
        try {
            int level = section1.getInt("level");
            int maxLevel = section1.getInt("maxLevel");
            int delay = section1.getInt("delay");
            EntityType entityType = EntityType.valueOf(section1.getString("spawnType"));
            return new Spawner(entityType, delay, level, maxLevel, location);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static void clearData(Spawner spawner) {
        Objects.requireNonNull(spawner);
        final Spawner cloned = spawner.clone();
        Common.asBukkitRunnable(() -> {
            ConfigurationSection section = dataFile.getConfigurationSection("Data");
            assert section != null;
            section.set(cloned.getIdentifier(), null);
            save();
        }).runTaskAsynchronously(SpawnerPlugin.getInstance());
    }

}
