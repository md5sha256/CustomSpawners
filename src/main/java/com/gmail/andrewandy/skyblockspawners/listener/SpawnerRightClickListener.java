package com.gmail.andrewandy.skyblockspawners.listener;

import com.gmail.andrewandy.skyblockspawners.event.SpawnerRightClickEvent;
import com.gmail.andrewandy.skyblockspawners.object.Spawner;
import com.gmail.andrewandy.skyblockspawners.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

public class SpawnerRightClickListener implements Listener {
    @EventHandler
    public void onSpawnerRightClick(SpawnerRightClickEvent event) {

        Spawner spawner = event.getSpawner();
        Player player = event.getPlayer();

        //Create an inventory
        Inventory inv = Bukkit.createInventory(null, 27, Common.colourise("&a&l" + Common.capitalise(spawner.getSpawnedType().name().toLowerCase()) + " Spawner"));

        //Fill all empty slots with Black stained glass
        ItemStack[] contents = inv.getContents();
        for (int index = 0; index < contents.length; index++) {
            if (contents[index] != null) {
                continue;
            }
            ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName("");
            item.setItemMeta(itemMeta);
            contents[index] = item;
        }

        //Exit Button
        ItemStack exitButton = contents[10];
        exitButton.setType(Material.BARRIER);
        ItemMeta exitButtonMeta = exitButton.getItemMeta();
        exitButtonMeta.setDisplayName(Common.colourise("&c&lExit"));
        exitButtonMeta.setLore(Arrays.asList(Common.colourise("&cClick this button to exit the menu.")));
        exitButton.setItemMeta(exitButtonMeta);

        //ShowBalance
        int playerBalance = 1000;
        ItemStack balance1 = contents[12];
        ItemStack balance2 = contents[14];
        balance1.setType(Material.PAPER);
        balance2.setType(Material.PAPER);
        ItemMeta balanceMeta = balance1.getItemMeta();
        balanceMeta.setDisplayName(Common.colourise("&aCurrent Balance"));
        balanceMeta.setLore(Arrays.asList(
                Common.colourise("&eYour Current Balance is: " + playerBalance)));
        balance1.setItemMeta(balanceMeta);
        balance2.setItemMeta(balanceMeta);
        //TODO MainIcon + Upgrade button

        inv.setContents(contents);
        player.openInventory(inv);
    }
}
