package com.gmail.andrewandy.skyblockspawners.data;

import com.gmail.andrewandy.skyblockspawners.SkyblockSpawnerBukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.sqlite.JDBC;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DataUtil {

    //TODO finish this;
    private static final BukkitRunnable purgeSpawners = null;
    private static final BukkitRunnable loadSpawners = null;
    private static Connection shared;

    private static File database;

    public static void setupUtil() throws IOException, SQLException {
        File pluginDir = SkyblockSpawnerBukkit.getInstance().getDataFolder();
        File db = new File("data.db");
        if (!db.isFile()) {
            db.createNewFile();
        }
        database = db;
        shared = DriverManager.getConnection(JDBC.PREFIX + database.getPath());
    }

    public static Connection getShared() {
        return shared;
    }

    public static Connection getNew() throws SQLException {
        return DriverManager.getConnection(JDBC.PREFIX + database.getPath());
    }
}
