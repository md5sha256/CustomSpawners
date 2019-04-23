package com.gmail.andrewandy.skyblockspawners.data;

import com.gmail.andrewandy.skyblockspawners.SkyblockSpawnerBukkit;
import com.gmail.andrewandy.skyblockspawners.SkyblockSpawners;
import com.gmail.andrewandy.skyblockspawners.util.Common;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;

public class SetupDatabase {

    public static void createDB(String filename) {
        File db = new File(SkyblockSpawnerBukkit.getInstance().getDataFolder(), filename);
        String url = "jdbc:sqlite:";
        try (Connection connection = DriverManager.getConnection(url + SkyblockSpawnerBukkit.getInstance().getDataFolder().getAbsolutePath()+ File.separator + filename)) {
            if (connection != null && db.isFile()) {
                DatabaseMetaData meta = connection.getMetaData();
                Common.log(Level.INFO, "Database created.");
            }

        } catch (
                SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void setupTables() {
        Connection connection = SkyblockSpawnerBukkit.getDatabase();
        try {
            String sql = "CREATE TABLE IF NOT EXISTS spawners ("
                    + "	identifier text PRIMARY KEY,"
                    + "	entityType text NOT NULL,"
                    + " delay integer NOT NULL,"
                    + " level integer NOT NULL,"
                    + " maxLevel integer NOT NULL,"
                    + " world text NOT NULL,"
                    + " locationX integer NOT NULL,"
                    + " locationY integer NOT NULL,"
                    + " locationZ integer NOT NULL"
                    + ");";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
