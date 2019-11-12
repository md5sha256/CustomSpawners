package com.gmail.andrewandy.spawneraddon;

import com.gmail.andrewandy.spawnerplugin.config.Config;
import com.gmail.andrewandy.spawnerplugin.util.Common;
import world.bentobox.bentobox.api.addons.Addon;

import java.util.logging.Level;

/*
 * Please ignore this as its just here for if I plan on using bentoBox lul.
 */

public final class SpawnerAddon extends Addon {

    public static String prefix = "&a[&bSkyblock&eSpawners&a]";
    private static SpawnerAddon instance;
    private static Config cfg;
    private static Config data;

    public static SpawnerAddon getInstance() {
        return instance;
    }

    public static Config getCfg() {
        return cfg;
    }

    public static Config getData() {
        return data;
    }

    @Override
    public void onLoad() {
        instance = this;
        cfg = new Config("settings.yml");
        data = new Config("data.yml");
    }

    @Override
    public void onEnable() {
        Common.log(Level.INFO, prefix + " " + "Plugin enabled successfully.");
    }

    @Override
    public void onDisable() {

        Common.log(Level.INFO, prefix + " " + "Plugin has been disabled.");
        instance = null;
    }

    @Override
    public void onReload() {
        this.onDisable();
        this.onEnable();
    }
}

