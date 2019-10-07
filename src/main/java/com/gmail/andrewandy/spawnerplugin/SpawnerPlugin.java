package com.gmail.andrewandy.spawnerplugin;

import com.gmail.andrewandy.spawnerplugin.command.SpawnerCommand;
import com.gmail.andrewandy.spawnerplugin.config.Config;
import com.gmail.andrewandy.spawnerplugin.data.DataUtil;
import com.gmail.andrewandy.spawnerplugin.data.SpawnerCache;
import com.gmail.andrewandy.spawnerplugin.listener.*;
import com.gmail.andrewandy.spawnerplugin.util.Common;
import com.gmail.andrewandy.spawnerplugin.util.Gui;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

public class SpawnerPlugin extends JavaPlugin {

    public static String prefix = "&a[&bSkyblock&eSpawners&a] ";
    private static SpawnerPlugin instance;
    private static Config cfg;
    private static SpawnerCache spawnerCache;
    private static Economy economy;
    private static RegisteredServiceProvider<Economy> economyProvider;

    public static SpawnerPlugin getInstance() {
        return instance;
    }

    public static Config getCfg() {
        return cfg;
    }

    public static SpawnerCache getSpawnerCache() {
        return spawnerCache;
    }

    public static Economy getEconomy() {
        return economy;
    }

    @Override
    public void onEnable() {
        instance = this;
        Gui.setupHandler(instance);
        cfg = new Config("settings.yml");
        cfg.options().copyDefaults();
        try {
            DataUtil.setupUtil();
        } catch (IOException ex) {
            ex.printStackTrace();
            Common.log(Level.SEVERE, "Unable to setup database.");
        }
        cfg = new Config("settings.yml");
        spawnerCache = new SpawnerCache();
        setupEconomy();
        new BlockBreakListener();
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new BlockRightClickListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnerPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnerRightClickListener(), this);
        getCommand("SpawnerCommand").setExecutor(new SpawnerCommand());
        Common.log(Level.INFO, "Plugin enabled successfully.");
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        } else {
            //DEBUG
            System.out.println("NO economy found!");
        }
    }

    @Override
    public void onDisable() {
        Common.log(Level.INFO, "Plugin has been disabled.");
        instance = null;
    }
}