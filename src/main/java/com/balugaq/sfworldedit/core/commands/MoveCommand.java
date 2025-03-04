package com.balugaq.sfworldedit.core.commands;

import com.balugaq.sfworldedit.api.Preparable;
import com.balugaq.sfworldedit.api.objects.SubCommand;
import com.balugaq.sfworldedit.api.objects.enums.Facing;
import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.core.managers.DisplayManager;
import com.balugaq.sfworldedit.utils.PermissionUtil;
import com.balugaq.sfworldedit.utils.WorldUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MoveCommand extends SubCommand implements Preparable {
    private static final String KEY = "move";
    @Nonnull
    private final ISFWorldEdit plugin;

    public MoveCommand(@Nonnull ISFWorldEdit plugin) {
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

        UUID uuid = player.getUniqueId();
        int distance = 1;
        if (args.length >= 1) {
            String s = args[0];
            if (s.equalsIgnoreCase("cancel")) {
                DisplayManager.killDisplays(uuid);
                plugin.send(player, "command.move.success");
                return true;
            }
            try {
                distance = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                plugin.send(player, "error.invalid-argument", s);
                return false;
            }
        }

        Facing facing = WorldUtils.getFacing(player.getLocation().getYaw(), player.getLocation().getPitch());
        move(uuid, facing, distance);

        plugin.send(player, "command.move.success");

        return true;
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (!PermissionUtil.hasPermission(commandSender, this)) {
            return new ArrayList<>();
        }

        return new ArrayList<>();
    }

    @Override
    @Nonnull
    public String getKey() {
        return KEY;
    }
}
