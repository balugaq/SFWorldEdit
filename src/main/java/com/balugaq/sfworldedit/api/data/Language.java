package com.balugaq.sfworldedit.api.data;

import com.balugaq.sfworldedit.utils.Debug;
import com.google.common.base.Preconditions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public final class Language {
    @Nonnull
    private final String lang;
    @Nonnull
    private final File currentFile;
    @Nonnull
    private final FileConfiguration currentConfig;

    @ParametersAreNonnullByDefault
    public Language(@Nonnull String lang, @Nonnull File currentFile, @Nonnull FileConfiguration defaultConfig) {
        Preconditions.checkArgument(lang != null, "Language key cannot be null");
        Preconditions.checkArgument(currentFile != null, "Current file cannot be null");
        Preconditions.checkArgument(defaultConfig != null, "default config cannot be null");
        this.lang = lang;
        this.currentFile = currentFile;
        this.currentConfig = YamlConfiguration.loadConfiguration(currentFile);
        this.currentConfig.setDefaults(defaultConfig);
        Iterator<String> iterator = defaultConfig.getKeys(true).iterator();

        while (iterator.hasNext()) {
            String key = iterator.next();
            if (!this.currentConfig.contains(key)) {
                this.currentConfig.set(key, defaultConfig.get(key));
            }
        }

        this.save();
    }

    @Nonnull
    public String getName() {
        return this.lang;
    }

    @Nonnull
    public FileConfiguration getLang() {
        return this.currentConfig;
    }

    public void save() {
        try {
            this.currentConfig.save(this.currentFile);
        } catch (IOException e) {
            Debug.trace(e);
        }

    }
}
