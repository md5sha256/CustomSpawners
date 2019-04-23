package com.gmail.andrewandy.skyblockspawners.data;

import com.gmail.andrewandy.skyblockspawners.SkyblockSpawnerBukkit;
import com.gmail.andrewandy.skyblockspawners.object.Spawner;
import com.gmail.andrewandy.skyblockspawners.util.Common;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class UpdateSpawners extends BukkitRunnable {


    @Override
    public void run() {

        Set<Spawner> toUpdate = new HashSet<>(SkyblockSpawnerBukkit.getSpawnerManager().getRegisteredSpawners());
        Set<Spawner> toPurge = new HashSet<>(SkyblockSpawnerBukkit.getSpawnerManager().getRegisteredSpawners());
        Set<Spawner> inDB = new HashSet<>();

        System.out.println("toPurge after instantiation: " + toPurge);

        String sql = "SELECT identifier, entityType, level, maxLevel ,delay, world, locationX, locationY, locationZ FROM spawners";

        try {

            Connection connection = SkyblockSpawnerBukkit.getDatabase();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            Common.log(Level.INFO, "Beginning task to save to database.");

            //Loop through everything in the database
            while (resultSet.next()) {
                String identifier = resultSet.getString("identifier");

                if (SkyblockSpawnerBukkit.getSpawnerManager().getSpawnerByIdentifier(identifier) != null) {
                    System.out.println("Identifier isn't null");
                    Spawner spawner = SkyblockSpawnerBukkit.getSpawnerManager().getSpawnerByIdentifier(identifier);
                    inDB.add(spawner);
                    return;
                }
                System.out.println("EntityType creation");
                EntityType entityType = EntityType.valueOf(resultSet.getString("entityType"));
                int level = resultSet.getInt("level");
                int delay = resultSet.getInt("delay");
                int maxLevel = resultSet.getInt("maxLevel");

                World world = SkyblockSpawnerBukkit.getInstance().getServer().getWorld(resultSet.getString("world"));
                int x = resultSet.getInt("locationX");
                int y = resultSet.getInt("locationY");
                int z = resultSet.getInt("locationZ");
                Location location = new Location(world, x, y, z);
                location.setWorld(world);

                Spawner spawner = new Spawner(entityType, delay, level, maxLevel, location);
                SkyblockSpawnerBukkit.getSpawnerManager().registerSpawner(spawner);
                inDB.add(spawner);
            }
            System.out.println("Before: " + SkyblockSpawnerBukkit.getSpawnerManager().getPurge());
            toPurge.addAll(SkyblockSpawnerBukkit.getSpawnerManager().getPurge());
            System.out.println("toPurge: " + toPurge);
            //Check for purging
            for (Spawner spawner : toPurge) {
                System.out.println(spawner);
                if (!inDB.contains(spawner)) {
                    System.out.println("skipping");
                    continue;
                }
                System.out.println("removing");
                toPurge.remove(spawner);

            }

            //Update spawners
            for (Spawner spawner : toUpdate) {
                if (inDB.contains(spawner)) {
                    String request = "UPDATE spawners " + "SET "
                            + "entityType = " + "'" + spawner.getSpawnedType().name() + "'" + ", "
                            + "level = " + "'" + spawner.getLevel() + "'" + ", "
                            + "maxLevel = " + "'" + spawner.getMaxLevel() + "'" + ", "
                            + "delay = " + "'" + spawner.getDelay() + "'" + ", "
                            + "world = " + "'" + spawner.getLocation().getWorld().getName() + "'" + ", "
                            + "locationX = " + "'" + spawner.getLocation().getBlockX() + "'" + ", "
                            + "locationY = " + "'" + spawner.getLocation().getBlockY() + "'" + ", "
                            + "locationZ = " + "'" + spawner.getLocation().getBlockZ() + "' "
                            + "WHERE " + "identifier = " + "'" + spawner.getUniqueIdentifier() + "'";
                    PreparedStatement ps = connection.prepareStatement(request);
                    ps.executeUpdate();
                    ps.close();
                } else {
                    System.out.println("making new");
                    String request = "INSERT INTO spawners (identifier, entityType, delay, level, maxLevel, world, locationX, locationY, locationZ) " + "VALUES "
                            + "("
                            + "'" + spawner.getUniqueIdentifier() + "'" + ","
                            + "'" + spawner.getSpawnedType().name() + "'" + ","
                            + "'" + spawner.getDelay() + "'" + ","
                            + "'" + spawner.getLevel() + "'" + ","
                            + "'" + spawner.getMaxLevel() + "'" + ","
                            + "'" + spawner.getLocation().getWorld().getName() + "'" + ","
                            + "'" + spawner.getLocation().getBlockX() + "'" + ","
                            + "'" + spawner.getLocation().getBlockY() + "'" + ","
                            + "'" + spawner.getLocation().getBlockZ() + "'"
                            + ")";
                    PreparedStatement ps = connection.prepareStatement(request);
                    System.out.println(spawner.getUniqueIdentifier());
                    ps.executeUpdate();
                    ps.close();
                }
            }
            PurgeSpawners purgeTask = new PurgeSpawners(toPurge);
            purgeTask.run();
            Common.log(Level.INFO, "Saving complete.");
            toPurge.clear();
            SkyblockSpawnerBukkit.getSpawnerManager().getPurge().clear();
        } catch (SQLException ex) {
            ex.printStackTrace();
            Common.log(Level.INFO, "test");
        }
    }
}
