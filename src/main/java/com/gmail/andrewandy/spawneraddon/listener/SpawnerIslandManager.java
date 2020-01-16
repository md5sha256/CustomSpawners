package com.gmail.andrewandy.spawneraddon.listener;

import com.gmail.andrewandy.corelib.api.menu.Menu;
import com.gmail.andrewandy.corelib.util.ItemBuilder;
import com.gmail.andrewandy.spawneraddon.SpawnerAddon;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//TODO
public class SpawnerIslandManager {

    private final Map<Island, Integer> spawnerCount = new HashMap<>();

    public SpawnerIslandManager() {
        SpawnerAddon.getInstance().registerListener(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onSpawnerClick(SpawnerPlaceEvent event) {
                Location location = event.getSpawner().getLocation();
                IslandsManager manager = BentoBox.getInstance().getIslands();
                manager.getIslandAt(location).ifPresent(island -> {
                    if (spawnerCount.get(island) > 40) {
                        PanelItemBuilder builder = new PanelItemBuilder();
                        PanelItem item = builder.icon(
                                new ItemBuilder(Material.SPAWNER)
                                        .setDisplayName("&b&lSpawner Count")
                                        .setAmount(1)
                                .setLore(Collections.singletonList("&aCurrent Spawners: " + spawnerCount.get(island) + "/" +  40))
                                        .build())
                                .glow(true)
                                .build();
                        Panel panel = new Panel();
                        panel.setInventory(Bukkit.createInventory(null, 9, "&b&lCustom Island Settings."));
                        //panel.open();
                        // Common.tell(event.getPlayer(), "&c[Spawners] Max Spawnercount for this island has been reached!");
                    }
                });

            }
        });
    }

    public int getSpawnerCount(Island island) {
        return spawnerCount.get(Objects.requireNonNull(island));
    }
}
