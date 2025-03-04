package com.balugaq.sfworldedit.utils;

import com.balugaq.sfworldedit.api.data.BukkitContent;
import com.balugaq.sfworldedit.api.data.ChunkData;
import com.balugaq.sfworldedit.api.data.Content;
import com.balugaq.sfworldedit.api.data.SFContent;
import com.balugaq.sfworldedit.api.objects.enums.Facing;
import com.balugaq.sfworldedit.implementation.SFWorldedit;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Warning;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class WorldUtils {
    public static final boolean ALLOW_UNDO = SFWorldedit.getInstance().getConfigManager().isAllowUndo();
    @Nullable
    protected static Constructor<?> craftPlayerProfileConstructor;
    @Nullable
    protected static Class<?> craftBlockStateClass;
    @Nullable
    protected static Field interfaceBlockDataField;
    @Nullable
    protected static Field blockPositionField;
    @Nullable
    protected static Field worldField;
    @Nullable
    protected static Field weakWorldField;
    @Nullable
    protected static Class<?> craftPlayerProfileClass;
    protected static Class<?> craftSkullClass;
    protected static Method setOwnerProfileMethod;
    protected static boolean success = false;

    static {
        try {
            World sampleWorld = Bukkit.getWorlds().get(0);
            BlockState blockstate = sampleWorld.getBlockAt(0, 0, 0).getState();
            var result = ReflectionUtil.getDeclaredFieldsRecursively(blockstate.getClass(), "data");
            interfaceBlockDataField = result.getFirstValue();
            interfaceBlockDataField.setAccessible(true);
            craftBlockStateClass = result.getSecondValue();
            blockPositionField = ReflectionUtil.getDeclaredFieldsRecursively(craftBlockStateClass, "position").getFirstValue();
            blockPositionField.setAccessible(true);
            worldField = ReflectionUtil.getDeclaredFieldsRecursively(craftBlockStateClass, "world").getFirstValue();
            worldField.setAccessible(true);
            weakWorldField = ReflectionUtil.getDeclaredFieldsRecursively(craftBlockStateClass, "weakWorld").getFirstValue();
            weakWorldField.setAccessible(true);
            success = true;
        } catch (Throwable ignored) {

        }
    }

    @CanIgnoreReturnValue
    public static boolean copyBlockState(@Nonnull BlockState copy, @Nonnull Block toBlock) {
        if (!success) {
            return false;
        }

        BlockState toState = toBlock.getState();
        if (!craftBlockStateClass.isInstance(toState) || !craftBlockStateClass.isInstance(copy)) {
            return false;
        }

        try {
            blockPositionField.set(copy, blockPositionField.get(toState));
            worldField.set(copy, worldField.get(toState));
            weakWorldField.set(copy, weakWorldField.get(toState));
            copy.update(true, false);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nonnull
    public static String locationToString(@Nonnull Location l) {
        if (l == null) {
            return SFWorldedit.getInstance().getLocalizationService().getString("messages.error.unknown-location");
        }
        if (l.getWorld() == null) {
            return SFWorldedit.getInstance().getLocalizationService().getString("messages.error.unknown-world");
        }
        return l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }

    public static long locationRange(@Nonnull Location pos1, @Nonnull Location pos2) {
        if (pos1 == null || pos2 == null) {
            return 0;
        }

        final int downX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        final int upX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        final int downY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        final int upY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        final int downZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        final int upZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        return (long) (Math.abs(upX - downX) + 1) * (Math.abs(upY - downY) + 1) * (Math.abs(upZ - downZ) + 1);
    }

    @Deprecated(forRemoval = true)
    @Warning(reason = """
                This method is deprecated and will be removed in the future.
                Use the new method doWorldEdit(Player, Location, Location, Consumer<Location>, Runnable) instead.
            """)
    public static void doWorldEdit(@Nullable Player player, @Nonnull Location pos1, @Nonnull Location pos2) {
        doWorldEdit(player, pos1, pos2, null);
    }

    @Deprecated(forRemoval = true)
    @Warning(reason = """
                This method is deprecated and will be removed in the future.
                Use the new method doWorldEdit(Player, Location, Location, Consumer<Location>, Runnable) instead.
            """)
    public static void doWorldEdit(@Nullable Player player, @Nonnull Location pos1, @Nonnull Location pos2, @Nonnull Consumer<Location> consumer) {
        doWorldEdit(player, pos1, pos2, consumer, null);
    }

    public static void doWorldEdit(@Nullable Player player, @Nonnull Location pos1, @Nonnull Location pos2, @Nonnull Consumer<Location> consumer, @Nullable Runnable ending) {
        if (pos1 == null || pos2 == null) {
            return;
        }

        final int downX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        final int upX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        final int downY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        final int upY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        final int downZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        final int upZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        final World world = pos1.getWorld();
        final Map<ChunkData, Set<Location>> chunks = new HashMap<>();

        for (int x = downX; x <= upX; x++) {
            for (int y = downY; y <= upY; y++) {
                for (int z = downZ; z <= upZ; z++) {
                    final ChunkData chunkData = new ChunkData(world, x >> 4, z >> 4);
                    if (chunks.containsKey(chunkData)) {
                        chunks.get(chunkData).add(new Location(world, x, y, z));
                    } else {
                        final Set<Location> locations = new HashSet<>();
                        locations.add(new Location(world, x, y, z));
                        chunks.put(chunkData, locations);
                    }
                }
            }
        }

        List<Content> backup = new ArrayList<>();
        final Iterator<ChunkData> iterator = chunks.keySet().iterator();
        final int chunkLimitPerSecond = SFWorldedit.getInstance().getConfigManager().getModificationChunkPerSecond();
        for (int i = 0; i < chunks.size() && iterator.hasNext(); i += chunkLimitPerSecond) {
            Debug.debug("WorldEdit: processing chunk " + i + "/" + chunks.size());
            Bukkit.getScheduler().runTaskLater(SFWorldedit.getInstance(), () -> {
                Debug.debug("WorldEdit: processing task...");
                for (int j = 0; j < chunkLimitPerSecond && iterator.hasNext(); j++) {
                    final ChunkData chunkData = iterator.next();
                    final Set<Location> locations = chunks.get(chunkData);
                    for (Location location : locations) {
                        if (ALLOW_UNDO && player != null) {
                            if (isSlimefunBlock(location)) {
                                backup.add(getSFContent(location));
                            } else {
                                backup.add(getBukkitContent(location));
                            }
                        }
                        consumer.accept(location);
                    }
                }
            }, 20L * i / chunkLimitPerSecond);
        }

        if (player != null) {
            Bukkit.getScheduler().runTaskLater(SFWorldedit.getInstance(), () -> {
                if (ALLOW_UNDO) {
                    Debug.debug("WorldEdit: saving backup...");
                    SFWorldedit.getInstance().getCommandManager().addBackup(player.getUniqueId(), backup);
                }
                if (ending != null) {
                    ending.run();
                }
            }, 20L * chunks.size() / chunkLimitPerSecond);
        }
    }

    public static void doSimpleWorldEdit(@Nonnull Location pos1, @Nonnull Location pos2, @Nonnull Consumer<Location> consumer, @Nonnull Runnable ending) {
        if (pos1 == null || pos2 == null) {
            return;
        }

        final int downX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        final int upX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        final int downY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        final int upY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        final int downZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        final int upZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        final World world = pos1.getWorld();
        final Map<ChunkData, Set<Location>> chunks = new HashMap<>();

        for (int x = downX; x <= upX; x++) {
            for (int y = downY; y <= upY; y++) {
                for (int z = downZ; z <= upZ; z++) {
                    final ChunkData chunkData = new ChunkData(world, x >> 4, z >> 4);
                    if (chunks.containsKey(chunkData)) {
                        chunks.get(chunkData).add(new Location(world, x, y, z));
                    } else {
                        final Set<Location> locations = new HashSet<>();
                        locations.add(new Location(world, x, y, z));
                        chunks.put(chunkData, locations);
                    }
                }
            }
        }

        List<Content> backup = new ArrayList<>();
        final Iterator<ChunkData> iterator = chunks.keySet().iterator();
        final int chunkLimitPerSecond = SFWorldedit.getInstance().getConfigManager().getModificationChunkPerSecond();
        for (int i = 0; i < chunks.size() && iterator.hasNext(); i += chunkLimitPerSecond) {
            Debug.debug("WorldEdit: processing chunk " + i + "/" + chunks.size());
            Bukkit.getScheduler().runTaskLater(SFWorldedit.getInstance(), () -> {
                Debug.debug("WorldEdit: processing task...");
                for (int j = 0; j < chunkLimitPerSecond && iterator.hasNext(); j++) {
                    final ChunkData chunkData = iterator.next();
                    final Set<Location> locations = chunks.get(chunkData);
                    for (Location location : locations) {
                        consumer.accept(location);
                    }
                }
            }, 20L * i / chunkLimitPerSecond);
        }

        Bukkit.getScheduler().runTaskLater(SFWorldedit.getInstance(), ending, 20L * chunks.size() / chunkLimitPerSecond);
    }

    public static long getRange(@Nonnull Location pos1, @Nonnull Location pos2) {
        if (pos1 == null || pos2 == null) {
            return 0;
        }
        final int downX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        final int upX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        final int downY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        final int upY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        final int downZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        final int upZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        return (long) (Math.abs(upX - downX) + 1) * (Math.abs(upY - downY) + 1) * (Math.abs(upZ - downZ) + 1);
    }

    public static boolean isSlimefunBlock(@Nonnull Location location) {
        return StorageCacheUtils.hasBlock(location);
    }

    @Nonnull
    public static BukkitContent getBukkitContent(@Nonnull Location location) {
        return new BukkitContent(location, location.getBlock().getState());
    }

    @Nonnull
    public static SFContent getSFContent(@Nonnull Location location) {
        SlimefunItem item = StorageCacheUtils.getSfItem(location);
        String id = null;
        if (item != null) {
            id = item.getId();
        }

        Map<Integer, ItemStack> menu = new HashMap<>();
        BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
        if (blockMenu != null) {
            for (int i = 0; i < blockMenu.getSize(); i++) {
                ItemStack itemStack = blockMenu.getItemInSlot(i);
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    menu.put(i, itemStack);
                }
            }
        }

        Map<String, String> data = new HashMap<>();
        SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
        StorageCacheUtils.requestLoad(blockData);
        if (blockData != null) {
            try {
                data = blockData.getAllData();
            } catch (IllegalStateException ignored) {
            }
        }

        return new SFContent(location, location.getBlock().getState(), id, menu, data, true);
    }

    @Nonnull
    public static Facing getFacing(float yaw, float pitch) {
        yaw = yaw % 360;
        if (yaw < 0) yaw += 360;

        if (pitch > 45) {
            return Facing.NY;
        } else if (pitch < -45) {
            return Facing.PY;
        }

        if (yaw >= 315 || yaw < 45) {
            return Facing.PZ;
        } else if (yaw >= 45 && yaw < 135) {
            return Facing.NX;
        } else if (yaw >= 135 && yaw < 225) {
            return Facing.NZ;
        } else {
            return Facing.PX;
        }
    }
}