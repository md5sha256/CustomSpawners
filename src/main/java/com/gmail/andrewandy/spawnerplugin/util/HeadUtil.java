package com.gmail.andrewandy.spawnerplugin.util;

import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class HeadUtil {

    private static Map<EntityType, String> textureMap = new HashMap<>();
    private static Map<Material, String> blockTextures = new HashMap<>();

    public static void loadData() {
        for (EntityType entityType : EntityType.values()) {
            if (entityType.getEntityClass() == null) {
                continue;
            }
            String name = entityType.getEntityClass().getSimpleName().toUpperCase();
            switch (name) {
                case "IRONGOLEM":
                    name = "GOLEM";
                    break;
                case "MAGAMACUBE":
                    name = "LAVASLIME";
                    break;
                case "WITHERSKELETON":
                    name = "WSKELETON";
                    break;
                case "TNTPRIMED":
                case "TNT":
                    name = "TNT2";
                    break;

            }
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer("MHF_" + name.toUpperCase());
            UUID uuid = offlinePlayer.getUniqueId();
            String url = "https://mc-heads.net/user/" + uuid;
            Runnable runnable = () -> {
                try {
                    new URL(url).openConnection();
                } catch (IOException ignored) {
                    return;
                }
                String download = "https://mc-heads.net/download" + uuid;
                byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", download).getBytes());
                synchronized (textureMap) {
                    textureMap.put(entityType, new String(encodedData));
                }
            };
            Bukkit.getServer().getScheduler().runTaskAsynchronously(SpawnerPlugin.getInstance(), runnable);
        }
        //Setup blocks
        for (Material material : Material.values()) {
            if (!material.isBlock()) {
                continue;
            }
            String url = "https://mcsearch.com/user/MHF_" + material.name();
            Runnable runnable = () -> {
                try {
                    new URL(url).openConnection();
                } catch (IOException ignored) {
                    return;
                }
                byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
                synchronized (blockTextures) {
                    blockTextures.put(material, new String(encodedData));
                }
            };
            Bukkit.getServer().getScheduler().runTaskAsynchronously(SpawnerPlugin.getInstance(), runnable);
        }
    }

    public static Optional<String> getBlockTexture(Material material) {
        if (textureMap.isEmpty() || blockTextures.isEmpty()) {
            loadData();
            return Optional.empty();
        }
        String str = blockTextures.get(material);
        if (str == null) {
            return Optional.empty();
        }
        return Optional.of(str);
    }

    public static Optional<String> getEntityTexture(EntityType entityType) {
        if (textureMap.isEmpty() || blockTextures.isEmpty()) {
            loadData();
            return Optional.empty();
        }
        String str = textureMap.get(entityType);
        if (str == null) {
            return Optional.empty();
        }
        return Optional.of(str);
    }
}
