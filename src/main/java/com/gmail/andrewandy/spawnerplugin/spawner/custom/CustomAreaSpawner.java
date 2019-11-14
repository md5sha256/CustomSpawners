package com.gmail.andrewandy.spawnerplugin.spawner.custom;

import com.gmail.andrewandy.corelib.util.gui.Gui;
import com.gmail.andrewandy.spawnerplugin.spawner.AbstractSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.OfflineSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.StackableSpawner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.*;
import java.util.function.Function;

public class CustomAreaSpawner<T extends AbstractSpawner & CustomisableSpawner> extends AbstractSpawner implements StackableSpawner<CustomAreaSpawner<T>> {

    private final Function<Block, Block[]> spawnerFunction;
    private final int maxSize;
    private T spawner;

    /**
     * Creates a custom area spawner backed by a CustomisableSpawner.
     * @param spawner The spawner to spawn.
     * @param spawnLocationFunction The function used to determine which blocks should be targeted to spawn.
     */
    public CustomAreaSpawner(T spawner, Function<Block, Block[]> spawnLocationFunction, int maxSize){
        super(spawner.getLocation(), spawner.getBlockMaterial(), spawner.getOwner(), spawner.getDelay(), spawner.getSpawnChance());
        this.spawner = spawner;
        super.peers = new HashSet<>(spawner.getPeers());
        this.spawnerFunction = Objects.requireNonNull(spawnLocationFunction);
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        if (maxSize > 1 && !(spawner instanceof StackableSpawner)) {
            throw new IllegalArgumentException("Base spawner is not stackable.");
        }
        this.maxSize = maxSize;
    }

    @Override
    protected void tick() {
        super.tick();
        for (Block block : spawnerFunction.apply(getLocation().getBlock())) {
            spawner.spawnTick(block);
        }
    }

    public T getWrappedSpawner() {
        return spawner;
    }

    @Override
    public BlockState getAsBlockState() {
        return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public Optional<Gui> getDisplayUI() {
        return Optional.empty();
    }

    @Override
    public Collection<OfflineSpawner<CustomAreaSpawner<T>>> getStacked() {
        return null;
    }

    @Override
    public void stack(OfflineSpawner<CustomAreaSpawner<T>> spawner) {

    }

    @Override
    public void remove(OfflineSpawner<CustomAreaSpawner<T>> spawner) {

    }

    @Override
    public boolean stackAll(Collection<OfflineSpawner<CustomAreaSpawner<T>>> offlineSpawners) {
        return false;
    }

    @Override
    public int maxSize() {
        return maxSize;
    }
}
