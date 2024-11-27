package com.balugaq.sfworldedit.core.commands;

import com.balugaq.sfworldedit.api.objects.SubCommand;
import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.implementation.SFWorldedit;
import com.balugaq.sfworldedit.utils.PermissionUtil;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends SubCommand {
    private static final String KEY = "reload";
    private final ISFWorldEdit plugin;

    public ReloadCommand(@Nonnull ISFWorldEdit plugin) {
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
        Bukkit.getServer().getScheduler().runTask(Slimefun.instance(), () -> {
            final String message = plugin.getString("messages.command.reload.success");

            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            Bukkit.getServer().getPluginManager().enablePlugin(plugin);
            commandSender.sendMessage(message);
        });
        return true;
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        return new ArrayList<>();
    }
}
