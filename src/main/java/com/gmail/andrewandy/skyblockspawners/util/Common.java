package com.gmail.andrewandy.skyblockspawners.util;

import com.gmail.andrewandy.skyblockspawners.SkyblockSpawnerBukkit;
import com.gmail.andrewandy.skyblockspawners.SkyblockSpawners;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.logging.Level;

public class Common {
    /**
     * Tell a player a message.
     *
     * @param toWhom   toWhom you want to send the message
     * @param messages the messages you want to send.
     * @since 1.0
     */

    public static void tell(CommandSender toWhom, String... messages) {
        for (final String message : messages)
            toWhom.sendMessage(colourise(message));
    }

    /**
     * Colourise a message by using chatCode "&".
     *
     * @param message the message you want to colourise.
     * @return colourised message
     * @since 1.0
     */

    public static String colourise(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }


    public static void log(Level level, String messages) {
        SkyblockSpawnerBukkit.getInstance().getLogger().log(level, colourise(SkyblockSpawners.prefix + ": " + messages));
    }

    /**
     * Capitalise the first expression of a string
     *
     * @param original message you want to capitalise.
     * @return the capitalised version
     * @since 1.0-SNAPSHOT
     */

    public static String capitalise(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    /**
     * Get a random number from a set range.
     *
     * @param min minimum number.
     * @param max maximum number
     * @return returns a random number from the given range.
     * @since 1.0
     */

    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    /**
     * Get a random <code>EntityType</code> from a set list.
     *
     * @param types the types to choose from
     * @return a random EntityType from the given types.
     * @since 1.0
     */

    public static EntityType getRandomEntityType(List<EntityType> types) {
        int index = getRandomNumberInRange(0, types.size());
        return types.get(index);
    }

    /**
     * Get all the available entity types from the bukkit <code>EntityType</code> enum.
     *
     * @return all <code>EntityType</code> Enum values
     * @since 1.0
     */

    public static List<EntityType> getEntityTypes() {
        return Arrays.asList(EntityType.values());
    }

    /**
     * Get an EntityType from a string
     *
     * @param type the EntityType in string form.
     * @return an Enum value of the string. Returns <code>null</code> if an invalid <code>EntityType</code> is specified.
     * @since 1.0
     */

    public static EntityType getEntityTypeFromString(String type) {
        for (EntityType entityType : EntityType.values()
        ) {
            if (type.equals(entityType.name())) {
                return entityType;
            }
        }
        return null;
    }

    /**
     * Get a list of EntityTypes from a Collection.
     *
     * @param type The Collection of <code>EntityType</code> in string form.
     * @return a List of EntityTypes.
     * @since 1.0
     */

    public static List<EntityType> getEntityTypeFromString(Collection<String> type) {
        List<EntityType> entityTypeList = new ArrayList<>();
        for (EntityType entityType : EntityType.values()
        ) {
            for (String stringTypes : type
            ) {
                if (!entityType.name().equalsIgnoreCase(stringTypes)) {
                    continue;
                }
                entityTypeList.add(entityType);
            }
        }
        return entityTypeList;
    }

}
