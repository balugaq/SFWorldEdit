package com.balugaq.sfworldedit.core.managers;

import com.balugaq.sfworldedit.api.data.Backup;
import com.balugaq.sfworldedit.api.data.Content;
import com.balugaq.sfworldedit.api.objects.CachedRequest;
import com.balugaq.sfworldedit.api.objects.SubCommand;
import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.core.commands.BlockInfoAddCommand;
import com.balugaq.sfworldedit.core.commands.BlockInfoRemoveCommand;
import com.balugaq.sfworldedit.core.commands.BlockMenuSlotFindCommand;
import com.balugaq.sfworldedit.core.commands.BlockMenuSlotSetCommand;
import com.balugaq.sfworldedit.core.commands.ClearCommand;
import com.balugaq.sfworldedit.core.commands.ClearPosCommand;
import com.balugaq.sfworldedit.core.commands.CloneCommand;
import com.balugaq.sfworldedit.core.commands.ConfirmCommand;
import com.balugaq.sfworldedit.core.commands.HelpCommand;
import com.balugaq.sfworldedit.core.commands.PasteCommand;
import com.balugaq.sfworldedit.core.commands.ReloadCommand;
import com.balugaq.sfworldedit.core.commands.RuleCommand;
import com.balugaq.sfworldedit.core.commands.SetPos1Command;
import com.balugaq.sfworldedit.core.commands.SetPos2Command;
import com.balugaq.sfworldedit.core.commands.UndoCommand;
import com.balugaq.sfworldedit.core.commands.VersionCommand;
import com.balugaq.sfworldedit.utils.ParticleUtil;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;

public class CommandManager implements IManager {
    private final Map<UUID, Backup> actions = new HashMap<>();
    private final Map<String, Queue<CachedRequest>> cachedRequests = new HashMap<>();
    private final Map<UUID, Pair<Location, Location>> selection = new HashMap<>();
    private final List<SubCommand> commands = new ArrayList<>();
    private ISFWorldEdit plugin;

    public CommandManager(@Nonnull ISFWorldEdit plugin) {
        this.plugin = plugin;
    }

    public boolean registerCommands() {
        for (SubCommand command : commands) {
            plugin.getCommand("sfw" + command.getKey()).setExecutor(command);
        }
        return true;
    }

    public void iter(@Nonnull Consumer<SubCommand> consumer) {
        commands.forEach(consumer);
    }

    public void clearSelection(@Nonnull UUID player) {
        selection.remove(player);
    }

    public void setPos1(@Nonnull UUID player, @Nonnull Location pos1) {
        selection.put(player, new Pair<>(pos1, getPos2(player)));
    }

    public void setPos2(@Nonnull UUID player, @Nonnull Location pos2) {
        selection.put(player, new Pair<>(getPos1(player), pos2));
    }

    @Nullable
    public Location getPos1(@Nonnull UUID player) {
        Pair<Location, Location> pair = selection.get(player);
        if (pair == null) {
            return null;
        }
        return selection.get(player).getFirstValue();
    }

    @Nullable
    public Location getPos2(@Nonnull UUID player) {
        Pair<Location, Location> pair = selection.get(player);
        if (pair == null) {
            return null;
        }
        return selection.get(player).getSecondValue();
    }

    public void addCachedRequest(@Nonnull CommandSender sender, @Nonnull CachedRequest request) {
        String key = sender.getName();
        if (!cachedRequests.containsKey(key)) {
            cachedRequests.put(key, new LinkedList<>());
        }

        cachedRequests.get(key).add(request);
    }

    @Nullable
    public CachedRequest pullCachedRequest(CommandSender sender) {
        if (cachedRequests.isEmpty()) {
            return null;
        }

        String key = sender.getName();
        if (!cachedRequests.containsKey(key)) {
            return null;
        }

        return cachedRequests.get(key).poll();
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

    @Override
    public void onLoad() {
        commands.add(new HelpCommand(plugin));
        commands.add(new SetPos1Command(plugin));
        commands.add(new SetPos2Command(plugin));
        commands.add(new ClearPosCommand(plugin));
        commands.add(new CloneCommand(plugin));
        commands.add(new PasteCommand(plugin));
        commands.add(new ClearCommand(plugin));
        commands.add(new BlockMenuSlotSetCommand(plugin));
        commands.add(new BlockInfoAddCommand(plugin));
        commands.add(new BlockInfoRemoveCommand(plugin));
        commands.add(new ReloadCommand(plugin));
        commands.add(new VersionCommand(plugin));
        if (plugin.getConfigManager().isAllowUndo()) {
            commands.add(new UndoCommand(plugin));
        }
        commands.add(new RuleCommand(plugin));
        commands.add(new ConfirmCommand(plugin));
        commands.add(new BlockMenuSlotFindCommand(plugin));
        runParticleTask();
    }

    @Override
    public void onUnload() {
        for (SubCommand command : commands) {
            plugin.getCommand("sfw" + command.getKey()).setExecutor(null);
        }
        cachedRequests.clear();
        selection.clear();
        commands.clear();
        this.plugin = null;
    }

    public void initBackup(UUID playerUUID) {
        if (actions.containsKey(playerUUID)) {
            return;
        }
        actions.put(playerUUID, new Backup(playerUUID));
    }

    public void addBackup(@Nonnull UUID playerUUID, @Nonnull List<Content> backup) {
        initBackup(playerUUID);
        actions.get(playerUUID).addContent(backup);
    }

    @Nonnull
    public List<Content> leftBackup(@Nonnull UUID playerUUID) {
        initBackup(playerUUID);
        return actions.get(playerUUID).getLeftContentAndDecreasePointer(new ArrayList<>());
    }

    @Nonnull
    public List<Content> rightBackup(@Nonnull UUID playerUUID) {
        initBackup(playerUUID);
        return actions.get(playerUUID).getRightContentAndIncreasePointer(new ArrayList<>());
    }

    @Nonnull
    public List<Content> getBackup(@Nonnull UUID playerUUID, int pointer) {
        initBackup(playerUUID);
        return actions.get(playerUUID).getContent(pointer);
    }


    public void clearBackup(@Nonnull UUID playerUUID) {
        initBackup(playerUUID);
        actions.get(playerUUID).clear();
    }
}
