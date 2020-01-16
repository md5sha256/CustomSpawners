package com.gmail.andrewandy.spawnerplugin.listener;

import com.gmail.andrewandy.corelib.util.Common;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerBreakEvent;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerPlaceEvent;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerRightClickEvent;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerStackEvent;
import com.gmail.andrewandy.spawnerplugin.spawner.AbstractSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.OfflineSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.Spawners;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.StackableSpawner;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
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
        Optional<? extends AbstractSpawner> optionalAbstractSpawner = Spawners.registerIfPresent(event.getItemInHand(), event.getBlock().getLocation());
        if (!optionalAbstractSpawner.isPresent()) {
            return;
        }
        AbstractSpawner spawner = optionalAbstractSpawner.get();
        SpawnerPlaceEvent spawnerEvent = new SpawnerPlaceEvent(spawner);
        if (spawnerEvent.isCancelled()) {
            event.setCancelled(true);
            spawner.unregister();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Spawners.defaultManager().getFromLocation(block.getLocation()).ifPresent(spawner -> {
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
        });
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
        Spawners.defaultManager().getFromLocation(clicked.getLocation()).ifPresent(spawner -> {
            SpawnerRightClickEvent spawnerEvent = new SpawnerRightClickEvent(spawner, event.getPlayer(), event.getItem());
            if (spawnerEvent.isCancelled()) {
                event.setCancelled(true);
            }
        });
    }

    private void removeSpawners(Collection<Block> blocks) {
        final Iterator<Block> iterator = Objects.requireNonNull(blocks).iterator();
        while (iterator.hasNext()) {
            Spawners.defaultManager().getFromLocation(iterator.next().getLocation()).ifPresent(spawner -> {
                if (spawner.isInvulernable()) {
                    iterator.remove();
                }
            });
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

    @SuppressWarnings({"RawTypes", "Unchecked"})
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSpawnerClick(SpawnerRightClickEvent event) {
        if (event.getItemInHand() != null) {
            Optional<OfflineSpawner<?>> optionalSpawner = OfflineSpawner.ofItem(event.getItemInHand());
            optionalSpawner.ifPresent(offlineSpawner -> {
                if (offlineSpawner.getOriginalClass().isAssignableFrom(StackableSpawner.class) && event.getSpawner() instanceof StackableSpawner) {
                    StackableSpawner stackableSpawner = (StackableSpawner<?>) event.getSpawner();
                    if (Common.classEquals(stackableSpawner.getClass(), offlineSpawner.getOriginalClass())) {
                        SpawnerStackEvent spawnerStackEvent = new SpawnerStackEvent(stackableSpawner, offlineSpawner);
                        spawnerStackEvent.callEvent();
                        if (!spawnerStackEvent.isCancelled()) {
                            if (stackableSpawner.isFull()) {
                                Common.tell(event.getClicker(), "&e[Spawners] This stack of spawners is full!");
                                return;
                            }
                            stackableSpawner.stack(offlineSpawner);
                            Common.tell(event.getClicker(), "&b[Spawners] You have successfully stacked a spawner. " +
                                    "&aThere are currently " + stackableSpawner.size() + "/" + stackableSpawner.maxSize() + " spawners in this stack.");
                        }
                    }
                }
            });
        } else {
            if (event.getSpawner() instanceof AbstractSpawner && event.getClicker() instanceof HumanEntity) {
                ((AbstractSpawner) event.getSpawner()).getDisplayUI().ifPresent(menu -> menu.openMenu((HumanEntity) event.getClicker(), 0));
            }
        }
    }
}

