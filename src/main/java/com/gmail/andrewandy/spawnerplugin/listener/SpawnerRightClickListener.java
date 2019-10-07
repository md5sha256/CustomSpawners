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
import java.util.HashMap;
import java.util.Map;

public final class SpawnerRightClickListener implements Listener {

    @EventHandler
    public void onSpawnerRightClick(SpawnerRightClickEvent event) {
        Gui gui = buildGUI(event);
        Gui.Page page = gui.getPage(0);
        event.getPlayer().openInventory(page.getInventory(gui));
    }

    private Gui buildGUI(SpawnerRightClickEvent event) {
        final Spawner spawner = event.getSpawner();
        Player player = event.getPlayer();
        //Create the GUI.
        Gui gui = new Gui("&a&l" + Common.capitalise(spawner.getSpawnedType().name().toLowerCase()) + " Spawner", 27);
        Map<Integer, Gui.Button> buttonMap = new HashMap<>();
        final ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        ItemStack[] contents = new ItemStack[27];
        Gui.fillWithItem(filler, contents);
        //Exit Button
        Gui.Button exitButton = new Gui.Button("&c&lExit", Material.BARRIER, 1);
        exitButton.setLore(Collections.singletonList(Common.colourise("&cClick this button to exit the menu.")));
        exitButton.setOnAllClicks((inventoryClickEvent) -> inventoryClickEvent.getWhoClicked().closeInventory());
        contents[10] = exitButton;
        buttonMap.put(10, exitButton);
        //ShowBalance
        double playerBalance = SpawnerPlugin.getEconomy().getBalance(player);
        String formattedBalance = Common.asNumberPrefix(playerBalance);
        Gui.Button balance1 = new Gui.Button("&aCurrent Balance", Material.PAPER, 1);
        balance1.setLore(Collections.singletonList(
                Common.colourise("&eYour Current Balance is: " + formattedBalance)));
        contents[12] = balance1;
        contents[14] = balance1;
        buttonMap.put(12, balance1);
        buttonMap.put(14, balance1);
        //MainIcon
        Gui.Button mainIcon = new Gui.Button("&3" + Common.capitalise(spawner.getSpawnedType().name().toLowerCase()) + " &3Spawner", Material.SPAWNER, 1);
        mainIcon.setLore(Arrays.asList(
                Common.colourise("&b&lInformation:"),
                Common.colourise("  &7- &aMob Type: " + Common.capitalise(spawner.getSpawnedType().name().toLowerCase())),
                Common.colourise("  &7- &eCurrent Level: " + spawner.getLevel()),
                Common.colourise("  &7- &cMax Level: " + spawner.getMaxLevel())
        ));
        contents[13] = mainIcon;
        buttonMap.put(13, mainIcon);
        //Upgrade button 16
        Gui.Button upgrade = new Gui.Button(
                (spawner.getLevel() == spawner.getMaxLevel()) ? Common.colourise("&a&lMax Level.") : Common.colourise("&b&lUpgrade"),
                Material.DIAMOND, 1);
        if (spawner.getLevel() == spawner.getMaxLevel()) {
            upgrade.setLore(Collections.singletonList(Common.colourise("&aThe Max level has been reached.")));
        } else {
            upgrade.setLore(Arrays.asList(
                    Common.colourise("&aLevel " + spawner.getLevel() + " --> " + (spawner.getLevel() + 1)),
                    Common.colourise("&bCost to next Level: " + spawner.getNextUpgradeCost())));
        }
        upgrade.setOnAllClicks(
                (inventoryClickEvent -> {
                    if (spawner.getLevel() == spawner.getMaxLevel()) {
                        return;
                    }
                    ItemStack[] contents1 = new ItemStack[9];
                    Gui.fillWithItem(filler, contents1);
                    Gui confirm = new Gui("&e&lAre you sure?", 9);
                    Map<Integer, Gui.Button> buttons = new HashMap<>();
                    Gui.Button confirmed = new Gui.Button("&a&lProceed", Material.EMERALD_BLOCK, 1);
                    confirmed.setOnAllClicks((event1) -> upgradeSpawner((Player) event1.getWhoClicked(), spawner));
                    buttons.put(2, confirmed);
                    contents1[2] = confirmed;
                    Gui.Button abort = new Gui.Button("&c&lAbort", Material.BARRIER, 1);
                    abort.setOnAllClicks((event1) -> {
                        event1.getWhoClicked().closeInventory();
                        Common.tell(event1.getWhoClicked(), "&cOperation aborted.");
                    });
                    buttons.put(6, abort);
                    contents1[6] = abort;
                    Gui.Page page = new Gui.Page(contents1, buttons);
                    confirm.setPage(0, page);
                    event.getPlayer().openInventory(confirm.getPage(0).getInventory(confirm));
                })
        );
        contents[16] = upgrade;
        buttonMap.put(16, upgrade);
        Gui.Page page = new Gui.Page(contents, buttonMap);
        gui.setPage(0, page);
        return gui;
    }

    private void upgradeSpawner(Player executor, Spawner spawner) {
        double playerBalance = SpawnerPlugin.getEconomy().getBalance(executor);
        double required = spawner.getNextUpgradeCost();
        if (playerBalance < required) {
            Common.tell(executor, "&c&lYou need $" + Common.asNumberPrefix(required - playerBalance) + " more to upgrade!");
            executor.closeInventory();
            return;
        }
        spawner.setLevel(spawner.getLevel() + 1);
        SpawnerPlugin.getEconomy().withdrawPlayer(executor, required);
        Common.tell(executor, "&b&lSuccess! &eThe " + Common.capitalise(spawner.getSpawnedType().name().toLowerCase())
                + "&e Spawner &bhas been upgraded to level " + spawner.getLevel() + "!");
        executor.closeInventory();
    }
}
