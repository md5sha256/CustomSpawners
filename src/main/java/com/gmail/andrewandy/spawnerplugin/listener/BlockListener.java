package com.gmail.andrewandy.spawnerplugin.listener;

import com.gmail.andrewandy.spawnerplugin.event.SpawnerPlaceEvent;
import com.gmail.andrewandy.spawnerplugin.spawner.AbstractSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.OfflineSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.Spawner;
import com.gmail.andrewandy.spawnerplugin.spawner.custom.CustomAreaSpawner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import java.util.Optional;

public class BlockListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        ItemStack placed = event.getItemInHand();
        NBTItem nbtItem = new NBTItem(placed);
        String rawClass = nbtItem.getString("class");
        String rawWrappedClass = nbtItem.getString("wrappedClass");
        if (rawClass == null) {
            return;
        }
        try {
            Class<?> clazz = Class.forName(rawClass);
            if (!clazz.isAssignableFrom(AbstractSpawner.class)) {
                return;
            }
            Class<? extends AbstractSpawner> casted = clazz.asSubclass(AbstractSpawner.class);
            if (rawWrappedClass != null) {
                Class<?> wrapper = Class.forName(rawWrappedClass);
                if (!wrapper.isAssignableFrom(CustomAreaSpawner.class)) {
                    throw new IllegalStateException();
                }
                Spawner.ItemWrapper itemWrapper = (Spawner.ItemWrapper) casted.getMethod("getWrapper").invoke(null);
                Optional<? extends OfflineSpawner<? extends AbstractSpawner>> offlineSpawner = itemWrapper.fromItem(placed);
                if (!offlineSpawner.isPresent()) {
                    return;
                }
                OfflineSpawner<? extends AbstractSpawner> spawner = offlineSpawner.get();
                Optional<? extends AbstractSpawner> optionalAbstractSpawner = itemWrapper.toLiveAtLocation(spawner, block.getLocation());
                if (!optionalAbstractSpawner.isPresent()) {
                    return;
                }
                Optional<OfflineSpawner<CustomAreaSpawner<? extends AbstractSpawner>>> optional = new CustomAreaSpawner.WrapperImpl(casted).fromItem(placed);
                if (!optional.isPresent()) {
                    return;
                }
                Optional<CustomAreaSpawner<?>> customAreaSpawner = new CustomAreaSpawner.WrapperImpl(casted).toLiveAtLocation(optional.get(), block.getLocation());
                customAreaSpawner.ifPresent((customSpawner) -> {
                    AbstractSpawner.getHandler(AbstractSpawner.class).register(customSpawner);
                    customSpawner.updateBlockState();
                    new SpawnerPlaceEvent(customSpawner).callEvent();
                });
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Optional<MetadataValue> value = block.getMetadata("CustomSpawner").stream().findAny();
        if (!value.isPresent()) {
            return;
        }
        MetadataValue metadataValue = value.get();
        Object raw = metadataValue.value();
        if (!(raw instanceof ItemStack)) {
            return;
        }
        ItemStack itemStack = (ItemStack) raw;
    }
}

