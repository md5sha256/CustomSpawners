package com.gmail.andrewandy.spawnerplugin.listener;

import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerRightClickEvent;
import com.gmail.andrewandy.spawnerplugin.object.Spawner;
import com.gmail.andrewandy.spawnerplugin.util.Common;
import com.gmail.andrewandy.spawnerplugin.util.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;

public final class SpawnerRightClickListener implements Listener {

    @EventHandler
    public void onSpawnerRightClick(SpawnerRightClickEvent event) {
        Gui gui = buildGUI(event);
        event.getPlayer().openInventory(gui.getPageAsInventory(0));
    }

    private Gui buildGUI(SpawnerRightClickEvent event) {
        final Spawner spawner = event.getSpawner();
        Player player = event.getPlayer();
        //Create the GUI.
        Gui gui = new Gui("&a&l" + Common.capitalise(spawner.getSpawnedType().name().toLowerCase()) + " Spawner", 27);
        final ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        ItemStack[] contents = Gui.fillWithItem(filler, new ItemStack[27]);
        //Exit Button
        Gui.Button exitButton = new Gui.Button("&c&lExit", Material.BARRIER, 1);
        exitButton.setLore(Collections.singletonList(Common.colourise("&cClick this button to exit the menu.")));
        //TODO figure out what is wrong here.
        exitButton.setOnAllClicks((inventoryClickEvent) -> inventoryClickEvent.getWhoClicked().closeInventory());
        contents[10] = exitButton;
        //ShowBalance
        double playerBalance = SpawnerPlugin.getEconomy().getBalance(player);
        String formattedBalance = Common.asNumberPrefix(playerBalance);
        Gui.Button balance1 = new Gui.Button("&aCurrent Balance", Material.PAPER, 1);
        ItemStack balance2 = new Gui.Button("&aCurrent Balance", Material.PAPER, 1);
        balance1.setLore(Collections.singletonList(
                Common.colourise("&eYour Current Balance is: " + formattedBalance)));
        balance2.setLore(balance1.getLore());
        contents[12] = balance1;
        contents[14] = balance2;
        //MainIcon
        Gui.Button mainIcon = new Gui.Button("&3" + Common.capitalise(spawner.getSpawnedType().name().toLowerCase()) + " &3Spawner", Material.SPAWNER, 1);
        mainIcon.setLore(Arrays.asList(
                Common.colourise("&b&lInformation:"),
                Common.colourise("  &7- &aMob Type: " + Common.capitalise(spawner.getSpawnedType().name().toLowerCase())),
                Common.colourise("  &7- &eCurrent Level: " + spawner.getLevel()),
                Common.colourise("  &7- &cMax Level:" + spawner.getMaxLevel())
        ));
        contents[13] = mainIcon;
        //Upgrade button 16
        Gui.Button upgrade = new Gui.Button(
                (spawner.getLevel() == spawner.getMaxLevel()) ? Common.colourise("&a&lMax Level.") : Common.colourise("&b&lUpgrade"),
                Material.DIAMOND, 1);
        upgrade.setLore(Collections.singletonList(
                (spawner.getLevel() == spawner.getMaxLevel()) ?
                        Common.colourise("&aThe Max level has been reached.") : Common.colourise("&aLevel " + spawner.getLevel() + " --> " + (spawner.getLevel() + 1))
        ));
        upgrade.setOnAllClicks(
                (inventoryClickEvent -> {
                    if (spawner.getLevel() == spawner.getMaxLevel()) {
                        return;
                    }
                    Gui confirm = new Gui("&e&lAre you sure?", 9);
                    confirm.setPage(0, new ItemStack[9]);
                    Gui.fillWithItem(filler, confirm.getPageSnapshot(0));
                    Gui.Button confirmed = new Gui.Button("&aProceed", Material.EMERALD, 1);
                    confirmed.setOnAllClicks((event1) -> upgradeSpawner((Player) event1.getWhoClicked(), spawner));
                })
        );
        gui.setPage(0, contents);
        return gui;
    }

    private void upgradeSpawner(Player executor, Spawner spawner) {
        double playerBalance = SpawnerPlugin.getEconomy().getBalance(executor);
        double required = spawner.getNextUpgradeCost();
        if (playerBalance < required) {
            Common.tell(executor, "&c&lYou need $" + Common.asNumberPrefix(playerBalance - required) + "more to upgrade!");
            executor.closeInventory();
            return;
        }
        spawner.setLevel(spawner.getLevel() + 1);
        SpawnerPlugin.getEconomy().withdrawPlayer(executor, required);
        Common.tell(executor, "&b&lSuccess! &eThe &n" + Common.capitalise(spawner.getSpawnedType().name().toLowerCase())
                + "&b Spawner has been upgraded to level " + spawner.getLevel() + "!");
    }
}
