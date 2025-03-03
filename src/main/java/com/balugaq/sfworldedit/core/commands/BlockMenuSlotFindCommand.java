package com.balugaq.sfworldedit.core.commands;

import com.balugaq.sfworldedit.api.objects.SubCommand;
import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.utils.ClipboardUtil;
import com.balugaq.sfworldedit.utils.PermissionUtil;
import com.balugaq.sfworldedit.utils.WorldUtils;
import com.balugaq.sfworldedit.utils.YamlWriter;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockMenuSlotFindCommand extends SubCommand {
    private static final String KEY = "blockmenuslotfind";
    @Nonnull
    private final ISFWorldEdit plugin;

    public BlockMenuSlotFindCommand(@Nonnull ISFWorldEdit plugin) {
        this.plugin = plugin;
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

        final Location pos1 = plugin.getCommandManager().getPos1(player.getUniqueId());
        final Location pos2 = plugin.getCommandManager().getPos2(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            plugin.send(commandSender, "error.no-selection");
            return false;
        }

        if (!Objects.equals(pos1.getWorld().getUID(), pos2.getWorld().getUID())) {
            plugin.send(commandSender, "error.world-mismatch");
            return false;
        }

        final long range = WorldUtils.getRange(pos1, pos2);
        final long max = plugin.getConfigManager().getModificationBlockLimit();
        if (range > max) {
            plugin.send(commandSender, "error.too-many-blocks", range, max);
            return false;
        }

        if (args.length == 0) {
            plugin.send(player, "error.missing-argument", "slot");
            return false;
        }

        final String s = args[0];
        if (s == null) {
            plugin.send(player, "error.missing-argument", "slot");
            return false;
        }

        int slot;
        try {
            slot = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            plugin.send(player, "error.invalid-argument", s);
            return false;
        }

        if (slot < 0 || slot > 53) {
            plugin.send(player, "error.invalid-argument", s);
            return false;
        }

        final ItemStack hand = player.getInventory().getItemInMainHand();

        plugin.send(player, "command.blockmenuslotfind.start", WorldUtils.locationToString(pos1), WorldUtils.locationToString(pos2));

        final long currentMillSeconds = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger();
        final YamlWriter writer = new YamlWriter();
        writer.setRoot("failed-locations");
        final AtomicInteger index = new AtomicInteger(0);
        WorldUtils.doWorldEdit(player, pos1, pos2, (location -> {
            final BlockMenu menu = StorageCacheUtils.getMenu(location);
            if (menu != null) {
                if (slot < menu.getSize()) {
                    if (SlimefunUtils.isItemSimilar(menu.getItemInSlot(slot), hand, true, true, true, true)) {
                        count.addAndGet(1);
                    } else {
                        writer.set("" + index.getAndIncrement(), location);
                    }
                }
            }
        }), () -> {
            plugin.send(player, "command.blockmenuslotfind.success", count.get(), System.currentTimeMillis() - currentMillSeconds);
            ClipboardUtil.send(player, writer.toString());
        });

        return true;
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (!PermissionUtil.hasPermission(commandSender, this)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53");
        }

        return new ArrayList<>();
    }

    @Override
    @Nonnull
    public String getKey() {
        return KEY;
    }
}
