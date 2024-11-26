package com.balugaq.sfworldedit.core.managers;

import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.utils.Debug;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ConfigManager implements IManager {
    public static final long DEFAULT_MODIFICATION_BLOCK_LIMIT = 32768;
    private long cachedModificationBlockLimit = -1;
    private ISFWorldEdit plugin;

    public ConfigManager(@Nonnull ISFWorldEdit plugin) {
        this.plugin = plugin;
    }

    private void setupDefaultConfig() {
        // config.yml
        final InputStream inputStream = this.plugin.getResource("config.yml");
        final File existingFile = new File(this.plugin.getDataFolder(), "config.yml");

        if (inputStream == null) {
            return;
        }

        final Reader reader = new InputStreamReader(inputStream);
        final FileConfiguration resourceConfig = YamlConfiguration.loadConfiguration(reader);
        final FileConfiguration existingConfig = YamlConfiguration.loadConfiguration(existingFile);

        for (String key : resourceConfig.getKeys(false)) {
            checkKey(existingConfig, resourceConfig, key);
        }

        try {
            existingConfig.save(existingFile);
        } catch (IOException e) {
            Debug.trace(e);
        }
    }

    @ParametersAreNonnullByDefault
    private void checkKey(@Nonnull FileConfiguration existingConfig, @Nonnull FileConfiguration resourceConfig, @Nonnull String key) {
        final Object currentValue = existingConfig.get(key);
        final Object newValue = resourceConfig.get(key);
        if (newValue instanceof ConfigurationSection section) {
            for (String sectionKey : section.getKeys(false)) {
                checkKey(existingConfig, resourceConfig, key + "." + sectionKey);
            }
        } else if (currentValue == null) {
            existingConfig.set(key, newValue);
        }
    }

    public boolean isAutoUpdate() {
        return plugin.getConfig().getBoolean("auto-update");
    }

    public boolean isDebug() {
        return plugin.getConfig().getBoolean("debug");
    }

    public long getModificationBlockLimit() {
        if (cachedModificationBlockLimit != -1) {
            return cachedModificationBlockLimit;
        }

        String s = plugin.getConfig().getString("worldedit.modification-block-limit");
        try {
            long modificationBlockLimit = Long.parseLong(s);

            if (modificationBlockLimit < 1 || modificationBlockLimit > Long.MAX_VALUE) {
                modificationBlockLimit = DEFAULT_MODIFICATION_BLOCK_LIMIT;
                Debug.severe("Invalid modification block limit: " + s + ", using default value: " + DEFAULT_MODIFICATION_BLOCK_LIMIT);
            }

            cachedModificationBlockLimit = modificationBlockLimit;
        } catch (NumberFormatException e) {
            cachedModificationBlockLimit = DEFAULT_MODIFICATION_BLOCK_LIMIT;
            Debug.severe("Invalid modification block limit: " + s + ", using default value: " + DEFAULT_MODIFICATION_BLOCK_LIMIT);
        }

        return cachedModificationBlockLimit;
    }

    @Nullable
    public String getLanguage() {
        return plugin.getConfig().getString("language");
    }

    public void saveAll() {
        plugin.getLogger().info(plugin.getLocalizationService().getString("messages.save-all"));
    }

    @Override
    public void onLoad() {
        setupDefaultConfig();
    }

    @Override
    public void onUnload() {
        this.plugin = null;
    }
}
