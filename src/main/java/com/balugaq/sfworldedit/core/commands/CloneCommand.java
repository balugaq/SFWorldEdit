package com.balugaq.sfworldedit.core.commands;

import com.balugaq.sfworldedit.api.objects.SubCommand;
import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.utils.CommandUtil;
import com.balugaq.sfworldedit.utils.PermissionUtil;
import com.balugaq.sfworldedit.utils.WorldUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.ChunkPosition;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class CloneCommand extends SubCommand {
    private static final String KEY = "clone";
    private final List<String> FLAGS = List.of("override");
    private final ISFWorldEdit plugin;

    public CloneCommand(@Nonnull ISFWorldEdit plugin) {
        this.plugin = plugin;
    }

    @Override
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
            return false;
        }

        if (!Objects.equals(pos1.getWorld().getUID(), pos2.getWorld().getUID())) {
            plugin.send(player, "error.world-mismatch");
            return false;
        }

        final long range = WorldUtils.getRange(pos1, pos2);
        final long max = plugin.getConfigManager().getModificationBlockLimit();
        if (range > max) {
            plugin.send(commandSender, "error.too-many-blocks", range, max);
            return false;
        }

        plugin.send(player, "command.clone.start", WorldUtils.locationToString(pos1), WorldUtils.locationToString(pos2));
        final long currentMillSeconds = System.currentTimeMillis();

        final boolean override = CommandUtil.hasFlag(args, "override") || CommandUtil.hasFlag(args, "o");
        final AtomicInteger count = new AtomicInteger();
        final Location playerLocation = player.getLocation();
        final ItemStack itemInHand = player.getInventory().getItemInMainHand();

        final int dx = playerLocation.getBlockX() - pos1.getBlockX();
        final int dy = playerLocation.getBlockY() - pos1.getBlockY();
        final int dz = playerLocation.getBlockZ() - pos1.getBlockZ();

        final Map<ChunkPosition, Set<Location>> tickingBlocks = Slimefun.getTickerTask().getLocations();

        WorldUtils.doWorldEdit(pos1, pos2, (fromLocation -> {
            final Block fromBlock = fromLocation.getBlock();
            final Block toBlock = playerLocation.getWorld().getBlockAt(fromLocation.getBlockX() + dx, fromLocation.getBlockY() + dy, fromLocation.getBlockZ() + dz);
            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(fromLocation);
            final Location toLocation = toBlock.getLocation();

            // Block Data
            WorldUtils.copyBlockState(fromBlock.getState(), toBlock);

            // Count means successful pasting block data. Not including Slimefun data.
            count.addAndGet(1);

            // Slimefun Data
            if (slimefunItem == null) {
                return;
            }

            // Call Handler
            slimefunItem.callItemHandler(BlockPlaceHandler.class, handler -> handler.onPlayerPlace(
                    new BlockPlaceEvent(
                            toBlock,
                            toBlock.getState(),
                            toBlock.getRelative(BlockFace.SOUTH),
                            itemInHand,
                            player,
                            true,
                            EquipmentSlot.HAND
                    )
            ));

            final SlimefunBlockData fromSlimefunBlockData = Slimefun.getDatabaseManager().getBlockDataController().getBlockData(fromLocation);
            if (override) {
                Slimefun.getDatabaseManager().getBlockDataController().removeBlock(toLocation);
            }

            boolean ticking = false;
            final ChunkPosition chunkPosition = new ChunkPosition(fromLocation);
            if (tickingBlocks.containsKey(chunkPosition)) {
                if (tickingBlocks.get(chunkPosition).contains(fromLocation)) {
                    ticking = true;
                }
            }

            if (StorageCacheUtils.hasBlock(toLocation)) {
                return;
            }

            // Slimefun Block
            Slimefun.getDatabaseManager().getBlockDataController().createBlock(toLocation, slimefunItem.getId());
            final SlimefunBlockData toSlimefunBlockData = Slimefun.getDatabaseManager().getBlockDataController().getBlockData(toLocation);

            // SlimefunBlockData
            if (fromSlimefunBlockData == null || toSlimefunBlockData == null) {
                return;
            }

            final Map<String, String> data = fromSlimefunBlockData.getAllData();
            for (String key : data.keySet()) {
                toSlimefunBlockData.setData(key, data.get(key));
            }

            // BlockMenu
            final BlockMenu fromMenu = fromSlimefunBlockData.getBlockMenu();
            final BlockMenu toMenu = toSlimefunBlockData.getBlockMenu();

            if (fromMenu == null || toMenu == null) {
                return;
            }

            final ItemStack[] contents = fromMenu.getContents();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null) {
                    toMenu.getInventory().setItem(i, contents[i].clone());
                }
            }

            // Ticking
            if (!ticking) {
                Slimefun.getTickerTask().disableTicker(toLocation);
            }
        }));

        plugin.send(player, "command.clone.success", count.get(), System.currentTimeMillis() - currentMillSeconds);

        return true;
    }

    @Override
    @Nonnull
    public String getKey() {
        return KEY;
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (args.length == 0) {
            List<String> left = new ArrayList<>();
            for (String flag : FLAGS) {
                if (!CommandUtil.hasFlag(args, flag)) {
                    left.add(flag);
                }
            }
            return left;
        }
        return new ArrayList<>();
    }
}
