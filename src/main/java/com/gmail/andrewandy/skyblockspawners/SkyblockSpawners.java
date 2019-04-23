package com.gmail.andrewandy.skyblockspawners;

import com.gmail.andrewandy.skyblockspawners.config.Config;
import com.gmail.andrewandy.skyblockspawners.data.SetupDatabase;
import com.gmail.andrewandy.skyblockspawners.manager.SpawnerManager;
import com.gmail.andrewandy.skyblockspawners.util.Common;
import world.bentobox.bentobox.api.addons.Addon;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

/*
 * Please ignore this as its just here for if I plan on using bentoBox lul.
 */

public final class SkyblockSpawners extends Addon {

    public static String prefix = "&a[&bSkyblock&eSpawners&a]";
    private static SkyblockSpawners instance;
    private static Config cfg;
    private static Config data;
    private static SpawnerManager spawnerManager = new SpawnerManager();

    public static SkyblockSpawners getInstance() {
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

    private static void connect(Addon addon) {
        Connection connection = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:" + addon.getDataFolder().getAbsolutePath() + File.separator + "data.db";
            // create a connection to the database
            connection = DriverManager.getConnection(url);

            Common.log(Level.INFO, "Connected to database.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                SetupDatabase.createDB("data.db");
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private static void close(Addon addon) {
        Connection connection = null;
        try {
            // db parameters
            String url = addon.getDataFolder().getAbsolutePath() + File.separator + "data.db";
            // create a connection to the database
            connection = DriverManager.getConnection(url);
            connection.close();

            Common.log(Level.INFO, "Closed connection to database.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void onLoad() {
        instance = this;
        connect(this);
        cfg = new Config("settings.yml");
        data = new Config("data.yml");
    }

    @Override
    public void onEnable() {
        Common.log(Level.INFO, "Plugin enabled successfully.");
    }

    @Override
    public void onDisable() {

        Common.log(Level.INFO, "Plugin has been disabled.");
        instance = null;
    }

    @Override
    public void onReload() {
        this.onDisable();
        this.onEnable();
    }
}

