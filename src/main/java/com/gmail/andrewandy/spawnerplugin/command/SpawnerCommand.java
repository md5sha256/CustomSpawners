package com.gmail.andrewandy.spawnerplugin.command;

import com.gmail.andrewandy.corelib.api.command.BaseCommand;
import com.gmail.andrewandy.corelib.util.Common;
import com.gmail.andrewandy.spawnerplugin.spawner.Spawner;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.EntitySpawner;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;


/**
 * This is a debug command but, can be used to take reference as to how I plan on creating Items and {@link com.gmail.andrewandy.spawnerplugin.spawner.Spawner} objects.
 */
public class SpawnerCommand implements BaseCommand {

    private final static String USAGE = "&b[SkyblockSpawners] Usage: /spawner [type] [spawnedType] [delay] [spawnChance] [maxSize]";
    private final static String UNIMPLEMENTED = "&e[SkyblockSpawners] This feature is unimplemented.";

    @Override
    @SuppressWarnings("unchecked")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Common.tell(sender, "&c&l[Debug] This command can only be executed by  a player.");
            return true;
        }
        Player player = (Player) sender;
        if (!sender.hasPermission("SkyblockSpawners.debug")) {
            Common.tell(sender, "&c[SkyblockSpawners] Insufficient Permission.");
            return true;
        }
        if (args.length < 4) {
            Common.tell(sender, USAGE);
            return true;
        }
        int maxSize;
        int delay;
        float spawnChance;
        ItemStack spawner;
        try {
            delay = Integer.parseInt(args[2]);
            spawnChance = Float.parseFloat(args[3]);
            maxSize = Integer.parseInt(args[4]);
        } catch (NumberFormatException ex) {
            Common.tell(sender, "&e[Debug] Invalid Number provided.");
            return true;
        }
        if (maxSize < 0) {
            Common.tell(sender, "&e[Debug] Invalid MaxSize");
            return true;
        }
        if (delay < 1) {
            Common.tell(sender, "&e[Debug] Invalid Delay.");
            return true;
        }
        if (spawnChance < 0.00 || spawnChance > 1.00) {
            Common.tell(sender, "&e[Debug] Invalid SpawnChance. Chance must be between 0.00 and 1.00");
            return true;
        }
        String targetType = args[0];
        switch (targetType.toLowerCase()) {
            case "entity":
                EntityType entityType;
                try {
                    entityType = EntityType.valueOf(args[1].toUpperCase());
                    Class<? extends Entity> clazz = entityType.getEntityClass();
                    if (clazz == null) {
                        throw new IllegalArgumentException();
                    }
                    if (!Mob.class.isAssignableFrom(clazz) || Player.class.isAssignableFrom(clazz)) {
                        throw new IllegalArgumentException();
                    }
                } catch (IllegalArgumentException ex) {
                    Common.tell(sender, "&e[Debug] Invalid EntityType.");
                    return true;
                }
                EntitySpawner target = new EntitySpawner(player.getLocation(), Material.SPAWNER, player.getUniqueId(), delay, spawnChance, entityType, maxSize);
                spawner = ((Spawner.ItemWrapper<EntitySpawner>) EntitySpawner.getWrapper()).toItem(target);
                break;
            case "item":
            case "potion":
                Common.tell(sender, UNIMPLEMENTED);
                return true;
            default:
                Common.tell(sender, "&c&l[Debug] Invalid Spawner Type.");
                return true;
        }
        ItemMeta itemMeta = spawner.getItemMeta();
        itemMeta.setDisplayName(Common.colourise("&b&l[Debug] Custom Spawner."));
        spawner.setItemMeta(itemMeta);
        player.getInventory().addItem(spawner);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }


}
