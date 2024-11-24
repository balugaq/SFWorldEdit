package com.balugaq.sfworldedit.core.managers;

import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.core.commands.BlockInfoAdd;
import com.balugaq.sfworldedit.core.commands.BlockInfoRemove;
import com.balugaq.sfworldedit.core.commands.BlockMenuSlotSet;
import com.balugaq.sfworldedit.core.commands.Clear;
import com.balugaq.sfworldedit.core.commands.ClearPos;
import com.balugaq.sfworldedit.core.commands.Clone;
import com.balugaq.sfworldedit.core.commands.Paste;
import com.balugaq.sfworldedit.core.commands.SetPos1;
import com.balugaq.sfworldedit.core.commands.SetPos2;
import com.balugaq.sfworldedit.core.commands.SubCommand;
import com.balugaq.sfworldedit.utils.ParticleUtil;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommandManager implements IManager {
    public final Map<UUID, Pair<Location, Location>> selection = new HashMap<>();
    private final List<SubCommand> commands = new ArrayList<>();
    private final ISFWorldEdit plugin;
    public CommandManager(@Nonnull ISFWorldEdit plugin) {
        this.plugin = plugin;
    }

    public boolean registerCommands() {
        for (SubCommand command : commands) {
            plugin.getCommand("sfw" + command.getKey()).setExecutor(command);
        }
        return true;
    }

    @Override
    public void onLoad() {
        commands.add(new SetPos1(plugin));
        commands.add(new SetPos2(plugin));
        commands.add(new ClearPos(plugin));
        commands.add(new Clone(plugin));
        commands.add(new Paste(plugin));
        commands.add(new Clear(plugin));
        commands.add(new BlockMenuSlotSet(plugin));
        commands.add(new BlockInfoAdd(plugin));
        commands.add(new BlockInfoRemove(plugin));
        runParticleTask();
    }

    @Override
    public void onUnload() {
        for (SubCommand command : commands) {
            plugin.getCommand("sfw" + command.getKey()).setExecutor(null);
        }
        selection.clear();
        commands.clear();
    }

    public void clearSelection(UUID player) {
        selection.remove(player);
    }

    public void setPos1(UUID player, Location pos1) {
        selection.put(player, new Pair<>(pos1, getPos2(player)));
    }

    public void setPos2(UUID player, Location pos2) {
        selection.put(player, new Pair<>(getPos1(player), pos2));
    }

    public Location getPos1(UUID player) {
        return selection.get(player).getFirstValue();
    }

    public Location getPos2(UUID player) {
        return selection.get(player).getSecondValue();
    }

    public void runParticleTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            Iterator<UUID> iterator = selection.keySet().iterator();
            while (iterator.hasNext()) {
                UUID uuid = iterator.next();
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) {
                    continue;
                }

                Location pos1 = getPos1(uuid);
                Location pos2 = getPos2(uuid);
                if (pos1 == null || pos2 == null) {
                    return;
                }

                plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> ParticleUtil.drawRegionOutline(plugin, Particle.WAX_OFF, 0, pos1, pos2), Slimefun.getTickerTask().getTickRate());
            }
        }, 0, Slimefun.getTickerTask().getTickRate());
    }
}
