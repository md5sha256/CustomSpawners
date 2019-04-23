package com.gmail.andrewandy.skyblockspawners.command;

import com.gmail.andrewandy.skyblockspawners.SkyblockSpawnerBukkit;
import com.gmail.andrewandy.skyblockspawners.util.Common;
import de.tr7zw.itemnbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.util.Arrays;
import java.util.UUID;


/**
 * This is a debug command but, can be used to take reference as to how I plan on creating Items and {@link com.gmail.andrewandy.skyblockspawners.object.Spawner} objects.
 */
public class SpawnerCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Common.tell(sender, "&aThis command can only be run by players.");
            return false;
        }
        Player player = (Player) sender;

        if (args.length != 3) {
            return false;
        }
        ItemStack spawner = new ItemStack(Material.SPAWNER);
        NBTItem item = new NBTItem(spawner);
        item.setString("spawner", "true");
        item.setInteger("delay", Integer.valueOf(args[1]));
        item.setInteger("level", 1);
        item.setInteger("maxLevel", Integer.valueOf(args[2]));
        item.setString("entityType", EntityType.valueOf(args[0].toUpperCase()).name());
        ItemStack finalItem = item.getItem();
        ItemMeta itemMeta = finalItem.getItemMeta();
        itemMeta.setDisplayName(Common.colourise("&e" + Common.capitalise(args[0].toLowerCase()) + "&e Spawner"));
        itemMeta.setLore(Arrays.asList(
                Common.colourise(""),
                Common.colourise("&b&lInformation:"),
                Common.colourise("  &7-&a Mob: " + Common.capitalise(args[0].toLowerCase())),
                Common.colourise("  &7-&e Level: " + 1)
        ));
        finalItem.setItemMeta(itemMeta);
        player.getInventory().addItem(finalItem);
        return false;
    }
}
