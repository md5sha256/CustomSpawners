package com.gmail.andrewandy.skyblockspawners.data;

import com.gmail.andrewandy.skyblockspawners.SkyblockSpawnerBukkit;
import com.gmail.andrewandy.skyblockspawners.object.Spawner;
import com.gmail.andrewandy.skyblockspawners.util.Common;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;

public class PurgeSpawners extends BukkitRunnable {

    private Set<Spawner> toPurge;

    public PurgeSpawners(Set<Spawner> toPurge) {
        this.toPurge = toPurge;
    }

    @Override
    public void run() {
        try {
            Connection connection = SkyblockSpawnerBukkit.getDatabase();
            if (toPurge == null || toPurge.isEmpty()) {
                Common.log(Level.INFO, "No spawners needed to be purged from DB.");
                return;
            }

            Common.log(Level.INFO, "Beginning database cleanup.");
            for (Spawner spawner : toPurge) {
                String sql = "DELETE FROM spawners WHERE identifier = " + spawner.getUniqueIdentifier();
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.executeUpdate();
                statement.close();
            }
            Common.log(Level.INFO, "Purging complete.");


        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Set<Spawner> getToPurge() {
        return toPurge;
    }

    public void setToPurge(Set<Spawner> toPurge) {
        this.toPurge = toPurge;
    }
}
