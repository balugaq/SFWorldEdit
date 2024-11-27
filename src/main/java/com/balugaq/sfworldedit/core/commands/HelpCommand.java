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
import java.util.concurrent.atomic.AtomicBoolean;

public class HelpCommand extends SubCommand {
    private static final String KEY = "help";
    private final ISFWorldEdit plugin;

    public HelpCommand(@Nonnull ISFWorldEdit plugin) {
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

        if (args.length == 0) {
            plugin.sendList(commandSender, "messages.command.help.content");
            return true;
        }

        final String subCommand = args[0];
        final AtomicBoolean found = new AtomicBoolean(false);
        plugin.getCommandManager().iter(cmd -> {
            if (cmd.getKey().equals(subCommand)) {
                plugin.sendList(commandSender, "messages.command.help.usage." + cmd.getKey());
                found.set(true);
            }
        });

        if (!found.get()) {
            plugin.send(commandSender, "error.unknown-subcommand", subCommand);
        }

        return true;
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (args.length <= 1) {
            final List<String> result = new ArrayList<>();
            plugin.getCommandManager().iter(cmd -> result.add(cmd.getKey()));
            return result;
        }
        return new ArrayList<>();
    }
}
