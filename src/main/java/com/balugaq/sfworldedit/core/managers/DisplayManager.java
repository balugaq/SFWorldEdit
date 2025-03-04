package com.balugaq.sfworldedit.core.managers;

import com.balugaq.sfworldedit.implementation.SFWorldedit;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.metamechanists.displaymodellib.sefilib.entity.display.DisplayGroup;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@Getter
public class DisplayManager implements IManager {
    private final Map<UUID, DisplayGroup> displays = new HashMap<>();
    private final JavaPlugin plugin;
    private boolean running;

    public DisplayManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static DisplayGroup getDisplayGroup(UUID uuid) {
        return SFWorldedit.getInstance().getDisplayManager().getDisplayGroup0(uuid);
    }

    public static void killDisplays(UUID uuid) {
        SFWorldedit.getInstance().getDisplayManager().killDisplays0(uuid);
    }

    public static void registerDisplayGroup(UUID uuid, DisplayGroup group) {
        SFWorldedit.getInstance().getDisplayManager().registerDisplayGroup0(uuid, group);
    }

    public static void halt() {
        SFWorldedit.getInstance().getDisplayManager().halt0();
    }

    public static Map<UUID, DisplayGroup> getDisplayGroups() {
        return SFWorldedit.getInstance().getDisplayManager().getDislayGroups0();
    }

    public void halt0() {
        running = false;
        for (UUID uuid : new HashSet<>(displays.keySet())) {
            killDisplays(uuid);
        }
    }

    public void killDisplays0(UUID uuid) {
        if (running) {
            DisplayGroup group = displays.get(uuid);
            if (group != null) {
                group.remove();
            }
            displays.remove(uuid);
        }
    }

    public void registerDisplayGroup0(UUID uuid, DisplayGroup group) {
        displays.put(uuid, group);
    }

    public DisplayGroup getDisplayGroup0(UUID uuid) {
        return displays.get(uuid);
    }

    @Override
    public void onLoad() {
        running = true;
    }

    @Override
    public void onUnload() {
        halt();
    }

    @Nonnull
    public Map<UUID, DisplayGroup> getDislayGroups0() {
        return displays;
    }
}
