package com.balugaq.sfworldedit.utils;

import com.balugaq.sfworldedit.core.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class PermissionUtil {
    public static boolean hasPermission(CommandSender sender, String permission, SubCommand subCommand) {
        if (sender instanceof ConsoleCommandSender) {
            return true;
        }

        if (sender.isOp() || sender.hasPermission(Constants.PERMISSION_ADMIN) || sender.hasPermission(Constants.PERMISSION_COMMAND_ADMIN) || sender.hasPermission(permission)) {
            return true;
        }

        return false;
    }

    public static boolean hasPermission(CommandSender sender, SubCommand subCommand) {
        return hasPermission(sender, "sfworldedit.command." + subCommand.getKey().toLowerCase(), subCommand);
    }
}
