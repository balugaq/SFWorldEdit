package com.balugaq.sfworldedit.core.commands;

import com.balugaq.sfworldedit.api.objects.SubCommand;
import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.core.managers.DisplayManager;
import com.balugaq.sfworldedit.implementation.SFWorldedit;
import com.balugaq.sfworldedit.utils.PermissionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ClearProjectileCommand extends SubCommand {
    public static final String PREFIX = SFWorldedit.getInstance().getName();
    private static final String KEY = "clearprojectile";
    private final ISFWorldEdit plugin;

    public ClearProjectileCommand(ISFWorldEdit plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nonnull String getKey() {
        return KEY;
    }

    @Override
    public @Nonnull List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        return new ArrayList<>();
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

        SFWorldedit plugin = SFWorldedit.getInstance();
        DisplayManager.halt();
        plugin.getDisplayManager().onLoad();

        player.getWorld().getEntities().forEach(entity -> {
            if (entity instanceof Display display) {
                List<MetadataValue> metadata = display.getMetadata(SFWorldedit.getInstance().getName());
                if (!metadata.isEmpty() && metadata.get(0).asBoolean()) {
                    display.remove();
                }
            }
        });

        plugin.send(player, "commands.clearprojectile.success");

        return true;
    }
}
