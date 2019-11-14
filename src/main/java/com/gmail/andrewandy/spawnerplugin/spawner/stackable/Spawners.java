package com.gmail.andrewandy.spawnerplugin.spawner.stackable;

import com.gmail.andrewandy.spawnerplugin.spawner.AbstractSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.ItemStackSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.LivingEntitySpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.custom.CustomAreaSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.custom.CustomisableSpawner;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.UUID;
import java.util.function.Function;

public final class Spawners {

    public static LivingEntitySpawner singleStackEntitySpawner(Location location, Material baseBlock, UUID owner, int delay, float spawnChance, EntityType spawnedType) {
        return new EntitySpawner(location, baseBlock, owner, delay, spawnChance, spawnedType, 1);
    }

    public static ItemStackSpawner singleStackItemSpawner(Location location, Material baseBlock, UUID owner, int delay, float spawnChance, ItemStack toSpawn) {
        return new ItemSpawner(location, baseBlock, owner, delay, spawnChance, toSpawn, 1);
    }

    public static PotionEffectSpawner singleStackPotionEffectSpawner(Location location, Material baseBlock, UUID owner, int delay, float spawnChance, PotionEffect potionEffect, boolean lingering) {
        return new PotionEffectSpawner(location, baseBlock, owner, delay, spawnChance, potionEffect, lingering, 1);
    }

    public static <T extends AbstractSpawner & CustomisableSpawner & StackableSpawner<T>>
    CustomAreaSpawner<T> singleStackCustomAreaSpawner(T original, Function<Block, Block[]> spawnLocationFunction) {
        return new CustomAreaSpawner<>(original, spawnLocationFunction, 1);
    }

}
