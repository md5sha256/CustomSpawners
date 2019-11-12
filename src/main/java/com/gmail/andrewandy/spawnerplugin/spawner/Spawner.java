package com.gmail.andrewandy.spawnerplugin.spawner;

import com.gmail.andrewandy.spawnerplugin.util.Common;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public interface Spawner {

    Location getLocation();

    int getDelay();

    UUID getOwner();

    Collection<UUID> getPeers();

    void addPeer(UUID peer);

    void removePeer(UUID peer);

    Material getBlockMaterial();

    float getSpawnChance();
    void setSpawnChance(float chance);

    default boolean isPeer(UUID uuid) {
        return getPeers().contains(uuid);
    }

    static ItemWrapper<? extends Spawner> getWrapper() {
        throw new UnsupportedOperationException("Subclass must hide this method.");
    }

    BlockState getAsBlockState();

    default boolean inInvalidLocation() {
        Block block = getLocation().getBlock();
        return block.getRelative(BlockFace.UP).getType().isAir()
                && block.getRelative(BlockFace.DOWN).getType().isAir()
                && block.getRelative(BlockFace.EAST).getType().isAir()
                && block.getRelative(BlockFace.WEST).getType().isAir();
    }

    abstract class ItemWrapper<T extends AbstractSpawner> {

        protected ItemWrapper() {
        }

        public abstract ItemStack toItem(T spawner);

        /**
         * Attempts to create an OfflineSpawner of the original Spawner from its itemstack component.
         *
         * @param itemStack The itemstack to recreate to an OfflineSpawner.
         * @return Returns a populated optional of the OfflineSpawner version of the spawner, an empty
         * optional if there was any errors in re-creating the offline spawner.
         * @see #place(OfflineSpawner, Location) to place the spawner.
         */
        public abstract Optional<OfflineSpawner<T>> fromItem(ItemStack itemStack);

        public Optional<T> place(OfflineSpawner<T> offlineVersion, Location location) {
            Objects.requireNonNull(offlineVersion);
            Objects.requireNonNull(Objects.requireNonNull(location).getWorld());
            Class<? extends AbstractSpawner> clazz = offlineVersion.getOriginalClass();
            try {
                Method method = clazz.getMethod("fromItem", ItemStack.class, Location.class);
                @SuppressWarnings("unchecked")
                T result = (T) method.invoke(null, offlineVersion.getItemStack(), location);
                result.initialize();
                return Optional.of(result);
            } catch (ReflectiveOperationException ex) {
                return Optional.empty();
            } catch (UnsupportedOperationException ex) {
                Common.log(Level.WARNING, "A an invalid spawner class was registered: " + clazz.getName());
                Common.log(Level.WARNING, ex.getMessage());
                return Optional.empty();
            }
        }

        public abstract boolean isSpawner(ItemStack itemStack);

    }

}
