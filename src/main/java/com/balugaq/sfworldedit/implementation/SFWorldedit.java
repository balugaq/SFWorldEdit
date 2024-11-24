package com.balugaq.sfworldedit.implementation;

import com.balugaq.sfworldedit.api.objects.enums.MinecraftVersion;
import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.core.managers.CommandManager;
import com.balugaq.sfworldedit.core.managers.ConfigManager;
import com.balugaq.sfworldedit.core.services.LocalizationService;
import com.google.common.base.Preconditions;
import io.github.thebusybiscuit.slimefun4.utils.NumberUtils;
import lombok.Getter;
import net.guizhanss.guizhanlibplugin.updater.GuizhanUpdater;
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
public class SFWorldedit extends ISFWorldEdit {
    private static final int RECOMMENDED_JAVA_VERSION = 17;
    private static final MinecraftVersion RECOMMENDED_MC_VERSION = MinecraftVersion.MINECRAFT_1_16;
    private static SFWorldedit instance;
    private final String username;
    private final String repo;
    private final String branch;
    private CommandManager commandManager;
    private ConfigManager configManager;
    private LocalizationService localizationService;
    private MinecraftVersion minecraftVersion;
    private int javaVersion;


    public SFWorldedit() {
        this.username = "balugaq";
        this.repo = "SFWorldedit";
        this.branch = "master";
    }

    @Nonnull
    public CommandManager getCommandManager() {
        return getInstance().commandManager;
    }

    @Nonnull
    public static ConfigManager getConfigManager() {
        return getInstance().configManager;
    }

    @Nonnull
    public static MinecraftVersion getMinecraftVersion() {
        return getInstance().minecraftVersion;
    }

    public static SFWorldedit getInstance() {
        Preconditions.checkArgument(instance != null, "SFWorldedit has not been enabled yet！");
        return SFWorldedit.instance;
    }

    @Override
    public void onEnable() {
        Preconditions.checkArgument(instance == null, "SFWorldEdit already has been enabled！");
        instance = this;

        getLogger().info("Loading Config Manager...");
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);

        getLogger().info("正在加载语言文件...");
        this.localizationService = new LocalizationService(this);

        // Checking environment compatibility
        boolean isCompatible = environmentCheck();

        if (!isCompatible) {
            getLogger().warning("环境不兼容！插件已被禁用！");
            onDisable();
            return;
        }

        getLogger().info("尝试自动更新...");
        tryUpdate();

        getLogger().info("正在注册指令");
        this.commandManager = new CommandManager(this);
        this.commandManager.onLoad();

        if (!commandManager.registerCommands()) {
            getLogger().warning("注册指令失败！");
        }
        
        getLogger().info("成功启用此附属");
    }

    @Override
    public void onDisable() {
        Preconditions.checkArgument(instance != null, "SFWorldedit has not been enabled yet！");

        // Managers
        this.commandManager.onUnload();
        this.configManager.onUnload();
        
        this.commandManager = null;
        this.configManager = null;

        // Other fields
        this.minecraftVersion = null;
        this.javaVersion = 0;

        // Clear instance
        instance = null;
        getLogger().info("成功禁用此附属");
    }

    public void tryUpdate() {
        try {
            if (getConfigManager().isAutoUpdate() && getDescription().getVersion().startsWith("Build")) {
                GuizhanUpdater.start(this, getFile(), username, repo, branch);
            }
        } catch (UnsupportedClassVersionError ignored) {
            getLogger().warning("自动更新失败！");
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
            getLogger().warning("[DEBUG] " + message);
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
            getLogger().warning("无法获取到 Minecraft 版本！");
            return false;
        }

        if (minecraftVersion == MinecraftVersion.UNKNOWN) {
            getLogger().warning("无法识别到 Minecraft 版本！请谨慎使用此插件！");
        }

        if (!minecraftVersion.isAtLeast(RECOMMENDED_MC_VERSION)) {
            return false;
        }

        if (javaVersion < RECOMMENDED_JAVA_VERSION) {
            getLogger().warning("Java 版本过低，请使用 Java " + RECOMMENDED_JAVA_VERSION + " 或以上版本！");
            return false;
        }

        return true;
    }

    @Override
    public LocalizationService getLocalizationService() {
        return getInstance().localizationService;
    }
}