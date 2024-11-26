package com.balugaq.sfworldedit.api.plugin;

import com.balugaq.sfworldedit.core.managers.CommandManager;
import com.balugaq.sfworldedit.core.managers.ConfigManager;
import com.balugaq.sfworldedit.core.services.LocalizationService;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public abstract class ISFWorldEdit extends JavaPlugin implements SlimefunAddon {
    @Nonnull
    public abstract LocalizationService getLocalizationService();

    @Nonnull
    public String getString(@Nonnull String path) {
        return getLocalizationService().getString(path);
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    public String getString(@Nonnull String key, @Nonnull Object... args) {
        return getLocalizationService().getString(key, args);
    }


    public void send(@Nonnull CommandSender sender, @Nonnull String messageKey, @Nonnull Object... args) {
        getLocalizationService().send(sender, messageKey, args);
    }

    public void sendList(@Nonnull CommandSender sender, @Nonnull String messageKey) {
        getLocalizationService().sendList(sender, messageKey);
    }

    @Nonnull
    public abstract CommandManager getCommandManager();

    @Nonnull
    public abstract ConfigManager getConfigManager();
}
