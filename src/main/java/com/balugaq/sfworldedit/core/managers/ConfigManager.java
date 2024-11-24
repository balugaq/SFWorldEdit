package com.balugaq.sfworldedit.core.managers;

import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ConfigManager implements IManager {
    private final ISFWorldEdit plugin;

    public ConfigManager(ISFWorldEdit plugin) {
        this.plugin = plugin;
        setupDefaultConfig();
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
            e.printStackTrace();
        }
    }

    @ParametersAreNonnullByDefault
    private void checkKey(FileConfiguration existingConfig, FileConfiguration resourceConfig, String key) {
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

    public String getLanguage() {
        return plugin.getConfig().getString("language");
    }

    public void saveAll() {
        plugin.getLogger().info(plugin.getLocalizationService().getString("messages.save-all"));
    }
}
