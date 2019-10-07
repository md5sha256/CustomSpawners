package com.gmail.andrewandy.spawnerplugin.command;

import com.gmail.andrewandy.spawnerplugin.util.Common;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;


/**
 * This is a debug command but, can be used to take reference as to how I plan on creating Items and {@link com.gmail.andrewandy.spawnerplugin.object.Spawner} objects.
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
        try {
            Integer.parseInt(args[1]);
            Integer.parseInt(args[2]);
            EntityType.valueOf(args[3]);
        } catch (IllegalArgumentException ignored) {
            Common.tell(sender, "Invalid syntax");
        }
        ItemStack spawner = new ItemStack(Material.SPAWNER);
        NBTItem item = new NBTItem(spawner);
        item.setString("spawner", "true");
        item.setInteger("delay", Integer.parseInt(args[1]));
        item.setInteger("level", 1);
        item.setInteger("maxLevel", Integer.parseInt(args[2]));
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
