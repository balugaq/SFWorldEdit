package com.balugaq.sfworldedit.core.services;

import com.balugaq.sfworldedit.api.data.Language;
import com.balugaq.sfworldedit.utils.Debug;
import com.balugaq.sfworldedit.utils.TextUtil;
import com.google.common.base.Preconditions;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LocalizationService {
    private static final Map<String, String> CACHE = new HashMap<>();
    private static final String KEY_NAME = ".name";
    private static final String KEY_LORE = ".lore";
    private static final String MSG_KEY_NULL = "key cannot be null";
    private static final String MSG_ID_NULL = "id cannot be null";
    private static final String MSG_MATERIAL_NULL = "Material cannot be null";
    private static final String MSG_ITEMSTACK_NULL = "ItemStack cannot be null";
    private static final String MSG_TEXTURE_NULL = "Texture cannot be null";
    @Nonnull
    private final JavaPlugin plugin;
    @Nonnull
    private final String langFolderName;
    @Nonnull
    private final File langFolder;
    @Nonnull
    private final List<String> languages;
    @Nonnull
    private final Map<String, Language> langMap;
    private final String colorTagRegex = "<[a-zA-Z0-9_]+>";
    private final Pattern pattern = Pattern.compile(this.colorTagRegex);
    @Nonnull
    @Getter
    private String idPrefix = "";
    @Nonnull
    private String itemGroupKey = "categories";
    @Nonnull
    private String itemsKey = "items";
    @Nonnull
    private String recipesKey = "recipes";

    @ParametersAreNonnullByDefault
    public LocalizationService(@Nonnull JavaPlugin plugin) {
        this(plugin, "lang");
    }

    @ParametersAreNonnullByDefault
    public LocalizationService(@Nonnull JavaPlugin plugin, @Nonnull String folderName) {
        this.languages = new LinkedList<>();
        this.langMap = new LinkedHashMap<>();
        Preconditions.checkArgument(plugin != null, "The plugin instance should not be null");
        Preconditions.checkArgument(folderName != null, "The folder name should not be null");
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            boolean success = plugin.getDataFolder().mkdir();
            if (!success) {
                Debug.warn("Failed to create data folder for plugin " + plugin.getName());
            }
        }

        this.langFolderName = folderName;
        this.langFolder = new File(plugin.getDataFolder(), folderName);
        if (!this.langFolder.exists()) {
            boolean success = this.langFolder.mkdir();
            if (!success) {
                Debug.warn("Failed to create language folder for plugin " + plugin.getName());
            }
        }

    }

    @ParametersAreNonnullByDefault
    public LocalizationService(@Nonnull JavaPlugin plugin, @Nonnull String folderName, @Nonnull String langFile) {
        this(plugin, folderName);
        this.addLanguage(langFile);
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    public String getString(@Nonnull String key, @Nonnull Object... args) {
        return MessageFormat.format(getString(key), args);
    }

    @ParametersAreNonnullByDefault
    public void sendMessage(@Nonnull CommandSender sender, @Nonnull String messageKey, @Nonnull Object... args) {
        Preconditions.checkArgument(sender != null, "CommandSender cannot be null");
        Preconditions.checkArgument(messageKey != null, "Message key cannot be null");

        send(sender, MessageFormat.format(getString("messages." + messageKey), args));
    }

    public final void addLanguage(@Nonnull String langFilename) {
        Preconditions.checkArgument(langFilename != null, "The language file name should not be null");
        final File langFile = new File(this.langFolder, langFilename + ".yml");
        final String resourcePath = this.langFolderName + "/" + langFilename + ".yml";
        if (!langFile.exists()) {
            try {
                this.plugin.saveResource(resourcePath, false);
            } catch (IllegalArgumentException ignored) {
                this.plugin.getLogger().log(Level.SEVERE, "The default language file {0} does not exist in jar file!", resourcePath);
                return;
            }
        }

        this.languages.add(langFilename);
        final InputStreamReader defaultReader = new InputStreamReader(this.plugin.getResource(resourcePath), StandardCharsets.UTF_8);
        final FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultReader);
        this.langMap.put(langFilename, new Language(langFilename, langFile, defaultConfig));
    }

    public final void removeLanguage(@Nonnull String langFilename) {
        Preconditions.checkArgument(langFilename != null, "The language file name should not be null");

        this.languages.remove(langFilename);
        this.langMap.remove(langFilename);
    }

    @Nonnull
    public final List<String> getLanguages() {
        return new ArrayList<>(this.languages);
    }

    @Nonnull
    public String getString0(@Nonnull String path) {
        Preconditions.checkArgument(path != null, "path cannot be null");
        final String cached = CACHE.get(path);
        if (cached != null) {
            return cached;
        }

        final Iterator<String> languages = this.languages.iterator();

        String localization;
        do {
            if (!languages.hasNext()) {
                plugin.getLogger().severe("No localization found for path: " + path);
                return path;
            }

            final String lang = languages.next();
            localization = this.langMap.get(lang).getLang().getString(path);
        } while (localization == null);

        CACHE.put(path, localization);
        return localization;
    }

    @Nonnull
    public List<String> getStringList(@Nonnull String path) {
        Preconditions.checkArgument(path != null, "path cannot be null");
        final Iterator<String> languages = this.languages.iterator();

        List<String> localization;
        do {
            if (!languages.hasNext()) {
                plugin.getLogger().severe("No localization found for path: " + path);
                return new ArrayList<>();
            }

            final String lang = languages.next();
            localization = this.langMap.get(lang).getLang().getStringList(path);
        } while (localization.isEmpty());

        for (int i = 0; i < localization.size(); i++) {
            localization.set(i, color(localization.get(i)));
        }
        return localization;
    }

    @Nonnull
    public String[] getStringArray(@Nonnull String path) {
        return this.getStringList(path).stream().map(this::color).toList().toArray(new String[0]);
    }

    @Nonnull
    protected JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Nonnull
    public String getString(@Nonnull String path) {
        return color(this.getString0(path));
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    public SlimefunItemStack getItemBy(@Nonnull String key, @Nonnull String id, @Nonnull Material material, @Nonnull String... extraLore) {
        Preconditions.checkArgument(key != null, MSG_KEY_NULL);
        Preconditions.checkArgument(id != null, MSG_ID_NULL);
        Preconditions.checkArgument(material != null, MSG_MATERIAL_NULL);
        SlimefunItemStack item = new SlimefunItemStack((this.idPrefix + id).toUpperCase(Locale.ROOT), material, this.getString(key + "." + id + KEY_NAME), this.getStringArray(key + "." + id + KEY_LORE));
        if (extraLore != null && extraLore.length != 0) {
            appendLore(item, extraLore);
        }
        return item;
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    public SlimefunItemStack getItemBy(@Nonnull String key, @Nonnull String id, @Nonnull String texture, @Nonnull String... extraLore) {
        Preconditions.checkArgument(key != null, MSG_KEY_NULL);
        Preconditions.checkArgument(id != null, MSG_ID_NULL);
        Preconditions.checkArgument(texture != null, MSG_TEXTURE_NULL);
        return appendLore(new SlimefunItemStack((this.idPrefix + id).toUpperCase(Locale.ROOT), texture, this.getString(key + "." + id + ".name"), this.getStringArray(key + "." + id + ".lore")), extraLore);
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    public SlimefunItemStack getItemBy(@Nonnull String key, @Nonnull String id, @Nonnull ItemStack itemStack, @Nonnull String... extraLore) {
        Preconditions.checkArgument(key != null, MSG_KEY_NULL);
        Preconditions.checkArgument(id != null, MSG_ID_NULL);
        Preconditions.checkArgument(itemStack != null, MSG_ITEMSTACK_NULL);
        return appendLore(new SlimefunItemStack((this.idPrefix + id).toUpperCase(Locale.ROOT), itemStack, this.getString(key + "." + id + ".name"), this.getStringArray(key + "." + id + ".lore")), extraLore);
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    public SlimefunItemStack getItemGroupItem(@Nonnull String id, @Nonnull Material material) {
        return this.getItemBy(this.itemGroupKey, id, material);
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    public SlimefunItemStack getItemGroupItem(@Nonnull String id, @Nonnull String texture) {
        return this.getItemBy(this.itemGroupKey, id, texture);
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    public SlimefunItemStack getItemGroupItem(@Nonnull String id, @Nonnull ItemStack itemStack) {
        return this.getItemBy(this.itemGroupKey, id, itemStack);
    }

    @Nonnull
    public SlimefunItemStack getItem(@Nonnull String id, @Nonnull Material material, @Nonnull String... extraLore) {
        return this.getItemBy(this.itemsKey, id, material, extraLore);
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    public SlimefunItemStack getItem(@Nonnull String id, @Nonnull String texture, @Nonnull String... extraLore) {
        return this.getItemBy(this.itemsKey, id, texture, extraLore);
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    public SlimefunItemStack getItem(@Nonnull String id, @Nonnull ItemStack itemStack, @Nonnull String... extraLore) {
        return this.getItemBy(this.itemsKey, id, itemStack, extraLore);
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    public RecipeType getRecipeType(@Nonnull String id, @Nonnull Material material, @Nonnull String... extraLore) {
        return new RecipeType(new NamespacedKey(this.getPlugin(), id), this.getItemBy(this.recipesKey, id, material, extraLore));
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    public RecipeType getRecipeType(@Nonnull String id, @Nonnull String texture, @Nonnull String... extraLore) {
        return new RecipeType(new NamespacedKey(this.getPlugin(), id), this.getItemBy(this.recipesKey, id, texture, extraLore));
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    public RecipeType getRecipeType(@Nonnull String id, @Nonnull ItemStack itemStack, @Nonnull String... extraLore) {
        return new RecipeType(new NamespacedKey(this.getPlugin(), id), this.getItemBy(this.recipesKey, id, itemStack, extraLore));
    }

    public void setIdPrefix(@Nonnull String idPrefix) {
        this.idPrefix = idPrefix;
    }

    public void setItemGroupKey(@Nonnull String itemGroupKey) {
        this.itemGroupKey = itemGroupKey;
    }

    public void setItemsKey(@Nonnull String itemsKey) {
        this.itemsKey = itemsKey;
    }

    public void setRecipesKey(@Nonnull String recipesKey) {
        this.recipesKey = recipesKey;
    }

    @Nonnull
    private <T extends ItemStack> T appendLore(@Nonnull T itemStack, @Nullable String... extraLore) {
        Preconditions.checkArgument(itemStack != null, MSG_ITEMSTACK_NULL);
        if (extraLore != null && extraLore.length != 0) {
            final ItemMeta meta = itemStack.getItemMeta();
            final List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.addAll(color(Arrays.asList(extraLore)));
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    @Nonnull
    public String color(@Nonnull String str) {
        str = ChatColor.translateAlternateColorCodes('&', str);
        if (str.startsWith("<random_color>")) {
            str = str.replaceAll("<random_color>", "");
            str = TextUtil.colorPseudorandomString(str);
        }

        if (str.startsWith("<color_random_string>")) {
            str = str.replaceAll("<color_random_string>", "");
            str = TextUtil.colorRandomString(str);
        }

        return str;
    }

    @Nonnull
    public List<String> color(@Nonnull List<String> strList) {
        Preconditions.checkArgument(strList != null, "String list cannot be null");
        return strList.stream().map(this::color).collect(Collectors.toList());
    }

    @ParametersAreNonnullByDefault
    public void send(@Nonnull CommandSender sender, @Nonnull String messageKey, @Nonnull Object... args) {
        sender.sendMessage(color(MessageFormat.format(getString("messages." + messageKey), args)));
    }

    @ParametersAreNonnullByDefault
    public void sendList(@Nonnull CommandSender sender, @Nonnull String messageKey) {
        final List<String> list = getStringList(messageKey);
        if (list == null || list.isEmpty()) {
            return;
        }

        for (String line : list) {
            sender.sendMessage(color(line));
        }
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    public ItemStack getItemStack(@Nonnull String key, @Nonnull Material material) {
        return new CustomItemStack(material, this.getString(key + KEY_NAME), this.getStringArray(key + KEY_LORE));
    }
}