package com.gmail.andrewandy.spawnerplugin.listener;


import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.betaobjects.*;
import com.gmail.andrewandy.spawnerplugin.data.DataUtil;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerBreakEvent;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerPlaceEvent;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerRightClickEvent;
import com.gmail.andrewandy.spawnerplugin.util.Common;
import com.gmail.andrewandy.spawnerplugin.util.Gui;
import com.gmail.andrewandy.spawnerplugin.util.HeadUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BlockInteractListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getMetadata("customSpawner").stream().anyMatch(
                (meta) -> {
                    Plugin plugin = meta.getOwningPlugin();
                    if (plugin == null) {
                        return false;
                    }
                    return plugin.getName().equalsIgnoreCase(SpawnerPlugin.getInstance().getName());
                })) {
            Material material = block.getType();
            BlockState state = block.getState();
            if (!(state instanceof CreatureSpawner)) {
                System.out.println(material);
                System.out.println("not instance!");
            }
            if (SpawnerPlugin.getSpawnerCache().isCached(Spawner.getTestIdentifier(block.getLocation()))) {
                Spawner spawner = SpawnerPlugin.getSpawnerCache().getFromCache(Spawner.getTestIdentifier(block.getLocation()));
                assert spawner != null;
                SpawnerBreakEvent breakEvent = new SpawnerBreakEvent(event.getPlayer(), spawner);
                if (breakEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerBreak(SpawnerBreakEvent event) {
        Entity breaker = event.getBreaker();
        if (!(breaker instanceof Player)) {
            return;
        }
        Player player = (Player) breaker;
        com.gmail.andrewandy.spawnerplugin.betaobjects.Spawner spawner = event.getSpawner();
        if (!spawner.getOwner().equals(player.getUniqueId()) || spawner.getTeamMembers().contains(player.getUniqueId())) {
            Common.tell(player, "&cOops! &dLooks like you don't have permission to interact with this spawner!");
            event.setCancelled(true);
            return;
        }
        ItemStack item;
        if (spawner instanceof LivingEntitySpawner) {
            LivingEntitySpawner entitySpawner = (LivingEntitySpawner) spawner;
            item = entitySpawner.getAsItem();
        } else if (spawner instanceof StackableItemSpawner) {
            item = spawner.getAsItem();
        } else {
            item = spawner.getAsItem();
        }
        spawner.getLocation().getWorld().dropItemNaturally(spawner.getLocation(), item);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        Block clicked = event.getClickedBlock();
        assert clicked != null;
        Spawner spawner = SpawnerPlugin.getSpawnerCache().getFromCache(Spawner.getTestIdentifier(clicked.getLocation()));
        if (spawner == null) {
            Optional<com.gmail.andrewandy.spawnerplugin.betaobjects.OfflineSpawner> optional = DataUtil.loadData(clicked.getLocation());
            if (!optional.isPresent()) {
                return;
            }
            Optional<Spawner> loading = optional.get().asSpawner(clicked.getLocation());
            if (!loading.isPresent()) {
                return;
            }
            Spawner clickedSpawner = loading.get();
            SpawnerPlugin.getSpawnerCache().cache(clickedSpawner);
            SpawnerRightClickEvent clickEvent = new SpawnerRightClickEvent(event.getPlayer(), clickedSpawner);
            if (clickEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        ItemStack itemStack = event.getItemInHand();
        Optional<OfflineSpawner> offlineSpawner = OfflineSpawner.asOfflineSpawner(itemStack, block.getLocation());
        if (!offlineSpawner.isPresent()) {
            System.out.println("Not present.");
            return;
        }
        Optional<Spawner> spawner = offlineSpawner.get().asSpawner(block.getLocation());
        if (!spawner.isPresent()) {
            return;
        }
        Spawner live = spawner.get();
        SpawnerPlaceEvent spawnerEvent = new SpawnerPlaceEvent(event.getPlayer(), live, event.getHand());
        spawnerEvent.callEvent();
        if (spawnerEvent.isCancelled()) {
            return;
        }
        SpawnerPlugin.getSpawnerCache().cache(live);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerPlace(SpawnerPlaceEvent event) {
        System.out.println("Called!");
        Player player = event.getPlayer();
        Spawner spawner = event.getSpawner();
        if (!spawner.getOwner().equals(player.getUniqueId()) && !spawner.getTeamMembers().contains(player.getUniqueId())) {
            event.setCancelled(true);
            Common.tell(player, "&cOops! &dLooks like you don't have permission to interact with this spawner!");
            return;
        }
        spawner.getLocation().getBlock().setMetadata("customSpawner", new FixedMetadataValue(SpawnerPlugin.getInstance(), spawner.getClass().getName()));
        player.getInventory().setItem(event.getSlot(), null);
        Common.tell(player, "&aYou have just placed a spawner.");
    }

    private Gui buildGui(Spawner spawner, Player clicker) {
        String formattedBalance = Common.asNumberPrefix(SpawnerPlugin.getEconomy().getBalance(clicker));
        Gui gui = new Gui("&eSpawner", 27);
        ItemStack[] contents = new ItemStack[27];
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        Gui.fillWithItem(filler, contents);
        Gui.Button exitButton = new Gui.Button("&c&lExit", Material.BARRIER, 1);
        exitButton.setOnAllClicks((event) -> event.getWhoClicked().closeInventory());
        contents[10] = exitButton;
        ItemStack balanceViewer = new ItemStack(Material.PAPER);
        meta = balanceViewer.getItemMeta();
        meta.setDisplayName("&eBalance");
        meta.setLore(Collections.singletonList(Common.colourise("&bCurrent Balance: ") + formattedBalance));
        balanceViewer.setItemMeta(meta);
        contents[12] = balanceViewer;
        contents[14] = balanceViewer;
        Gui.Button mainIcon = new Gui.Button(spawner.getAsItem());
        contents[13] = mainIcon;
        ItemStack stack16;
        if (spawner instanceof LivingEntitySpawner) {
            LivingEntitySpawner entitySpawner = (LivingEntitySpawner) spawner;
            EntityType spawnedType = entitySpawner.getSpawnedType();
            if (spawnedType.getEntityClass() == null) {
                throw new UnsupportedOperationException("SpawnedType class is null! " + spawnedType.name());
            }
            Optional<String> customTexture = HeadUtil.getEntityTexture(spawnedType);
            if (!customTexture.isPresent()) {
                if (Mob.class.isAssignableFrom(spawnedType.getEntityClass())) {
                    customTexture = HeadUtil.getEntityTexture(EntityType.WITHER_SKELETON);
                } else if (Animals.class.isAssignableFrom(spawnedType.getEntityClass())) {
                    customTexture = HeadUtil.getEntityTexture(EntityType.CHICKEN);
                } else {
                    throw new UnsupportedOperationException("Unable to find suitable replacement for entityType");
                }
                assert customTexture.isPresent();
                String texture = customTexture.get();
                ItemStack skull = new ItemStack(Material.SKELETON_SKULL);
                ItemMeta itemMeta = skull.getItemMeta();
                SkullMeta skullMeta = (SkullMeta) itemMeta;
                PlayerProfile profile = Bukkit.createProfile(null, null);
                profile.setProperty(new ProfileProperty("texture", texture));
                skullMeta.setPlayerProfile(profile);
                stack16 = skull;
            } else {
                String texture = customTexture.get();
                ItemStack skull = new ItemStack(Material.SKELETON_SKULL);
                ItemMeta itemMeta = skull.getItemMeta();
                SkullMeta skullMeta = (SkullMeta) itemMeta;
                PlayerProfile profile = Bukkit.createProfile(null, null);
                profile.setProperty(new ProfileProperty("texture", texture));
                skullMeta.setPlayerProfile(profile);
                skull.setItemMeta(skullMeta);
                stack16 = skull;
            }
        } else if (spawner instanceof ItemStackSpawner) {
            ItemStackSpawner stackSpawner = (ItemStackSpawner) spawner;
            stack16 = stackSpawner.getBase().getItem();
        } else {
            stack16 = exitButton;
        }
        contents[16] = stack16;
        Gui.Page page = new Gui.Page(contents);
        gui.setPage(0, page);
        page.update(gui);
        return gui;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block clicked = event.getClickedBlock();
        assert clicked != null;
        List<MetadataValue> meta = clicked.getMetadata("customSpawner");
        if (meta.isEmpty()) {
            return;
        }
        MetadataValue value = meta.get(0);
        String raw = value.asString();
        Spawner target;
        SpawnerRightClickEvent clickEvent = new SpawnerRightClickEvent(event.getPlayer(), null);
    }

}
