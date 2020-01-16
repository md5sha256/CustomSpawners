package com.gmail.andrewandy.spawnerplugin;

import com.gmail.andrewandy.corelib.util.Common;
import com.gmail.andrewandy.spawnerplugin.command.SpawnerCommand;
import com.gmail.andrewandy.spawnerplugin.config.Config;
import com.gmail.andrewandy.spawnerplugin.listener.BlockListener;
import com.gmail.andrewandy.spawnerplugin.listener.ChunkListener;
import com.gmail.andrewandy.spawnerplugin.spawner.Spawners;
import com.gmail.andrewandy.spawnerplugin.spawner.data.SpawnerData;
import com.gmail.andrewandy.spawnerplugin.util.HeadUtil;
import com.google.common.base.Stopwatch;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SpawnerPlugin extends JavaPlugin {

    public static String prefix = "&a[&bSkyblock&eSpawners&a] ";
    private static SpawnerPlugin instance;
    private static Config cfg;
    //private static SpawnerCache spawnerCache;
    private static Economy economy;
    private static RegisteredServiceProvider<Economy> economyProvider;

    public static SpawnerPlugin getInstance() {
        return instance;
    }

    public static Config getCfg() {
        return cfg;
    }

    // public static SpawnerCache getSpawnerCache() {return spawnerCache;}

    public static Economy getEconomy() {
        return economy;
    }

    @Override
    public void onEnable() {
        instance = this;
        HeadUtil.loadData();
        cfg = new Config("settings.yml");
        cfg.options().copyDefaults();
        /*
        try {
            DataUtil.setupUtil();
        } catch (IOException ex) {
            ex.printStackTrace();
            Common.log(Level.SEVERE, "Unable to setup database.");
        }

         */
        cfg = new Config("settings.yml");
        //spawnerCache = new SpawnerCache();
        setupEconomy();
        //getServer().getPluginManager().registerEvents(new BlockInteractListener(), this);
        getCommand("SpawnerCommand").setExecutor(new SpawnerCommand());
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new ChunkListener(), this);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Spawners.defaultManager().loadAllSpawners(this);
        //Save data every 10 mins or so.
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> SpawnerData.saveToDisk(true), 12000, 10);
        stopwatch.stop();
        Common.log(Level.INFO, "&b[Spawner Loading] Took " + stopwatch.elapsed(TimeUnit.MILLISECONDS), "ms.");
        Common.log(Level.INFO, "Plugin enabled successfully.");
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        } else {
            //DEBUG
            System.out.println("No economy found!");
        }
    }

    @Override
    public void onDisable() {
        /*
        for (Spawner spawner : spawnerCache.getCached()) {
            System.out.println("Saving data");
            DataUtil.saveData(spawner);
        }
        spawnerCache.forceClear(true);
        */
        Common.log(Level.INFO, "&a&lSaving spawner data...");
        Spawners.defaultManager().saveAll();
        Common.log(Level.INFO, "Plugin has been disabled.");
        instance = null;
    }
}
