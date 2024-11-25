package com.balugaq.sfworldedit.core.commands;

import com.balugaq.sfworldedit.api.objects.SubCommand;
import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.utils.CommandUtil;
import com.balugaq.sfworldedit.utils.PermissionUtil;
import com.balugaq.sfworldedit.utils.WorldUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
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

public class BlockMenuSlotSet extends SubCommand {
    private static final String KEY = "blockmenuslotset";
    private final ISFWorldEdit plugin;

    public BlockMenuSlotSet(@Nonnull ISFWorldEdit plugin) {
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

        Location pos1 = plugin.getCommandManager().getPos1(player.getUniqueId());
        Location pos2 = plugin.getCommandManager().getPos2(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            plugin.send(commandSender, "error.no-selection");
            return false;
        }

        if (!Objects.equals(pos1.getWorld().getUID(), pos2.getWorld().getUID())) {
            plugin.send(commandSender, "error.world-mismatch");
            return false;
        }

        if (!CommandUtil.hasArgFlag(args, "slot")) {
            plugin.send(player, "error.missing-argument", "slot");
            return false;
        }

        String s = CommandUtil.getArgFlag(args, "slot");
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

        ItemStack hand = player.getInventory().getItemInMainHand();

        plugin.send(player, "command.blockmenuslotset.start", WorldUtils.locationToString(pos1), WorldUtils.locationToString(pos2));

        final long currentMillSeconds = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger();
        WorldUtils.doWorldEdit(pos1, pos2, (location -> {
            final BlockMenu menu = StorageCacheUtils.getMenu(location);
            if (menu != null) {
                menu.replaceExistingItem(slot, hand);
            }
            count.addAndGet(1);
        }));

        plugin.send(player, "command.blockmenuslotset.success", count, System.currentTimeMillis() - currentMillSeconds);
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
