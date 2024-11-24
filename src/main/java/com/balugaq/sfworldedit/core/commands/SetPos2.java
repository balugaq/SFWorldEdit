package com.balugaq.sfworldedit.core.commands;

import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.utils.PermissionUtil;
import com.balugaq.sfworldedit.utils.WorldUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class SetPos2 extends SubCommand {
    private static final String KEY = "pos2";
    private final ISFWorldEdit plugin;
    public SetPos2(@Nonnull ISFWorldEdit plugin) {
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

        plugin.getCommandManager().setPos2(player.getUniqueId(), player.getLocation().getBlock().getLocation());
        if (plugin.getCommandManager().getPos1(player.getUniqueId()) != null) {
            plugin.send(player, "command.setpos2.success-with-range", WorldUtils.locationRange(plugin.getCommandManager().getPos1(player.getUniqueId()), plugin.getCommandManager().getPos2(player.getUniqueId())));
        } else {
            plugin.send(player, "command.setpos2.success", plugin.getCommandManager().getPos1(player.getUniqueId()));
        }
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
