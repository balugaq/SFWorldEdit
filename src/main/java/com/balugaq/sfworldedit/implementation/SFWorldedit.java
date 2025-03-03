package com.balugaq.sfworldedit.implementation;

import com.balugaq.sfworldedit.api.objects.enums.MinecraftVersion;
import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.core.managers.CommandManager;
import com.balugaq.sfworldedit.core.managers.ConfigManager;
import com.balugaq.sfworldedit.core.services.LocalizationService;
import com.balugaq.sfworldedit.utils.Debug;
import com.balugaq.sfworldedit.utils.SlimefunItemUtil;
import com.google.common.base.Preconditions;
import io.github.thebusybiscuit.slimefun4.utils.NumberUtils;
import lombok.Getter;
import net.guizhanss.guizhanlibplugin.updater.GuizhanUpdater;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.MessageFormat;

/**
 * This is the main class of the JustEnoughGuide plugin.
 * It depends on the Slimefun4 plugin and provides a set of features to improve the game experience.
 *
 * @author balugaq
 * @since 1.0
 */
@SuppressWarnings("unused")
@Getter
public class SFWorldedit extends ISFWorldEdit implements Listener {
    private static final String DEFAULT_LANGUAGE = "zh-CN";
    private static final int RECOMMENDED_JAVA_VERSION = 17;
    private static final MinecraftVersion RECOMMENDED_MC_VERSION = MinecraftVersion.MINECRAFT_1_16;
    @Nullable
    private static SFWorldedit instance;
    @Nonnull
    private final String username;
    @Nonnull
    private final String repo;
    @Nonnull
    private final String branch;
    @Nullable
    private CommandManager commandManager;
    @Nullable
    private ConfigManager configManager;
    @Nullable
    private LocalizationService localizationService;
    @Nullable
    private MinecraftVersion minecraftVersion;
    private int javaVersion;


    public SFWorldedit() {
        this.username = "balugaq";
        this.repo = "SFWorldedit";
        this.branch = "master";
    }

    @Nonnull
    public static MinecraftVersion getMinecraftVersion() {
        return getInstance().minecraftVersion;
    }

    @Nonnull
    public static SFWorldedit getInstance() {
        Preconditions.checkArgument(instance != null, "SFWorldedit has not been enabled yet！");
        return SFWorldedit.instance;
    }

    @Nonnull
    public static String getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }

    @Override
    @Nonnull
    public ConfigManager getConfigManager() {
        return getInstance().configManager;
    }

    @Override
    @Nonnull
    public CommandManager getCommandManager() {
        return getInstance().commandManager;
    }

    @Override
    public void onEnable() {
        Preconditions.checkArgument(instance == null, "SFWorldEdit already has been enabled！");
        instance = this;

        Debug.info("Loading Config Manager...");
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.configManager.onLoad();

        Debug.info("Loading Localization Service...");
        this.localizationService = new LocalizationService(this);
        Debug.info("Loading Language \"" + this.configManager.getLanguage() + "\"...");
        String language = this.configManager.getLanguage();
        if (language == null) {
            Debug.warn("Language is not set!");
        }
        this.localizationService.addLanguage(language);

        Debug.info("Loading Default Language \"" + DEFAULT_LANGUAGE + "\"...");
        this.localizationService.addLanguage(DEFAULT_LANGUAGE);

        // Checking environment compatibility
        boolean isCompatible = environmentCheck();

        if (!isCompatible) {
            Debug.warning(getString("messages.startup.incompatible"));
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Debug.info(getString("messages.startup.trying-update"));
        tryUpdate();

        Debug.info(getString("messages.startup.loading-items"));
        SFWItemsSetup.setup(this);

        Debug.info(getString("messages.startup.registering-commands"));
        this.commandManager = new CommandManager(this);
        this.commandManager.onLoad();

        if (!commandManager.registerCommands()) {
            Debug.warning(getString("messages.startup.register-commands-failed"));
        }

        Debug.info(getString("messages.startup.done"));
    }

    @Override
    public void onDisable() {
        Preconditions.checkArgument(instance != null, "SFWorldedit has not been enabled yet！");

        SlimefunItemUtil.unregisterAllItems();

        if (this.commandManager != null) {
            this.commandManager.onUnload();
        }
        this.commandManager = null;

        final String message = getString("messages.shutdown.goodbye");
        this.localizationService = null;

        this.minecraftVersion = null;
        this.javaVersion = 0;

        if (this.configManager != null) {
            this.configManager.onUnload();
        }
        this.configManager = null;

        Debug.info(message);
        instance = null;
    }

    public void tryUpdate() {
        try {
            if (configManager.isAutoUpdate() && getDescription().getVersion().startsWith("Build")) {
                GuizhanUpdater.start(this, getFile(), username, repo, branch);
            }
        } catch (NoClassDefFoundError | NullPointerException e) {
            Debug.warning(getString("messages.startup.auto-update-failed"));
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            Debug.warning(getString("messages.startup.unsupported-guizhanlib-version"));
            Debug.trace(e);
        }
    }

    @Nonnull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Nullable
    @Override
    public String getBugTrackerURL() {
        return MessageFormat.format("https://github.com/{0}/{1}/issues/", this.username, this.repo);
    }

    public void debug(@Nonnull String message) {
        if (getConfigManager().isDebug()) {
            Debug.warning("[DEBUG] " + message);
        }
    }

    @Nonnull
    public String getVersion() {
        return getDescription().getVersion();
    }

    private boolean environmentCheck() {
        this.minecraftVersion = MinecraftVersion.getCurrentVersion();
        this.javaVersion = NumberUtils.getJavaVersion();
        if (minecraftVersion == null) {
            Debug.warning(getString("messages.startup.null-minecraft-version"));
            return false;
        }

        if (minecraftVersion == MinecraftVersion.UNKNOWN) {
            Debug.warning(getString("messages.startup.unknown-minecraft-version"));
        }

        if (!minecraftVersion.isAtLeast(RECOMMENDED_MC_VERSION)) {
            return false;
        }

        if (javaVersion < RECOMMENDED_JAVA_VERSION) {
            Debug.warning(getString("messages.startup.old-java-version", RECOMMENDED_JAVA_VERSION));
            return false;
        }

        return true;
    }

    @Nonnull
    @Override
    public LocalizationService getLocalizationService() {
        return getInstance().localizationService;
    }

    @Nonnull
    public NamespacedKey newKey(@Nonnull String key) {
        return new NamespacedKey(this, key);
    }
}