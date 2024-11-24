package com.balugaq.sfworldedit.core.commands;

import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.utils.CommandUtil;
import com.balugaq.sfworldedit.utils.PermissionUtil;
import com.balugaq.sfworldedit.utils.WorldUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockInfoAdd extends SubCommand {
    private static final String KEY = "blockinfoadd";
    private final ISFWorldEdit plugin;
    public BlockInfoAdd(@Nonnull ISFWorldEdit plugin) {
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

        Location pos1 = plugin.getCommandManager().getPos1(player.getUniqueId());
        Location pos2 = plugin.getCommandManager().getPos2(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            plugin.send(commandSender, "error.no-selection");
            return false;
        }

        if (!Objects.equals(pos1.getWorld().getUID(), pos2.getWorld().getUID())) {
            plugin.send(commandSender, "error.world-mismatch");
            return false;
        }

        plugin.send(player, "command.blockinfoadd.start", WorldUtils.locationToString(pos1), WorldUtils.locationToString(pos2));

        if (args.length == 0) {
            plugin.send(player, "error.missing-argument", "key");
            return false;
        }

        if (args.length == 1) {
            plugin.send(player, "error.missing-argument", "value");
            return false;
        }

        final String key = args[0];
        final String value = args[1];
        final long currentMillSeconds = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger();
        WorldUtils.doWorldEdit(pos1, pos2, (location -> {
            if (StorageCacheUtils.getBlock(location) != null) {
                StorageCacheUtils.setData(location, key, value);
                count.addAndGet(1);
            }
        }));

        plugin.send(player, "command.blockinfoadd.success", count, System.currentTimeMillis() - currentMillSeconds);
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
