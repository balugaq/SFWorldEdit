package com.balugaq.sfworldedit.core.commands;

import com.balugaq.sfworldedit.api.objects.SubCommand;
import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.utils.PermissionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class Reload extends SubCommand {
    private static final String KEY = "reload";
    private final ISFWorldEdit plugin;

    public Reload(@Nonnull ISFWorldEdit plugin) {
        this.plugin = plugin;
    }

    @Override
    @Nonnull
    public String getKey() {
        return KEY;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (!PermissionUtil.hasPermission(commandSender, this)) {
            plugin.send(commandSender, "error.no-permission");
            return false;
        }

        plugin.reloadConfig();
        plugin.send(commandSender, "command.reload.success");
        return true;
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        return new ArrayList<>();
    }
}
