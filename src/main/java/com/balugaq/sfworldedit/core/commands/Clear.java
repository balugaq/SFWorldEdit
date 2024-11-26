package com.balugaq.sfworldedit.core.commands;

import com.balugaq.sfworldedit.api.objects.SubCommand;
import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.utils.CommandUtil;
import com.balugaq.sfworldedit.utils.PermissionUtil;
import com.balugaq.sfworldedit.utils.WorldUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Clear extends SubCommand {
    private static final String KEY = "clear";
    private final ISFWorldEdit plugin;

    public Clear(@Nonnull ISFWorldEdit plugin) {
        this.plugin = plugin;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (!PermissionUtil.hasPermission(commandSender, this)) {
            plugin.send(commandSender, "error.no-permission");
            return false;
        }

        if (!(commandSender instanceof Player player)) {
            plugin.send(commandSender, "error.player-only");
            return false;
        }

        final Location pos1 = plugin.getCommandManager().getPos1(player.getUniqueId());
        final Location pos2 = plugin.getCommandManager().getPos2(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            plugin.send(commandSender, "error.no-selection");
            return false;
        }

        if (!Objects.equals(pos1.getWorld().getUID(), pos2.getWorld().getUID())) {
            plugin.send(commandSender, "error.world-mismatch");
            return false;
        }

        final long range = WorldUtils.getRange(pos1, pos2);
        final long max = plugin.getConfigManager().getModificationBlockLimit();
        if (range > max) {
            plugin.send(commandSender, "error.too-many-blocks", range, max);
            return false;
        }

        plugin.send(player, "command.clear.start", WorldUtils.locationToString(pos1), WorldUtils.locationToString(pos2));

        final boolean callHandler = CommandUtil.hasFlag(args, "callhandler") || CommandUtil.hasFlag(args, "c");
        final boolean skipVanilla = CommandUtil.hasFlag(args, "skipvanilla") || CommandUtil.hasFlag(args, "s");
        final long currentMillSeconds = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger();
        WorldUtils.doWorldEdit(pos1, pos2, (location -> {
            final Block targetBlock = pos1.getWorld().getBlockAt(location);
            if (StorageCacheUtils.hasBlock(location)) {
                if (callHandler) {
                    SlimefunItem item = StorageCacheUtils.getSfItem(location);
                    if (item != null) {
                        item.callItemHandler(BlockBreakHandler.class, handler -> handler.onPlayerBreak(
                                new BlockBreakEvent(targetBlock, player),
                                new ItemStack(Material.AIR),
                                new ArrayList<>()
                        ));
                    }
                }
                targetBlock.setType(Material.AIR);
            }
            Slimefun.getDatabaseManager().getBlockDataController().removeBlock(location);
            if (!skipVanilla) {
                targetBlock.setType(Material.AIR);
            }
            count.addAndGet(1);
        }));

        plugin.send(player, "command.clear.success", count.get(), System.currentTimeMillis() - currentMillSeconds);
        return true;
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        return new ArrayList<>();
    }

    @Override
    @Nonnull
    public String getKey() {
        return KEY;
    }
}
