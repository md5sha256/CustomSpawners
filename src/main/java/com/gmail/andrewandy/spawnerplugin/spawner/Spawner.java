package com.gmail.andrewandy.spawnerplugin.spawner;

import com.gmail.andrewandy.corelib.util.Common;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Objects;
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

    void updateBlockState();

    default boolean isInvalidLocation(Location location) {
        return isInvalidLocation(location, 1, true, true, 1);
    }

    default boolean isInvalidLocation(Location location, int height, boolean checkUp, boolean checkDown) {
        return isInvalidLocation(location, height, checkUp, checkDown, 1);
    }

    default boolean isInvalidLocation(Location location, int height, boolean checkUp, boolean checkDown, int width) {
        return Common.nextAirBlock(location, height, checkUp, checkDown, width).isPresent();
    }

    default Optional<Block> nearestAirBlock() {
        return Common.nextAirBlock(getLocation(), 1, true, true, 1);
    }

    default boolean currentLocationInvalid() {
        return isInvalidLocation(getLocation());
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
         * @see #place(AbstractSpawner, boolean) to place the spawner.
         * @see #toLiveAtLocation(OfflineSpawner, Location) to re-create the spawner object at a given location.
         */
        public abstract Optional<OfflineSpawner<T>> fromItem(ItemStack itemStack);

        /**
         * This is not to be confused with {@link #place(AbstractSpawner, boolean)}
         * This method creates a spawner object at a given location but does NOT initialize
         * the spawner.
         *
         * @param spawner  The offline spawner to be made live.
         * @param location The location for the spawner to be made live at.
         * @return Returns a populated optional of the Live version of the spawner if the
         * {@link #fromItem(ItemStack)} of the spawner is populated. Else it will return empty.
         */
        public abstract Optional<T> toLiveAtLocation(OfflineSpawner<T> spawner, Location location);

        /**
         * This is not to be confused with {@link #toLiveAtLocation(OfflineSpawner, Location)}
         * This method attempts to "place" or intialize the spawner object.
         *
         * @param spawner      The spawner object to initialize.
         * @param replaceIfAir Whether to continue if the block at the location is not air.
         * @return Returns true, if the block was updated. False if the block was not updated
         * due to it not being air.
         */
        public boolean place(T spawner, boolean replaceIfAir) {
            Objects.requireNonNull(spawner);
            Block block = spawner.getLocation().getBlock();
            if (block.getType().isAir() && !replaceIfAir) {
                return false;
            }
            block.setType(spawner.getBlockMaterial());
            block.setMetadata(spawner.getClass().getName(), spawner.getAsMetadata());
            return true;
        }

        public abstract boolean isSpawner(ItemStack itemStack);

    }

}
