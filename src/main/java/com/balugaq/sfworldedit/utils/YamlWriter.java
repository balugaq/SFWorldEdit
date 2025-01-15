package com.balugaq.sfworldedit.utils;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlRepresenter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.yaml.snakeyaml.DumperOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Random;

@Getter
public class YamlWriter {
    private static final Random random = new Random();
    private String root;
    private File file;
    private YamlConfiguration configuration;

    public YamlWriter() {
        configuration = new YamlConfiguration();
        try {
            ((DumperOptions) ReflectionUtil.getValue(configuration, "yamlDumperOptions")).setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        } catch (Throwable ignored) {
        }
        try {
            ((YamlRepresenter) ReflectionUtil.getValue(configuration, "representer")).setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        } catch (Throwable ignored) {
        }
    }

    @CanIgnoreReturnValue
    public @Nonnull YamlWriter setFile(@Nonnull File file) {
        this.file = file;
        this.configuration = YamlConfiguration.loadConfiguration(file);
        return this;
    }

    @CanIgnoreReturnValue
    public @Nonnull YamlWriter setRoot(@Nonnull String root) {
        this.root = root;
        return this;
    }

    @CanIgnoreReturnValue
    public @Nonnull YamlWriter set(String key, @Nullable ItemStack itemStack) {
        return set(key, itemStack, true);
    }

    @CanIgnoreReturnValue
    public @Nonnull YamlWriter set(String key, @Nullable ItemStack itemStack, boolean model) {
        if (itemStack == null) {
            return this;
        }

        if (itemStack.getType() == Material.AIR) {
            configuration.set(getKey(key + ".material_type"), "none");
            return this;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
        if (model) {
            if (itemMeta.hasCustomModelData()) {
                int modelData = itemMeta.getCustomModelData();
                configuration.set(getKey(key + ".modelId"), modelData);
            }

            if (itemMeta.hasLore()) {
                List<String> lore = itemMeta.getLore();
                if (lore != null && !lore.isEmpty()) {
                    configuration.set(getKey(key + ".lore"), lore.toArray(new String[0]));
                }
            }

            if (itemMeta.hasDisplayName()) {
                configuration.set(getKey(key + ".name"), itemMeta.getDisplayName());
            }
        }

        if (slimefunItem != null) {
            if (!slimefunItem.getId().equals("LOGITECH_SAMPLE_HEAD")) {
                configuration.set(getKey(key + ".material_type"), "slimefun");
                configuration.set(getKey(key + ".material"), slimefunItem.getId());
                configuration.set(getKey(key + ".amount"), itemStack.getAmount());
                return this;
            }
        }

        configuration.set(getKey(key + ".amount"), itemStack.getAmount());

        if (itemStack.getType() == Material.PLAYER_HEAD || itemStack.getType() == Material.PLAYER_WALL_HEAD) {
            if (itemMeta instanceof SkullMeta skullMeta) {
                try {
                    URL url = skullMeta.getOwnerProfile().getTextures().getSkin();
                    String path = url.getPath();
                    String[] parts = path.split("/");
                    String hash = parts[parts.length - 1];

                    configuration.set(getKey(key + ".material_type"), "skull_hash");
                    configuration.set(getKey(key + ".material"), hash);
                } catch (Throwable ignored) {
                }
            } else {
                configuration.set(getKey(key + ".material_type"), "mc");
                configuration.set(getKey(key + ".material"), itemStack.getType().name());
            }
        } else {
            configuration.set(getKey(key + ".material_type"), "mc");
            configuration.set(getKey(key + ".material"), itemStack.getType().name());
        }

        return this;
    }

    @CanIgnoreReturnValue
    public @Nonnull YamlWriter set(@Nonnull String key, Object value) {
        configuration.set(getKey(key), value);
        return this;
    }

    public @Nonnull String getKey(@Nonnull String key) {
        if (root == null) {
            return key;
        }

        return root + "." + key;
    }

    public @Nonnull String toString() {
        return configuration.saveToString();
    }

    public void save() {
        try {
            configuration.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
