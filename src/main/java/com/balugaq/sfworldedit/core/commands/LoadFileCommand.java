package com.balugaq.sfworldedit.core.commands;

import com.balugaq.sfworldedit.api.data.BukkitContent;
import com.balugaq.sfworldedit.api.data.Content;
import com.balugaq.sfworldedit.api.data.SFContent;
import com.balugaq.sfworldedit.api.objects.SubCommand;
import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.utils.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoadFileCommand extends SubCommand {
    private static final String KEY = "loadFile";
    @Nonnull
    private final ISFWorldEdit plugin;

    public LoadFileCommand(@Nonnull ISFWorldEdit plugin) {
        this.plugin = plugin;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static Content deserializeContent(@Nonnull ConfigurationSection c, @Nonnull Location location, @Nonnull Map<Integer, String> hashBackup) throws IOException, ClassNotFoundException {
        int type = c.getInt("type");
        Material material = null;
        BlockData blockData = null;
        Map<Integer, ItemStack> container = new HashMap<>();
        if ((type & BukkitContent.getIdentifier()) == BukkitContent.getIdentifier()) {
            blockData = Bukkit.createBlockData(c.getString("blockData"));
            material = blockData.getMaterial();
            if (c.contains("format")) {
                Map<Integer, List<Integer>> format = getMapIntListInt(c.getConfigurationSection("format"));
                Map<Integer, Integer> amountMap = getMapIntInt(c.getConfigurationSection("amountMap"));
                for (Map.Entry<Integer, List<Integer>> entry : format.entrySet()) {
                    String base64Str = hashBackup.get(entry.getKey());
                    ItemStack itemStack = getObject(base64Str);
                    List<Integer> slots = entry.getValue();
                    for (int slot : slots) {
                        container.put(slot, itemStack.clone());
                        itemStack.setAmount(amountMap.get(slot));
                    }
                }
            }
        }

        Map<String, String> data = new HashMap<>();
        Map<Integer, ItemStack> menu = new HashMap<>();
        String id = null;
        boolean ticking = false;
        if ((type & SFContent.getIdentifier()) == SFContent.getIdentifier()) {
            data = c.getObject("data", Map.class);

            Map<Integer, List<Integer>> format = getMapIntListInt(c.getConfigurationSection("format"));
            Map<Integer, Integer> amountMap = getMapIntInt(c.getConfigurationSection("amountMap"));
            for (Map.Entry<Integer, List<Integer>> entry : format.entrySet()) {
                String base64Str = hashBackup.get(entry.getKey());
                ItemStack itemStack = getObject(base64Str);
                for (int slot : entry.getValue()) {
                    menu.put(slot, itemStack.clone());
                    itemStack.setAmount(amountMap.get(slot));
                }
            }

            id = c.getString("id");
            ticking = c.getBoolean("ticking");
        }

        if (id != null) {
            Block block = location.getBlock();
            block.setType(material);
            BlockState state = block.getState();
            state.setBlockData(blockData);
            return new SFContent(location, state, id, menu, data, ticking);
        } else {
            Block block = location.getBlock();
            Material originType = block.getType();
            block.setType(material);
            BlockState state = block.getState();
            state.setBlockData(blockData);
            if (state instanceof Container statec) {
                Inventory inventory = statec.getInventory();
                for (Map.Entry<Integer, ItemStack> entry : container.entrySet()) {
                    inventory.setItem(entry.getKey(), entry.getValue());
                }
            }
            block.setType(originType);
            return new BukkitContent(location, state);
        }
    }

    @Nonnull
    public static String getBase64(Object object) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BukkitObjectOutputStream bs = new BukkitObjectOutputStream(stream);
        bs.writeObject(object);

        bs.close();
        return Base64Coder.encodeLines(stream.toByteArray());
    }

    public static <T> T getObject(@Nonnull String base64Str) throws IOException, ClassNotFoundException {
        ByteArrayInputStream stream = new ByteArrayInputStream(Base64Coder.decodeLines(base64Str));
        BukkitObjectInputStream bs = new BukkitObjectInputStream(stream);
        @SuppressWarnings("unchecked") T re = (T) bs.readObject();
        bs.close();
        return re;
    }

    @Nonnull
    public static Map<Integer, Integer> getMapIntInt(@Nonnull ConfigurationSection c) {
        Set<String> keys = c.getKeys(false);
        Map<Integer, Integer> map = new HashMap<>();
        for (String key : keys) {
            map.put(Integer.parseInt(key), c.getInt(key));
        }
        return new HashMap<>(map);
    }

    @Nonnull
    public static Map<Integer, List<Integer>> getMapIntListInt(@Nonnull ConfigurationSection c) {
        Set<String> keys = c.getKeys(false);
        Map<Integer, List<Integer>> map = new HashMap<>();
        for (String key : keys) {
            List<Integer> list = c.getIntegerList(key);
            map.put(Integer.parseInt(key), list);
        }
        return new HashMap<>(map);
    }

    @Nonnull
    public static Map<Integer, String> getMapIntString(@Nonnull ConfigurationSection c) {
        Set<String> keys = c.getKeys(false);
        Map<Integer, String> map = new HashMap<>();
        for (String key : keys) {
            map.put(Integer.parseInt(key), c.getString(key));
        }
        return new HashMap<>(map);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (!PermissionUtil.hasPermission(commandSender, this)) {
            plugin.send(commandSender, "error.no-permission");
            return false;
        }

        if (!(commandSender instanceof Player player)) {
            plugin.send(commandSender, "error.player-only");
            return false;
        }

        if (args.length < 1) {
            plugin.send(player, "error.missing-argument", "<file_name>");
            return false;
        }
        String fileName = args[0];
        String filePath = plugin.getDataFolder() + "/clones/" + fileName + ".yml";
        ConfigurationSection c = YamlConfiguration.loadConfiguration(new File(filePath));
        if (c == null || c.getKeys(false).isEmpty()) {
            plugin.send(player, "error.file-not-found");
            return false;
        }

        Map<Integer, String> hashBackup = getMapIntString(c.getConfigurationSection("hashBackup"));
        ConfigurationSection content = c.getConfigurationSection("content");
        String worldName = c.getString("world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.send(player, "error.world-not-found");
            return false;
        }

        for (String key : content.getKeys(false)) {
            String[] locArr = key.split("_");
            int x = Integer.parseInt(locArr[0]);
            int y = Integer.parseInt(locArr[1]);
            int z = Integer.parseInt(locArr[2]);
            Location location = new Location(world, x, y, z);
            ConfigurationSection part = content.getConfigurationSection(key);
            try {
                Content contentObj = deserializeContent(part, location, hashBackup);
                contentObj.action();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                plugin.send(player, "error.deserialization-error");
            }
        }


        return true;
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        return new ArrayList<>();
    }

    @Override
    @Nonnull
    public String getKey() {
        return KEY;
    }
}
