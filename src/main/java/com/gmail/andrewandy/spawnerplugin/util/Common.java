package com.gmail.andrewandy.spawnerplugin.util;

import com.gmail.andrewandy.spawneraddon.SpawnerAddon;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
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
     * Colourise a message by using chat Code "&".
     *
     * @param message the message you want to colourise.
     * @return colourised message
     * @since 1.0
     */

    public static String colourise(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> colourise(List<String> original) {
        List<String> target = new LinkedList<>();
        for (int i = 0; i < original.size(); i++) {
            target.add(i, colourise(original.get(i)));
        }
        return target;
    }


    public static void log(Level level, String messages) {
        SpawnerPlugin.getInstance().getLogger().log(level, colourise(SpawnerAddon.prefix + ": " + messages));
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
        return ThreadLocalRandom.current().nextInt((max - min) + 1) + min;
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

    /**
     * Converts a Runnable to a BukkitRunnable.
     *
     * @param runnable The runnable to convert.
     * @return Returns a new BukkitRunnable with the runnable as the run() method.
     * @since 1.0
     */
    public static BukkitRunnable asBukkitRunnable(final Runnable runnable) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                Objects.requireNonNull(runnable).run();
            }
        };
    }

    /**
     * Get the time in ticks from a particular timeUnit.
     *
     * @param timeUnit The timeUnit to convert from.
     * @param duration The duration of time.
     * @return Returns the modulus division of time in ticks.
     * @since 1.0
     */
    public static long toTicks(TimeUnit timeUnit, int duration) {
        //Since its 20 ticks per second, 1 tick = 0.05s or 50ms
        return Objects.requireNonNull(timeUnit).toMillis(duration) % 50;
    }

    public static String asNumberPrefix(double number) {
        String[] split = (Double.toString(number)).split(".");
        if (split.length == 0) {
            split = new String[]{Integer.toString((int) Math.floor(number)), Integer.toString(0)};
        }
        assert split.length == 2;
        final String nonDeci = split[0];
        final String prefix;
        final int rounds = Integer.parseInt(nonDeci) / 100;
        final int digits;
        if (split[1].length() > 3) {
            split[1] = split[1].substring(0, 3);
        }
        final int decimals = Integer.parseInt(split[1]);
        final int remainder = Integer.parseInt(nonDeci) % 100;
        if (nonDeci.length() < 3) {
            digits = Integer.parseInt(nonDeci);
        } else {
            digits = rounds * 100 + remainder;
        }
        switch (rounds) {
            case 1:
                prefix = "K";
                break;
            case 2:
                prefix = "M";
                break;
            case 3:
                prefix = "B";
                break;
            case 4:
                prefix = "T";
                break;
            case 5:
                prefix = "Q";
                break;
            case 6:
                prefix = "P";
                break;
            default:
                prefix = "";
                break;
        }
        return "" + digits + prefix + "." + decimals;
    }

}
