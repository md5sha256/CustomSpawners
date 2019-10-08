package com.gmail.andrewandy.spawnerplugin.listener;

import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.data.DataUtil;
import com.gmail.andrewandy.spawnerplugin.data.SpawnerCache;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerBreakEvent;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerEvent;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerPlaceEvent;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerRightClickEvent;
import com.gmail.andrewandy.spawnerplugin.object.*;
import com.gmail.andrewandy.spawnerplugin.util.Common;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BlockInteractListener implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getMetadata("customSpawner").stream().anyMatch(
                (meta) -> {
                    Plugin plugin = meta.getOwningPlugin();
                    if (plugin == null) {
                        return false;
                    }
                    return plugin.getName().equalsIgnoreCase(SpawnerPlugin.getInstance().getName());
                })) {
            Material material = block.getType();
            BlockState state = block.getState();
            if (!(state instanceof CreatureSpawner)) {
                System.out.println(material);
                System.out.println("not instance!");
            }
            if (SpawnerPlugin.getSpawnerCache().isCached(Spawner.getTestIdentifier(block.getLocation()))) {
                Spawner spawner = SpawnerPlugin.getSpawnerCache().getFromCache(Spawner.getTestIdentifier(block.getLocation()));
                assert spawner != null;
                SpawnerBreakEvent breakEvent = new SpawnerBreakEvent(event.getPlayer(), spawner);
                if (breakEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerBreak(SpawnerBreakEvent event) {
        Entity breaker = event.getBreaker();
        if (!(breaker instanceof Player)) {
            return;
        }
        Player player = (Player) breaker;
        Spawner spawner = event.getSpawner();
        if (!spawner.getOwner().equals(player.getUniqueId()) || spawner.getTeamMembers().contains(player.getUniqueId())) {
            Common.tell(player, "&cOops! &dLooks like you don't have permission to interact with this spawner!");
            event.setCancelled(true);
            return;
        }
        ItemStack item;
        if (spawner instanceof LivingEntitySpawner) {
            LivingEntitySpawner entitySpawner = (LivingEntitySpawner) spawner;
            item = entitySpawner.getAsItem();
        } else if (spawner instanceof StackableItemSpawner) {
            item = spawner.getAsItem();
        } else {
            item = spawner.getAsItem();
        }
        spawner.getLocation().getWorld().dropItemNaturally(spawner.getLocation(), item);
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        Block clicked = event.getClickedBlock();
        assert clicked != null;
        Spawner spawner = SpawnerPlugin.getSpawnerCache().getFromCache(Spawner.getTestIdentifier(clicked.getLocation()));
        if (spawner == null) {
            Optional<OfflineSpawner> optional = DataUtil.loadData(clicked.getLocation());
            if (!optional.isPresent()) {
                return;
            }
            Optional<Spawner> loading = optional.get().asSpawner(clicked.getLocation());
            if (!loading.isPresent()) {
                return;
            }
            Spawner clickedSpawner = loading.get();
            SpawnerPlugin.getSpawnerCache().cache(clickedSpawner);
            SpawnerRightClickEvent clickEvent = new SpawnerRightClickEvent(event.getPlayer(), clickedSpawner);
            if (clickEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        ItemStack itemStack = event.getItemInHand();
        Optional<OfflineSpawner> offlineSpawner = OfflineSpawner.asOfflineSpawner(itemStack, block.getLocation());
        if (!offlineSpawner.isPresent()) {
            System.out.println("Not present.");
            return;
        }
        Optional<Spawner> spawner = offlineSpawner.get().asSpawner(block.getLocation());
        if (!spawner.isPresent()) {
            return;
        }
        Spawner live = spawner.get();
        SpawnerPlaceEvent spawnerEvent = new SpawnerPlaceEvent(event.getPlayer(), live, event.getHand());
        spawnerEvent.callEvent();
        if (spawnerEvent.isCancelled()) {
            return;
        }
        SpawnerPlugin.getSpawnerCache().cache(live);
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerPlace(SpawnerPlaceEvent event) {
        Player player = event.getPlayer();
        Spawner spawner = event.getSpawner();
        if (!spawner.getOwner().equals(player.getUniqueId()) && !spawner.getTeamMembers().contains(player.getUniqueId())) {
            event.setCancelled(true);
            Common.tell(player, "&cOops! &dLooks like you don't have permission to interact with this spawner!");
            return;
        }
        spawner.getLocation().getBlock().setMetadata("customSpawner", new FixedMetadataValue(SpawnerPlugin.getInstance(), spawner.getClass().getName()));
        player.getInventory().setItem(event.getSlot(), null);
        Common.tell(player, "&aYou have just broken a spawner.");
    }

}
