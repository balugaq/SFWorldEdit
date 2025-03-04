package com.balugaq.sfworldedit.api;

import com.balugaq.sfworldedit.api.objects.enums.Facing;
import com.balugaq.sfworldedit.core.commands.ClearProjectileCommand;
import com.balugaq.sfworldedit.core.managers.DisplayManager;
import com.balugaq.sfworldedit.implementation.SFWorldedit;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.Nullable;
import org.metamechanists.displaymodellib.models.components.ModelCuboid;
import org.metamechanists.displaymodellib.models.components.ModelItem;
import org.metamechanists.displaymodellib.sefilib.entity.display.DisplayGroup;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface Preparable {
    String PREFIX = ClearProjectileCommand.PREFIX;
    ModelCuboid BLOCK_BASE = new ModelCuboid()
            .size(0.6F, 0.6F, 0.6F);
    ModelItem ITEM_BASE = new ModelItem()
            .size(1F);

    default boolean isPreparing(UUID uuid) {
        return getDisplayGroup(uuid) != null;
    }

    default boolean hasPreparedArgs(@Nonnull String... args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("-prepare") || arg.equalsIgnoreCase("-p")) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    default List<String> prepareArgs(String... args) {
        if (hasPreparedArgs(args)) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(List.of("-prepare", "-p"));
        }
    }

    @Nonnull
    @CanIgnoreReturnValue
    default DisplayGroup display(UUID uuid, @Nonnull Location location, @Nonnull Material material) {
        if (material == Material.PLAYER_WALL_HEAD) {
            material = Material.PLAYER_HEAD;
        }
        String ls = PREFIX + ":" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        Location displayLocation = location.clone().add(0.5, 0.5, 0.5);
        Display display;
        if (!isTile(material)) {
            display = BLOCK_BASE.material(material).build(displayLocation);
        } else {
            display = ITEM_BASE.material(material).build(displayLocation);
        }
        display.setMetadata(PREFIX, new FixedMetadataValue(SFWorldedit.getInstance(), true));
        DisplayGroup displayGroup = getOrCreateDisplayGroup(uuid, location);
        displayGroup.addDisplay(ls, display);
        return displayGroup;
    }

    @Nonnull
    @CanIgnoreReturnValue
    default DisplayGroup display(UUID uuid, @Nonnull Location location, @Nonnull BlockData blockData) {
        String ls = PREFIX + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        Location displayLocation = location.clone().add(0.5, 0.5, 0.5);
        Display display;
        if (!isTile(blockData.getMaterial())) {
            display = BLOCK_BASE.block(blockData).build(displayLocation);
        } else {
            display = ITEM_BASE.material(blockData.getMaterial()).build(displayLocation);
        }
        display.setMetadata(PREFIX, new FixedMetadataValue(SFWorldedit.getInstance(), true));
        DisplayGroup displayGroup = getOrCreateDisplayGroup(uuid, location);
        displayGroup.addDisplay(ls, display);
        return displayGroup;
    }

    @CanIgnoreReturnValue
    default DisplayGroup move(UUID uuid, @Nonnull Facing facing) {
        return move(uuid, facing, 1);
    }

    @CanIgnoreReturnValue
    default @Nullable DisplayGroup move(UUID uuid, @Nonnull Facing facing, int distance) {
        DisplayGroup displayGroup = getDisplayGroup(uuid);
        if (displayGroup == null) {
            return null;
        }
        for (Display display : displayGroup.getDisplays().values()) {
            switch (facing) {
                case PX -> display.teleport(display.getLocation().clone().add(distance, 0, 0));
                case NX -> display.teleport(display.getLocation().clone().add(-distance, 0, 0));
                case PY -> display.teleport(display.getLocation().clone().add(0, distance, 0));
                case NY -> display.teleport(display.getLocation().clone().add(0, -distance, 0));
                case PZ -> display.teleport(display.getLocation().clone().add(0, 0, distance));
                case NZ -> display.teleport(display.getLocation().clone().add(0, 0, -distance));
            }
        }

        return displayGroup;
    }

    @Nonnull
    @CanIgnoreReturnValue
    default DisplayGroup createDisplayGroup(UUID uuid, @Nonnull Location location) {
        DisplayGroup displayGroup = new DisplayGroup(location, 0.0F, 0.0F);
        getDisplayGroups().put(uuid, displayGroup);
        return displayGroup;
    }

    default DisplayGroup getDisplayGroup(UUID uuid) {
        return getDisplayGroups().get(uuid);
    }

    default DisplayGroup getOrCreateDisplayGroup(UUID uuid, @Nonnull Location location) {
        DisplayGroup displayGroup = getDisplayGroup(uuid);
        if (displayGroup == null) {
            displayGroup = createDisplayGroup(uuid, location);
            getDisplayGroups().put(uuid, displayGroup);
        }
        return displayGroup;
    }

    default Map<UUID, DisplayGroup> getDisplayGroups() {
        return DisplayManager.getDisplayGroups();
    }

    default void removeDisplayGroupFor(UUID uuid) {
        DisplayGroup displayGroup = getDisplayGroup(uuid);
        if (displayGroup != null) {
            displayGroup.remove();
        }

        getDisplayGroups().remove(uuid);
    }

    static boolean isTile(Material material) {
        if (Tag.BANNERS.isTagged(material) || Tag.BEDS.isTagged(material) ||Tag.SIGNS.isTagged(material) || Tag.SHULKER_BOXES.isTagged(material)) {
            return true;
        }

        boolean isTile = false;
        switch (material) {
            case BARREL, BEACON, BEEHIVE, BELL, BLAST_FURNACE, BREWING_STAND, SUSPICIOUS_GRAVEL, SUSPICIOUS_SAND, CALIBRATED_SCULK_SENSOR, CAMPFIRE, SOUL_CAMPFIRE, CHEST, ENDER_CHEST, TRAPPED_CHEST, CHISELED_BOOKSHELF, COMMAND_BLOCK, CHAIN_COMMAND_BLOCK, REPEATING_COMMAND_BLOCK, COMPARATOR, CONDUIT, SPAWNER, DAYLIGHT_DETECTOR, DECORATED_POT, DISPENSER, DROPPER, ENCHANTING_TABLE, END_GATEWAY, FURNACE, HOPPER, JIGSAW, JUKEBOX, LECTERN, SCULK_CATALYST, SCULK_SENSOR, SCULK_SHRIEKER, SMOKER, STRUCTURE_BLOCK -> {
                isTile = true;
            }
        }

        if (isTile) {
            return true;
        }

        if (material.name().equals("CRAFTER") || material.name().equals("CREAKING_HEART") || material.name().equals("TRIAL_SPAWNER") || material.name().equals("VAULT")) {
            return true;
        }

        if (material.name().endsWith("_HEAD")) {
            return true;
        }

        return false;
    }
}
