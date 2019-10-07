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

    private String name;
    private List<Page> pages = new ArrayList<>();
    private int size;
    private Map<Page, Inventory> inventoryMap = new HashMap<>();
    private static Set<Gui> registered = new HashSet<>();
    ;
    private static Listener listener = new Listener() {

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            boolean valid = false;
            Gui target = null;
            for (Gui gui : registered) {
                if (!gui.getInventoryMap().containsValue(event.getClickedInventory())) {
                    continue;
                }
                for (Map.Entry entry : gui.getInventoryMap().entrySet()) {
                    Inventory inv = (Inventory) entry.getValue();
                    if (inv != event.getInventory()) {
                        continue;
                    }
                    target = gui;
                    event.setCancelled(true);
                    valid = true;
                }
            }
            if (!valid) {
                return;
            }
            Page page = target.getPageByInventory(event.getClickedInventory());
            if (page == null) {
                return;
            }
            Button button = page.getButton(event.getSlot());
            if (button == null) {
                return;
            }
            switch (event.getClick()) {
                case SHIFT_RIGHT:
                case RIGHT:
                    if (button.onRightClick != null) {
                        button.onRightClick.accept(event);
                    }
                    break;
                case MIDDLE:
                    if (button.onMiddleClick != null) {
                        button.onMiddleClick.accept(event);
                    }
                    break;
                case LEFT:
                case SHIFT_LEFT:
                    if (button.onLeftClick != null) {
                        button.onLeftClick.accept(event);
                    }
                    break;
                default:
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            for (Gui gui : registered) {
                if (!gui.getInventoryMap().containsValue(event.getInventory())) {
                    continue;
                }
                Iterator<Map.Entry<Page, Inventory
                        >> iterator = gui.getInventoryMap().entrySet().iterator();
                while (iterator.hasNext()) {
                    Inventory inv = iterator.next().getValue();
                    if (inv != event.getInventory()) {
                        continue;
                    }
                    iterator.remove();
                    break;
                }
            }
        }
    };

    public static void setupHandler(JavaPlugin plugin) {
        Objects.requireNonNull(plugin).getServer().getPluginManager().registerEvents(listener, plugin);
    }

    private Map<Page, Inventory> getInventoryMap() {
        return inventoryMap;
    }

    public Gui(String name, int size) {
        this.name = Common.colourise(name);
        if (size % 3 != 0) {
            throw new IllegalArgumentException("Invalid size!");
        }
        this.size = size;
        registered.add(this);
    }

    public static void fillWithItem(ItemStack filler, ItemStack[] original) {
        Arrays.fill(original, filler);
    }

    public int getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public List<Page> getPages() {
        return pages;
    }

    public Page getPageByInventory(Inventory inventory) {
        for (Page p : pages) {
            if (p.inventory == inventory) {
                return p;
            }
        }
        return null;
    }

    public void insertPage(int pageNumber, Page page) {
        pages.add(pageNumber, page);
    }

    public void setPage(int pageNumber, Page page) {
        if (pages.size() - 1 < pageNumber) {
            pages.add(pageNumber, page);
            return;
        }
        if (page == null) {
            pages.remove(pageNumber);
            return;
        }
        this.pages.set(pageNumber, page);
    }

    public Page getPage(int pageNumber) {
        return pages.get(pageNumber);
    }

    public static class Page {
        private ItemStack[] contents;
        private Map<Integer, Button> buttonMap = new HashMap<>();
        private String name;
        private Inventory inventory;

        public Page(ItemStack[] contents) {
            this.contents = Objects.requireNonNull(contents);
        }

        public void setName(String name) {
            this.name = Common.colourise(name);
        }

        public String getName() {
            return name;
        }

        public Page(ItemStack[] contents, Map<Integer, Button> buttons) {
            this.contents = Objects.requireNonNull(contents);
            this.buttonMap = Objects.requireNonNull(buttons);
        }

        public ItemStack[] getContents() {
            return contents;
        }

        public Map<Integer, Button> getButtonMap() {
            return buttonMap;
        }

        public Button getButton(int slot) {
            return buttonMap.get(slot);
        }

        public void update(Gui gui) {
            Objects.requireNonNull(gui);
            if (name == null) {
                name = gui.name;
            }
            Inventory target = Bukkit.createInventory(null, gui.size, getName());
            target.setContents(contents);
            if (inventory != null) {
                gui.inventoryMap.replace(this, target);
            }
            gui.inventoryMap.put(this, target);
            this.inventory = target;
        }

        public Inventory getInventory(Gui gui) {
            if (inventory == null) {
                update(gui);
            }
            return inventory;
        }
    }

    public static class Button extends ItemStack {

        private Button() {
        }

        Consumer<InventoryClickEvent> onLeftClick;
        Consumer<InventoryClickEvent> onRightClick;
        Consumer<InventoryClickEvent> onMiddleClick;

        public Button(String displayName, Material material, int amount) {
            super(material);
            setName(displayName);
            setAmount(amount);
        }

        public void setAmount(int amount) {
            super.setAmount(amount);
        }

        public void setLore(List<String> lore) {
            ItemMeta meta = super.getItemMeta();
            meta.setLore(lore);
            super.setItemMeta(meta);
        }

        public void setName(String name) {
            ItemMeta meta = super.getItemMeta();
            meta.setDisplayName(Common.colourise(name));
            super.setItemMeta(meta);
        }

        public void setOnLeftClick(Consumer<InventoryClickEvent> onLeftClick) {
            this.onLeftClick = onLeftClick;
        }

        public void setOnRightClick(Consumer<InventoryClickEvent> onRightClick) {
            this.onRightClick = onRightClick;
        }

        public void setOnMiddleClick(Consumer<InventoryClickEvent> onMiddleClick) {
            this.onMiddleClick = onMiddleClick;
        }

        public void setOnAllClicks(Consumer<InventoryClickEvent> onClick) {
            setOnRightClick(onClick);
            setOnMiddleClick(onClick);
            setOnLeftClick(onClick);
        }
    }

}
