package com.gmail.andrewandy.spawnerplugin.betaobjects;

import com.gmail.andrewandy.corelib.util.gui.DropGUI;
import com.gmail.andrewandy.corelib.util.gui.Gui;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.data.SpawnerCache;
import com.gmail.andrewandy.spawnerplugin.util.Common;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class LivingEntitySpawner extends Spawner implements StackableSpawner {

    private final EntityType spawnedType;
    private final int maxSize;
    private final List<OfflineSpawner> stacked;

    public LivingEntitySpawner(ItemStack itemStack, Location location) throws IllegalAccessException {
        super(itemStack, location);
        Optional<LivingEntitySpawner> spawner = getFromItem(itemStack, location);
        if (!spawner.isPresent()) {
            throw new IllegalAccessException();
        }
        LivingEntitySpawner cloned = spawner.get();
        this.maxSize = cloned.maxSize;
        this.spawnedType = cloned.spawnedType;
        this.stacked = cloned.stacked;
    }

    @Override
    public Gui getGui() {
        final String displayName = "&b" + Common.capitalise(spawnedType.name().toLowerCase()) + " Spawner";
        Gui gui = new Gui(displayName);
        gui.setBlocksOnClose(false);
        Gui.Page page = new Gui.Page(27);
        Gui.ButtonBuilder buttonBuilder = new Gui.ButtonBuilder();
        Gui.Button exit = buttonBuilder.setDisplayName("&c&lExit")
                .setAmount(1)
                .setOnAllClicks((event) -> event.getWhoClicked().closeInventory())
                .buildAndClear();
        page.addButton(exit, 10);
        String ownerName = Bukkit.getServer().getOfflinePlayer(getOwner()).getName();
        Gui.Button mainIcon = buttonBuilder.setDisplayName(displayName)
                .setAmount(1)
                .setLore(Arrays.asList(
                        "&b&lInformation:",
                        "&aOwner: " + (ownerName == null ? "&cPlayer Not Found" : ownerName),
                        "&eSpawned Type: " + Common.capitalise(spawnedType.name().toLowerCase()),
                        "&bCurrent Stack: " + getSize(),
                        "&cMax Stack Size: " + maxSize))
                .buildAndClear();
        page.addButton(mainIcon, 13);
        final DropGUI stackGUI = new DropGUI("&d&lSAdding Stacks", 27);
        stackGUI.setComparator(DropGUI.SIMILAR_ITEM_STACK_COMPARATOR(getAsItem()));
        stackGUI.setRunOnClose((human) -> {
            Optional<ItemStack[]> optional = stackGUI.getDroppedItems();
            if (!optional.isPresent()) {
                Common.tell(human, "&b&l(!) &bNo spawners were added to the stack.");
                return;
            }
            ItemStack[] dropped = optional.get();
            int totalAdded;
            for (int i = 0; i < maxSize; i++) {
                Optional<OfflineSpawner> offlineSpawner = OfflineSpawner.fromItemStack(dropped[i]);
                if (!offlineSpawner.isPresent()) {
                    throw new IllegalArgumentException("Invalid Spawner!");
                }
                this.add(offlineSpawner.get());
            }
            if (getSize() + dropped.length > getMaxSize()) {
                totalAdded = getMaxSize() - getSize();
                int overflow = dropped.length - totalAdded;
                for (int i = maxSize; i < dropped.length; i++) {
                    human.getInventory().addItem(dropped[i]);
                    Common.tell(human, "&c&l(!)&c&l " + overflow + "&c Spawners were not added to the stack and were returned to your inventory.");
                }
            } else {
                totalAdded = dropped.length;
            }
            Common.tell(human, "&aYou have successfully added &n" + totalAdded + "&a spawners to this stack!");
        });
        return gui;
    }

    public LivingEntitySpawner(EntityType entityType, int delay, int maxSize, Location location) {
        super(delay, location);
        this.spawnedType = Objects.requireNonNull(entityType);
        this.maxSize = maxSize;
        this.stacked = new ArrayList<>(maxSize);
        Objects.requireNonNull(Objects.requireNonNull(location).getWorld());
    }

    @Override
    public int getSize() {
        return stacked.size();
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public void add(OfflineSpawner spawner) {
        if (isFull()) {
            return;
        }
        stacked.add(spawner);
    }

    @Override
    public void addAll(Collection<OfflineSpawner> spawners) {
        if (spawners.size() + getSize() > maxSize) {
            throw new IllegalArgumentException("Unable to exceed max stack size!");
        }
        stacked.addAll(spawners);
    }

    @Override
    public void withdraw(OfflineSpawner spawner) {
        stacked.remove(spawner);
    }

    @Override
    public void clear() {
        stacked.clear();
    }

    @Override
    public boolean canStack(OfflineSpawner spawner) {
        boolean target = Objects.requireNonNull(spawner).getOriginalClass().isAssignableFrom(LivingEntitySpawner.class);
        return target && spawner.getOwner().equals(super.getOwner()) || super.getTeamMembers().contains(spawner.getOwner());
    }

    @Override
    public List<OfflineSpawner> getStacked() {
        return new ArrayList<>(stacked);
    }


    public EntityType getSpawnedType() {
        return spawnedType;
    }

    @Override
    public Spawner clone() {
        return new LivingEntitySpawner(spawnedType, getDelay(), maxSize, getLocation());
    }

    public ItemStack getAsItem() {
        ItemStack item = new ItemStack(Material.SPAWNER);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(Common.colourise("&e" + Common.capitalise(spawnedType.name().toLowerCase()) + "&e Spawner"));
        itemMeta.setLore(Arrays.asList(
                Common.colourise(""),
                Common.colourise("&b&lInformation:"),
                Common.colourise("  &7-&a Mob: " + Common.capitalise(spawnedType.name().toLowerCase())),
                Common.colourise("  &7-&e Size: " + getSize()),
                Common.colourise("  &7-&c Max Size: " + maxSize)
        ));
        item.setItemMeta(itemMeta);
        return getAsItem(item);
    }

    public ItemStack getAsItem(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack.clone());
        nbtItem.setString("owner", super.getOwner().toString());
        nbtItem.setString("spawner", "mob");
        nbtItem.setString("entityType", spawnedType.name());
        nbtItem.setInteger("delay", super.getDelay());
        nbtItem.setObject("stacked", stacked);
        nbtItem.setObject("teamMembers", super.getTeamMembers());
        nbtItem.setInteger("maxSize", maxSize);
        return nbtItem.getItem();
    }

    @Override
    protected Optional<LivingEntitySpawner> getFromItem(ItemStack item, Location location) {
        Optional<LivingEntitySpawner> optional = Optional.empty();
        if (!instanceOfSpawner(item)) {
            return optional;
        }
        NBTItem nbtItem = new NBTItem(item.clone());
        try {
            int delay = nbtItem.getInteger("delay");
            //Should be fine.
            @SuppressWarnings("Unchecked")
            List<OfflineSpawner> stacked = (List<OfflineSpawner>) nbtItem.getObject("stacked", List.class);
            List<UUID> teamMembers = (List<UUID>) nbtItem.getObject("teamMembers", List.class);
            int maxSize = nbtItem.getInteger("maxSize");
            EntityType entityType = EntityType.valueOf(nbtItem.getString("entityType"));
            UUID owner = UUID.fromString(nbtItem.getString("owner"));
            LivingEntitySpawner spawner = new LivingEntitySpawner(entityType, delay, maxSize, location);
            spawner.setOwner(owner);
            spawner.addAll(stacked);
            teamMembers.forEach(spawner::addTeamMember);
            optional = Optional.of(spawner);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ignored) {
        }
        return optional;
    }

    @Override
    BlockDataMeta getAsMeta(BlockDataMeta meta) {
        //TODO
        return null;
    }


    @Override
    public boolean instanceOfSpawner(ItemStack itemStack) {
        NBTItem item = new NBTItem(itemStack);
        if (!item.hasNBTData()) {
            return false;
        }
        String type = item.getString("spawner");
        return type != null && type.equalsIgnoreCase("mob");
    }

}
