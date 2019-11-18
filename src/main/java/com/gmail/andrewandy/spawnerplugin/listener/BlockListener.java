package com.gmail.andrewandy.spawnerplugin.listener;

import com.gmail.andrewandy.spawnerplugin.event.SpawnerBreakEvent;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerPlaceEvent;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerRightClickEvent;
import com.gmail.andrewandy.spawnerplugin.spawner.AbstractSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.OfflineSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.Spawner;
import com.gmail.andrewandy.spawnerplugin.spawner.Spawners;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

/**
 * Handles all block interaction events, calls the corresponding spawner events if necessary.
 */
public class BlockListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Optional<Spawner.ItemWrapper> optionalWrapper = Spawners.getWrapper(event.getItemInHand());
        if (!optionalWrapper.isPresent()) {
            return;
        }
        Spawner.ItemWrapper wrapper = optionalWrapper.get();
        Optional<OfflineSpawner> optionalOfflineSpawner = wrapper.fromItem(event.getItemInHand());
        if (!optionalOfflineSpawner.isPresent()) {
            return;
        }
        Optional<AbstractSpawner> optionalSpawner = wrapper.toLiveAtLocation(optionalOfflineSpawner.get(), event.getBlock().getLocation());
        if (!optionalSpawner.isPresent()) {
            System.out.println("not present");
            return;
        }
        AbstractSpawner spawner = optionalSpawner.get();
        SpawnerPlaceEvent spawnerEvent = new SpawnerPlaceEvent(spawner);
        if (!spawnerEvent.isCancelled()) {
            spawner.register();
            spawner.tick();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Optional<AbstractSpawner> optional = Spawners.defaultManager().getFromLocation(block.getLocation());
        if (!optional.isPresent()) {
            return;
        }
        AbstractSpawner spawner = optional.get();
        SpawnerBreakEvent spawnerEvent = new SpawnerBreakEvent(spawner);
        if (spawnerEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        spawner.unregister(true);
        event.setDropItems(false);
        ItemStack toDrop = (ItemStack) spawner.getAsMetadata().value();
        if (toDrop == null) {
            return;
        }
        block.getWorld().dropItemNaturally(block.getLocation(), toDrop);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }
        Optional<AbstractSpawner> optionalAbstractSpawner = Spawners.defaultManager().getFromLocation(clicked.getLocation());
        if (!optionalAbstractSpawner.isPresent()) {
            return;
        }
        AbstractSpawner spawner = optionalAbstractSpawner.get();
        SpawnerRightClickEvent spawnerEvent = new SpawnerRightClickEvent(spawner, event.getPlayer());
        if (spawnerEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    private void removeSpawners(Collection<Block> blocks) {
        Iterator<Block> iterator = Objects.requireNonNull(blocks).iterator();
        while (iterator.hasNext()) {
            Optional<AbstractSpawner> optionalAbstractSpawner = Spawners.defaultManager().getFromLocation(iterator.next().getLocation());
            if (!optionalAbstractSpawner.isPresent()) {
                continue;
            }
            AbstractSpawner spawner = optionalAbstractSpawner.get();
            if (spawner.isInvulernable()) {
                iterator.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExplode(EntityExplodeEvent event) {
        removeSpawners(event.blockList());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExplode(BlockExplodeEvent event) {
        removeSpawners(event.blockList());
    }
}

