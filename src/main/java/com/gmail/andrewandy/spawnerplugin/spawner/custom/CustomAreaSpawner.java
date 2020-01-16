package com.gmail.andrewandy.spawnerplugin.spawner.custom;

import com.gmail.andrewandy.corelib.api.menu.Menu;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.spawner.AbstractSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.OfflineSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.StackableSpawner;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class CustomAreaSpawner<T extends AbstractCustomizableSpawner> extends AbstractSpawner implements StackableSpawner<CustomAreaSpawner<T>> {

    private final Function<Block, Block[]> spawnerFunction;
    private final int maxSize;
    private final T spawner;
    private Collection<OfflineSpawner<CustomAreaSpawner<T>>> stacked = new HashSet<>();

    /**
     * Creates a custom area spawner backed by a CustomisableSpawner.
     *
     * @param spawner               The spawner to spawn.
     * @param spawnLocationFunction The function used to determine which blocks should be targeted to spawn.
     */
    public CustomAreaSpawner(T spawner, Function<Block, Block[]> spawnLocationFunction, int maxSize) {
        super(spawner.getLocation(), spawner.getBlockMaterial(), spawner.getOwner(), spawner.getDelay(), spawner.getSpawnChance());
        this.spawner = cloneSpawner(spawner);
        super.peers = new HashSet<>(spawner.getPeers());
        this.spawnerFunction = Objects.requireNonNull(spawnLocationFunction);
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        if (maxSize > 1 && !(spawner instanceof StackableSpawner)) {
            throw new IllegalArgumentException("Base spawner is not stackable.");
        }
        if (spawner instanceof StackableSpawner) {
            StackableSpawner stackableSpawner = (StackableSpawner) spawner;
            if (maxSize != stackableSpawner.maxSize()) {
                throw new IllegalArgumentException("Spawner MaxSize and current size different!");
            }
        }
        this.maxSize = maxSize;
    }

    public static ItemWrapper<? extends CustomAreaSpawner<? extends AbstractSpawner>> getWrapper() {
        throw new UnsupportedOperationException("Use the specific wrapper.");
    }

    public static <U extends AbstractCustomizableSpawner> ItemWrapper<CustomAreaSpawner<U>> getSpecificWrapper(Class<U> targetClass) {
        return new WrapperImpl<>(targetClass);
    }

    @Override
    public void tick() {
        super.tick();
        for (Block block : spawnerFunction.apply(getLocation().getBlock())) {
            spawner.spawnTick(block);
        }
    }

    /**
     * @return Returns a cloned version of the underlying spawner.
     */
    public T getWrappedSpawner() {
        return cloneSpawner(spawner);
    }

    public Collection<? extends OfflineSpawner<?>> getUnderlyingStack() {
        if (!(spawner instanceof StackableSpawner<?>)) {
            throw new IllegalArgumentException("Underlying spawner is not stackable.");
        }
        return ((StackableSpawner<?>) spawner).getStacked();
    }

    public <U extends StackableSpawner<U>> void addToUnderlyingStack(OfflineSpawner<U> spawner) {
        Objects.requireNonNull(spawner);
        if (!(spawner.getOriginalClass().isAssignableFrom(StackableSpawner.class))) {
            throw new IllegalArgumentException("Underlying spawner is not stackable.");
        }
        if (!spawner.getOriginalClass().isAssignableFrom(this.spawner.getClass())) {
            throw new IllegalArgumentException("Spawner class and target class incompatible.");
        }
        @SuppressWarnings("unchecked") //The original class of the spawner has been checked to be the current T type.
                StackableSpawner<U> stackableSpawner = (StackableSpawner<U>) this.spawner;
        stackableSpawner.stack(spawner);
    }

    public <U extends StackableSpawner<U>> void removeFromUnderlyingStack(OfflineSpawner<U> spawner) {
        Objects.requireNonNull(spawner);
        if (!(spawner.getOriginalClass().isAssignableFrom(StackableSpawner.class))) {
            throw new IllegalArgumentException("Underlying spawner is not stackable.");
        }
        if (!spawner.getOriginalClass().isAssignableFrom(this.spawner.getClass())) {
            throw new IllegalArgumentException("Spawner class and target class incompatible.");
        }
        @SuppressWarnings("unchecked") //The original class of the spawner has been checked to be the current T type.
                StackableSpawner<U> stackableSpawner = (StackableSpawner<U>) this.spawner;
        stackableSpawner.remove(spawner);
    }

    public <U extends StackableSpawner<U>> boolean canStackUnderlying(Collection<OfflineSpawner<U>> spawners) {
        Objects.requireNonNull(spawner);
        if (spawners.isEmpty()) {
            return true;
        }
        Optional<OfflineSpawner<U>> optional = spawners.stream().filter(Objects::nonNull).findFirst();
        if (!optional.isPresent()) {
            return true;
        }
        OfflineSpawner<U> spawner = optional.get();
        if (!(spawner.getOriginalClass().isAssignableFrom(StackableSpawner.class))) {
            throw new IllegalArgumentException("Underlying spawner is not stackable.");
        }
        if (!spawner.getOriginalClass().isAssignableFrom(this.spawner.getClass())) {
            throw new IllegalArgumentException("Spawner class and target class incompatible.");
        }
        @SuppressWarnings("unchecked") //The original class of the spawner has been checked to be the current T type.
                StackableSpawner<U> stackableSpawner = (StackableSpawner<U>) this.spawner;
        return stackableSpawner.canStack(spawners);
    }

    public <U extends StackableSpawner<U>> void removeIfUnderlying(Predicate<OfflineSpawner<U>> predicate) {
        if (!(spawner.getClass()).isAssignableFrom(StackableSpawner.class)) {
            throw new IllegalArgumentException("Underlying spawner is not stackable.");
        }
        @SuppressWarnings("unchecked") //The original class of the spawner has been checked to be the current T type.
                StackableSpawner<U> stackableSpawner = (StackableSpawner<U>) this.spawner;
        stackableSpawner.removeIf(predicate);
    }

    public void clearUnderlyingStack() {
        if (!(spawner.getClass()).isAssignableFrom(StackableSpawner.class)) {
            throw new IllegalArgumentException("Underlying spawner is not stackable.");
        }
        StackableSpawner stackableSpawner = (StackableSpawner) this.spawner;
        stackableSpawner.clear();
    }

    private T cloneSpawner(T original) {
        @SuppressWarnings("unchecked") //This is ok since all classes SHOULD have this implemented.
                ItemWrapper<T> wrapper = (ItemWrapper<T>) T.getWrapper();
        ItemStack stack = wrapper.toItem(original);
        Optional<OfflineSpawner<T>> optional = wrapper.fromItem(stack);
        if (!optional.isPresent()) {
            throw new IllegalStateException("Unknown error occured when cloning...");
        }
        Optional<T> target = wrapper.toLiveAtLocation(optional.get(), getLocation());
        if (!target.isPresent()) {
            throw new IllegalStateException("Unknown error occured when cloning...");
        }
        return target.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public MetadataValue getAsMetadata() {
        ItemWrapper<CustomAreaSpawner<T>> wrapper = getSpecificWrapper((Class<T>) spawner.getClass());
        return new FixedMetadataValue(SpawnerPlugin.getInstance(), wrapper.toItem(this));
    }

    @Override
    public void initialize() {

    }

    @Override
    public Optional<Menu> getDisplayUI() {
        return Optional.empty();
    }

    @Override
    public Collection<OfflineSpawner<CustomAreaSpawner<T>>> getStacked() {
        return Collections.unmodifiableCollection(stacked);
    }

    @Override
    public void stack(OfflineSpawner<CustomAreaSpawner<T>> spawner) {
        if (!isFull()) {
            stacked.add(Objects.requireNonNull(spawner));
        }
    }

    @Override
    public void remove(OfflineSpawner<CustomAreaSpawner<T>> spawner) {
        stacked.remove(Objects.requireNonNull(spawner));
    }

    @Override
    public void removeIf(Predicate<OfflineSpawner<CustomAreaSpawner<T>>> predicate) {
        stacked.removeIf(Objects.requireNonNull(predicate));
    }

    @Override
    public void clear() {
        stacked.clear();
    }

    @Override
    public boolean stackAll(Collection<OfflineSpawner<CustomAreaSpawner<T>>> offlineSpawners) {
        if (canStack(offlineSpawners)) {
            return stacked.addAll(Objects.requireNonNull(offlineSpawners));
        } else {
            return false;
        }
    }

    @Override
    public int maxSize() {
        return maxSize;
    }

    public static class WrapperImpl<T extends AbstractCustomizableSpawner> extends ItemWrapper<CustomAreaSpawner<T>> {

        private Class<T> clazz;

        public WrapperImpl(Class<T> targetClass) {
            clazz = Objects.requireNonNull(targetClass);
        }

        @Override
        public ItemStack toItem(CustomAreaSpawner<T> spawner) {
            if (!tryCast().isPresent()) {
                throw new IllegalStateException("Unable to cast wrapper.");
            }
            ItemWrapper<T> wrapper = tryCast().get();
            ItemStack itemStack = wrapper.toItem(spawner.getWrappedSpawner());
            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString("wrapperClass", CustomAreaSpawner.class.getName());
            nbtItem.setInteger("wrapperMaxSize", spawner.maxSize());
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream(Integer.MAX_VALUE);
                ObjectOutputStream stream = new ObjectOutputStream(bytes);
                //Cast to ensure it will be serialised properly.
                stream.writeObject((Function<Block, Block[]> & Serializable) spawner.spawnerFunction);
                stream.close();
                nbtItem.setByteArray("spawnerFunction", bytes.toByteArray());
                bytes.close();
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to write function data!", ex);
            }
            return nbtItem.getItem();
        }

        @SuppressWarnings("unchecked")
        private Optional<ItemWrapper<T>> tryCast() {
            ItemWrapper<T> wrapper;
            try {
                wrapper = (ItemWrapper<T>) T.getWrapper();
            } catch (ClassCastException | UnsupportedOperationException ex) {
                return Optional.empty();
            }
            return Optional.of(wrapper);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Optional<OfflineSpawner<CustomAreaSpawner<T>>> fromItem(ItemStack itemStack) throws IllegalArgumentException {
            NBTItem nbtItem = new NBTItem(itemStack.clone());
            String rawWrapperClass = nbtItem.getString("wrapperClass");
            String rawClass = nbtItem.getString("class");
            //Check the wrapped size exists.
            int wrappedMaxSize = nbtItem.getInteger("wrappedMaxSize");
            if (rawWrapperClass == null || rawClass == null) {
                return Optional.empty();
            }
            try {
                Class<?> wrapperClazz = Class.forName(rawWrapperClass);
                Class<?> clazz = Class.forName(rawClass);
                if (!wrapperClazz.isAssignableFrom(CustomAreaSpawner.class)) {
                    return Optional.empty();
                }
                if (!clazz.isAssignableFrom(this.clazz)) {
                    return Optional.empty();
                }
            } catch (ClassNotFoundException ex) {
                return Optional.empty();
            }
            byte[] bytes = nbtItem.getByteArray("spawnedFunction");
            if (bytes == null) {
                return Optional.empty();
            }
            //Check if the function is valid.
            Function<Block, Block[]> function;
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                function = (Function<Block, Block[]>) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                throw new IllegalStateException("Unable to deserialise spawner!", ex);
            }
            OfflineSpawner<CustomAreaSpawner<T>> offlineSpawner = new OfflineSpawner<>((Class<CustomAreaSpawner<T>>) (Class<?>) CustomAreaSpawner.class, itemStack);
            return Optional.of(offlineSpawner);
        }

        /**
         * @param location This param is ignored, since the location has been determined by the spawner param.
         *                 Passing null here is fine.
         */
        @Override
        @SuppressWarnings("Unchecked")
        public Optional<CustomAreaSpawner<T>> toLiveAtLocation(OfflineSpawner<CustomAreaSpawner<T>> spawner, Location location) {
            if (!tryCast().isPresent()) {
                throw new IllegalStateException("Unable to cast wrapper.");
            }
            NBTItem nbtItem = new NBTItem(spawner.getItemStack());
            String rawWrapperClass = nbtItem.getString("wrapperClass");
            String rawClass = nbtItem.getString("class");
            int wrappedMaxSize = nbtItem.getInteger("wrappedMaxSize");
            if (rawWrapperClass == null || rawClass == null) {
                return Optional.empty();
            }
            try {
                Class<?> wrapperClazz = Class.forName(rawWrapperClass);
                Class<?> clazz = Class.forName(rawClass);
                if (!wrapperClazz.isAssignableFrom(CustomAreaSpawner.class)) {
                    return Optional.empty();
                }
                if (!clazz.isAssignableFrom(this.clazz)) {
                    return Optional.empty();
                }
            } catch (ClassNotFoundException ex) {
                return Optional.empty();
            }
            byte[] bytes = nbtItem.getByteArray("spawnedFunction");
            if (bytes == null) {
                return Optional.empty();
            }
            Function<Block, Block[]> function;
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                function = (Function<Block, Block[]>) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                throw new IllegalStateException("Unable to deserialise spawner!", ex);
            }
            if (function == null) {
                return Optional.empty();
            }
            ItemWrapper<T> wrapper = tryCast().get();
            Optional<OfflineSpawner<T>> optionalOfflineSpawner = wrapper.fromItem(spawner.getItemStack());
            if (!optionalOfflineSpawner.isPresent()) {
                return Optional.empty();
            }
            Optional<T> optional = wrapper.toLiveAtLocation(optionalOfflineSpawner.get(), location);
            return optional.map(wrappedSpawner -> new CustomAreaSpawner<>(wrappedSpawner, function, wrappedMaxSize));
        }

        @Override
        public boolean isSpawner(ItemStack itemStack) {
            return fromItem(itemStack).isPresent();
        }
    }
}