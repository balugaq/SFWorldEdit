package com.balugaq.sfworldedit.core.commands;

import com.balugaq.sfworldedit.api.objects.SubCommand;
import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class Help extends SubCommand {
    private static final String KEY = "help";
    private final ISFWorldEdit plugin;

    public Help(@Nonnull ISFWorldEdit plugin) {
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
        if (args.length == 0) {
            plugin.sendList(commandSender, "command.help.content");
            return true;
        }

        String subCommand = args[0];
        plugin.getCommandManager().iter(cmd -> {
            if (cmd.getKey().equals(subCommand)) {
                plugin.sendList(commandSender, "command.help.usage." + cmd.getKey());
            }
        });

        plugin.send(commandSender, "command.error.unknown-subcommand", subCommand);

        return true;
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (args.length <= 1) {
            List<String> result = new ArrayList<>();
            plugin.getCommandManager().iter(cmd -> {
                result.add(cmd.getKey());
            });
            return result;
        }
        return new ArrayList<>();
    }
}
