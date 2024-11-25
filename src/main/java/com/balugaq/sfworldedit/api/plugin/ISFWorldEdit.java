package com.balugaq.sfworldedit.api.plugin;

import com.balugaq.sfworldedit.core.managers.CommandManager;
import com.balugaq.sfworldedit.core.services.LocalizationService;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;

public abstract class ISFWorldEdit extends JavaPlugin implements SlimefunAddon  {
    @Nonnull
    public abstract LocalizationService getLocalizationService();
    public void send(@Nonnull CommandSender sender, @Nonnull String messageKey, @Nonnull Object... args) {
        getLocalizationService().send(sender, messageKey, args);
    }

    public void sendList(@Nonnull CommandSender sender, @Nonnull String messageKey) {
        getLocalizationService().sendList(sender, messageKey);
    }

    @Nonnull
    public abstract CommandManager getCommandManager();
}
