package com.gmail.andrewandy.spawnerplugin.spawner;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface Spawner {

    static ItemWrapper<? extends Spawner> getWrapper() {
        throw new UnsupportedOperationException("Subclass must hide this method.");
    }

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

        public abstract Optional<T> place(OfflineSpawner<T> spawner, Location location);

        public abstract boolean isSpawner(ItemStack itemStack);

    }

}
