package com.gmail.andrewandy.spawnerplugin.data;

import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.object.Spawner;
import com.gmail.andrewandy.spawnerplugin.util.Common;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

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

    public static void saveData(Spawner spawner) {
        Objects.requireNonNull(spawner);
        Common.asBukkitRunnable(() -> {
            ConfigurationSection section = dataFile.getConfigurationSection("Data");
            assert section != null;
            ConfigurationSection section1 = section.getConfigurationSection(spawner.getIdentifier());
            if (section1 == null) {
                section1 = section.createSection(spawner.getIdentifier());
            }
            String clazz = spawner.getClass().getName();
            section1.set("asItemStack", spawner.getAsItem());
            section1.set("class", clazz);
            save();
        }).runTaskAsynchronously(SpawnerPlugin.getInstance());
    }

    public static Optional<Spawner> loadData(String identifier) {
        Optional<Spawner> optional = Optional.empty();
        if (!isSaved(identifier)) {
            return optional;
        }
        ConfigurationSection section = dataFile.getConfigurationSection("Data");
        assert section != null;
        ConfigurationSection section1 = section.getConfigurationSection(identifier);
        assert section1 != null;
        String clazzName = section1.getString("class");
        try {
            Class<?> clazz = Class.forName(clazzName);
            if (!clazz.isAssignableFrom(Spawner.class)) {
                throw new ClassNotFoundException();
            }
            Class<? extends Spawner> casted = clazz.asSubclass(Spawner.class);
            ItemStack itemStack = section1.getItemStack("asItemStack");

        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            Common.log(Level.WARNING, "&eUnknown Spawner class: " + clazzName);
        }
        return optional;
    }

    public static boolean isSaved(String identifier) {
        ConfigurationSection section = dataFile.getConfigurationSection("Data");
        assert section != null;
        ConfigurationSection section1 = section.getConfigurationSection(identifier);
        return section1 == null;
    }

}
