package com.gmail.andrewandy.skyblockspawners;

import com.gmail.andrewandy.skyblockspawners.command.SpawnerCommand;
import com.gmail.andrewandy.skyblockspawners.config.Config;
import com.gmail.andrewandy.skyblockspawners.data.PurgeSpawners;
import com.gmail.andrewandy.skyblockspawners.data.SetupDatabase;
import com.gmail.andrewandy.skyblockspawners.data.UpdateSpawners;
import com.gmail.andrewandy.skyblockspawners.listener.BlockPlaceListener;
import com.gmail.andrewandy.skyblockspawners.listener.BlockRightClickListener;
import com.gmail.andrewandy.skyblockspawners.listener.SpawnerPlaceListener;
import com.gmail.andrewandy.skyblockspawners.listener.SpawnerRightClickListener;
import com.gmail.andrewandy.skyblockspawners.manager.SpawnerManager;
import com.gmail.andrewandy.skyblockspawners.util.Common;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class SkyblockSpawnerBukkit extends JavaPlugin {

    public static String prefix = "&a[&bSkyblock&eSpawners&a] ";
    private static SkyblockSpawnerBukkit instance;
    private static Config cfg;
    private static Config data;
    private static SpawnerManager spawnerManager = new SpawnerManager();
    private static Connection databaseConnection;
    private static UpdateSpawners updateTask;

    public static SkyblockSpawnerBukkit getInstance() {
        return instance;
    }

    public static Config getCfg() {
        return cfg;
    }

    public static Config getData() {
        return data;
    }

    public static SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }
    /*
    public void onLoad() {
        instance = this;
        connect(this);
        //cfg = new Config("settings.yml");
    }
    */
    @Override
    public void onEnable() {
        instance = this;
        cfg = new Config("settings.yml");
        cfg.options().copyDefaults();
        connect(this);
        SetupDatabase.setupTables();
        //cfg = new Config("settings.yml");
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new BlockRightClickListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnerPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnerRightClickListener(), this);
        getCommand("SpawnerCommand").setExecutor(new SpawnerCommand());
        Common.log(Level.INFO, "Plugin enabled successfully.");
        updateTask = new UpdateSpawners();
        updateTask.runTaskTimer(this, 10,200000);
    }

    @Override
    public void onDisable() {
        updateTask.cancel();
        UpdateSpawners updateSpawners = new UpdateSpawners();
        updateSpawners.run();
        close(getInstance());
        Common.log(Level.INFO, "Plugin has been disabled.");
        instance = null;
    }



    private static void connect(JavaPlugin plugin) {
        databaseConnection = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + "data.db";
            // create a connection to the database
            databaseConnection = DriverManager.getConnection(url);
            if (databaseConnection == null) {
                SetupDatabase.createDB("data.db");
            }
            Common.log(Level.INFO, "Connected to database.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            Common.log(Level.WARNING, "Unable to connect to database!");
            getInstance().getServer().getPluginManager().disablePlugin(getInstance());
        }
    }

    private static void close(JavaPlugin plugin) {
        databaseConnection = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + "data.db";
            // create a connection to the database
           databaseConnection = DriverManager.getConnection(url);
            databaseConnection.close();

            Common.log(Level.INFO, "Closed connection to database.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static Connection getDatabase() {
        return databaseConnection;
    }
}
