package com.gmail.andrewandy.skyblockspawners.listener;

import com.gmail.andrewandy.skyblockspawners.event.SpawnerRightClickEvent;
import com.gmail.andrewandy.skyblockspawners.object.Spawner;
import com.gmail.andrewandy.skyblockspawners.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class SpawnerRightClickListener implements Listener {

    private static Inventory inv;
    private static Spawner spawner;

    @EventHandler
    public void onSpawnerRightClick(SpawnerRightClickEvent event) {

        spawner = event.getSpawner();
        Player player = event.getPlayer();

        //Create an inventory
        inv = Bukkit.createInventory(null, 27, Common.colourise("&a&l" + Common.capitalise(spawner.getSpawnedType().name().toLowerCase()) + " Spawner"));

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

        //MainIcon
        ItemStack mainIcon = contents[13];
        mainIcon.setType(Material.SPAWNER);
        ItemMeta mainMeta = mainIcon.getItemMeta();
        mainMeta.setDisplayName(Common.colourise("&3" + Common.capitalise(spawner.getSpawnedType().name().toLowerCase()) + " &3Spawner"));
        mainMeta.setLore(Arrays.asList(
                Common.colourise("&b&lInformation:"),
                Common.colourise("  &7- &aMob Type: " + Common.capitalise(spawner.getSpawnedType().name().toLowerCase())),
                Common.colourise("  &7- &eCurrent Level: " + spawner.getLevel()),
                Common.colourise("  &7- &cMax Level:" + spawner.getMaxLevel())
        ));
        mainIcon.setItemMeta(mainMeta);

        //Upgrade button
        ItemStack upgrade = contents[16];
        upgrade.setType(Material.DIAMOND);
        ItemMeta upgradeMeta = upgrade.getItemMeta();
        int level = spawner.getLevel();
        upgradeMeta.setDisplayName(Common.colourise("&b&lUpgrade"));
        upgradeMeta.setLore(Arrays.asList(
                Common.colourise("&aLevel " + level + " --> " + (level + 1))
        ));
        upgrade.setItemMeta(upgradeMeta);

        inv.setContents(contents);
        player.openInventory(inv);
    }


    @EventHandler
    public void inventoryInteractEvent(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        if (!inventory.equals(inv)) {
            return;
        }

        switch (event.getSlot()) {
            default:
                event.setCancelled(true);
            case 10:
                event.setCancelled(true);
                player.closeInventory();
                break;
            case 16:
                event.setCancelled(true);
                player.closeInventory();
                Common.tell(player, "The spawner has been sucessfully updated.");
                Block block = spawner.getLocation().getBlock();
                CreatureSpawner cs = (CreatureSpawner) block.getState();
                cs.setDelay(cs.getDelay() / 2);
                spawner.setLevel(spawner.getLevel() + 1);
                break;
        }

    }
}
