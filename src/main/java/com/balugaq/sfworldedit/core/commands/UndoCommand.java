package com.balugaq.sfworldedit.core.commands;

import com.balugaq.sfworldedit.api.data.Content;
import com.balugaq.sfworldedit.api.objects.SubCommand;
import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.utils.PermissionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UndoCommand extends SubCommand {
    private static final String KEY = "undo";
    @Nonnull
    private final ISFWorldEdit plugin;

    public UndoCommand(@Nonnull ISFWorldEdit plugin) {
        this.plugin = plugin;
    }

    @Override
    @Nonnull
    public String getKey() {
        return KEY;
    }

    @Override
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
        List<Content> contents = plugin.getCommandManager().leftBackup(uuid);
        if (contents.isEmpty()) {
            plugin.send(player, "error.no-undo");
            return false;
        }

        for (Content content : contents) {
            content.action();
        }

        plugin.send(player, "command.undo.success");
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        return new ArrayList<>();
    }
}
