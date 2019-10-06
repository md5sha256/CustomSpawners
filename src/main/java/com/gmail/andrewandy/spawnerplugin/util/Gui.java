package com.gmail.andrewandy.spawnerplugin.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;

public class Gui {

    private int size;
    private String defaultName;
    private static Set<Inventory> registeredInventories = new HashSet<>();
    private static Listener GuiHandler = new Listener() {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!registeredInventories.contains(event.getInventory())) {
                return;
            }
            event.setCancelled(true);
            if (!(event.getCurrentItem() instanceof Button)) {
                return;
            }
            Button button = (Button) event.getCurrentItem();
            switch (event.getClick()) {
                case LEFT:
                case SHIFT_LEFT:
                    button.onLeftClick.accept(event);
                    break;
                case SHIFT_RIGHT:
                case RIGHT:
                    button.onRightClick.accept(event);
                    break;
                case MIDDLE:
                    button.onMiddleClick.accept(event);
                    break;
                default:
                    break;
            }
        }
    };
    private static Listener ClearTask = new Listener() {
        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            registeredInventories.remove(event.getInventory());
        }
    };
    private List<Inventory> pageMap = new ArrayList<>();

    public Gui(String name, int size) {
        if (size % 3 != 0) {
            throw new IllegalArgumentException("Size must be a multiple of 3");
        }
        this.size = size;
        this.defaultName = Common.colourise(name);
    }

    public static void setupHandler(JavaPlugin plugin) {
        Objects.requireNonNull(plugin).getServer().getPluginManager().registerEvents(GuiHandler, plugin);
        Objects.requireNonNull(plugin).getServer().getPluginManager().registerEvents(ClearTask, plugin);
    }

    public static ItemStack[] fillWithItem(ItemStack itemStack, ItemStack[] original) {
        Objects.requireNonNull(itemStack);
        Objects.requireNonNull(original);
        Arrays.fill(original, itemStack);
        return original;
    }

    public void setPage(int page, ItemStack[] contents) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be greater than 0");
        }
        Inventory inventory = Bukkit.createInventory(null, size, defaultName);
        inventory.setContents(Objects.requireNonNull(contents));
        pageMap.add(page, inventory);
        registeredInventories.add(inventory);
    }

    public ItemStack[] getPageSnapshot(int page) {
        Inventory inventory = getPageAsInventory(page);
        if (inventory == null) {
            return null;
        } else {
            return inventory.getContents();
        }
    }

    public Inventory getPageInventory(int page) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be greater than 0");
        }
        return pageMap.get(page);
    }

    public void setPageName(int page, String name) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be greater than 0");
        }
        if (pageMap.size() - 1 < page) {
            pageMap.add(0, Bukkit.createInventory(null, size, Common.colourise(name)));
        }
        pageMap.set(page, Bukkit.createInventory(null, size, Common.colourise(name)));
    }

    public void setPageContents(int page, ItemStack[] contents) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be greater than 0");
        }
        if (pageMap.size() - 1 < page) {
            pageMap.add(0, Bukkit.createInventory(null, size, Common.colourise(defaultName)));
        }
        Inventory inventory = pageMap.get(page);
        inventory.setContents(Objects.requireNonNull(contents));
    }

    public Inventory getPageAsInventory(int page) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be greater than 0");
        }
        if (page > pageMap.size() -1) {
            return null;
        }
        return pageMap.get(page);
    }

    public static class Button extends ItemStack {

        Consumer<InventoryClickEvent> onLeftClick;
        Consumer<InventoryClickEvent> onRightClick;
        Consumer<InventoryClickEvent> onMiddleClick;

        private Button() {
        }

        public Button(String name, Material material, int amount) {
            super.setType(material);
            setName(name);
            setAmount(amount);
        }

        public void setOnAllClicks(Consumer<InventoryClickEvent> onClick) {
            setOnLeftClick(onClick);
            setOnMiddleClick(onClick);
            setOnRightClick(onClick);
        }

        public void setName(String name) {
            ItemMeta meta = super.getItemMeta();
            meta.setDisplayName(Objects.requireNonNull(Common.colourise(name)));
            super.setItemMeta(meta);
        }

        public void setLore(List<String> lore) {
            ItemMeta meta = super.getItemMeta();
            meta.setLore(Objects.requireNonNull(Common.colourise(lore)));
            super.setItemMeta(meta);
        }

        public void setOnLeftClick(Consumer<InventoryClickEvent> onClick) {
            this.onLeftClick = Objects.requireNonNull(onClick);
        }

        public void setOnRightClick(Consumer<InventoryClickEvent> onClick) {
            this.onRightClick = Objects.requireNonNull(onClick);
        }

        public void setOnMiddleClick(Consumer<InventoryClickEvent> onClick) {
            this.onRightClick = Objects.requireNonNull(onClick);
        }

    }
}
