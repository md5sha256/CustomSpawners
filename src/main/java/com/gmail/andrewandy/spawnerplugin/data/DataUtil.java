package com.gmail.andrewandy.spawnerplugin.data;

import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.object.OfflineSpawner;
import com.gmail.andrewandy.spawnerplugin.object.Spawner;
import com.gmail.andrewandy.spawnerplugin.util.Common;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public final class DataUtil {

    //Effectively final once setup.
    private static YamlConfiguration dataFile;
    private static File data;

    private static synchronized void save() {
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

    public static Optional<OfflineSpawner> loadData(Location location) {
        ConfigurationSection section = dataFile.getConfigurationSection("Data");
        assert section != null;
        Optional<String> key = section.getValues(false).keySet().stream()
                .filter(str -> str.contains(OfflineSpawner.formatLocation(location.clone())))
                .findFirst();
        if (key.isPresent()) {
            ConfigurationSection section1 = section.getConfigurationSection(key.get());
            if (section1 == null) {
                System.out.println(key);
                throw new IllegalArgumentException("Error in loading data!");
            }
            if (!section1.isItemStack("asItem")) {
                System.out.println(key);
                throw new IllegalArgumentException("Error in loading data!");
            }
            ItemStack itemStack = section1.getItemStack("asItem");
            if (itemStack == null) {
                System.out.println(section1.get("asItem"));
                throw new IllegalArgumentException("Error in loading data!");
            }
            return OfflineSpawner.asOfflineSpawner(itemStack, location);
        } else {
            return Optional.empty();
        }
    }

    public static void saveData(Spawner spawner) {
        final Spawner cloned = spawner.clone();
        Common.asBukkitRunnable(() -> {
            OfflineSpawner offlineSpawner = new OfflineSpawner(cloned);
            ConfigurationSection section = dataFile.getConfigurationSection("Data");
            assert section != null;
            ConfigurationSection section1 = section.createSection(offlineSpawner.getSpawnerIDWithLocation());
            section1.set("asItem", offlineSpawner.getAsItemStack());
            save();
        }).runTaskAsynchronously(SpawnerPlugin.getInstance());
    }

    public static boolean isSaved(UUID spawnerID) {
        ConfigurationSection section = dataFile.getConfigurationSection("Data");
        assert section != null;
        ConfigurationSection section1 = section.getConfigurationSection(spawnerID.toString());
        return section1 == null;
    }

    public static boolean isSaved(Location location) {
        ConfigurationSection section = dataFile.getConfigurationSection("Data");
        assert section != null;
        Optional<String> key = section.getValues(false).keySet().stream()
                .filter(str -> str.contains(OfflineSpawner.formatLocation(location.clone())))
                .findFirst();
        return key.isPresent();
    }

}
