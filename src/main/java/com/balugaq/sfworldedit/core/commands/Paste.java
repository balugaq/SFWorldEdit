package com.balugaq.sfworldedit.core.commands;

import com.balugaq.sfworldedit.api.objects.SubCommand;
import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.utils.CommandUtil;
import com.balugaq.sfworldedit.utils.PermissionUtil;
import com.balugaq.sfworldedit.utils.WorldUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerHead;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerSkin;
import org.bukkit.Location;
import org.bukkit.Material;
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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Paste extends SubCommand {
    private static final String KEY = "paste";
    private static final List<String> FLAGS = List.of("override", "force");
    private final ISFWorldEdit plugin;
    public Paste(@Nonnull ISFWorldEdit plugin) {
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

        if (args.length < 1) {
            plugin.send(commandSender, "help.command.paste");
            return false;
        }

        final String sfid = args[0];
        final boolean override = CommandUtil.hasFlag(args, "override") || CommandUtil.hasFlag(args, "o");
        final boolean force = CommandUtil.hasFlag(args, "force") || CommandUtil.hasFlag(args, "f");
        final SlimefunItem sfItem = SlimefunItem.getById(sfid);

        Location pos1 = plugin.getCommandManager().getPos1(player.getUniqueId());
        Location pos2 = plugin.getCommandManager().getPos2(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            plugin.send(commandSender, "error.no-selection");
            return false;
        }

        if (!Objects.equals(pos1.getWorld().getUID(), pos2.getWorld().getUID())) {
            plugin.send(player, "error.world-mismatch");
            return false;
        }

        if (sfItem == null) {
            plugin.send(player, "error.invalid-slimefun-block-id");
            return false;
        }

        if (!sfItem.getItem().getType().isBlock()) {
            plugin.send(player, "error.block-only");
            return false;
        }

        if (sfItem.getItem().getType() == Material.AIR) {
            plugin.send(player, "error.air-block");
            return false;
        }

        if (!force && sfItem instanceof NotPlaceable) {
            plugin.send(player, "error.not-placeable");
            return false;
        }

        plugin.send(player, "command.paste.start", WorldUtils.locationToString(pos1), WorldUtils.locationToString(pos2));

        final long currentMillSeconds = System.currentTimeMillis();

        final AtomicInteger count = new AtomicInteger();
        final Material t = sfItem.getItem().getType();
        final ItemStack itemStack = sfItem.getItem();
        PlayerSkin skin0 = null;
        boolean isHead0 = false;
        final PlayerSkin skin;
        final boolean isHead;
        if (itemStack.getType() == Material.PLAYER_HEAD || itemStack.getType() == Material.PLAYER_WALL_HEAD) {
            if (itemStack instanceof SlimefunItemStack sfis) {
                Optional<String> texture = sfis.getSkullTexture();
                if (texture.isPresent()) {
                    skin0 = PlayerSkin.fromBase64(texture.get());
                    isHead0 = true;
                }
            }
        }
        skin = skin0;
        isHead = isHead0;

        WorldUtils.doWorldEdit(pos1, pos2, (location -> {
            final Block targetBlock = location.getBlock();
            sfItem.callItemHandler(BlockPlaceHandler.class, handler -> handler.onPlayerPlace(
                    new BlockPlaceEvent(
                            targetBlock,
                            targetBlock.getState(),
                            targetBlock.getRelative(BlockFace.DOWN),
                            itemStack,
                            player,
                            true,
                            EquipmentSlot.HAND
                    )
            ));
            if (override) {
                Slimefun.getDatabaseManager().getBlockDataController().removeBlock(location);
            }
            if (!StorageCacheUtils.hasBlock(location)) {
                targetBlock.setType(t);
                if (isHead) {
                    PlayerHead.setSkin(targetBlock, skin, false);
                }
                Slimefun.getDatabaseManager().getBlockDataController().createBlock(location, sfid);
            }
            count.addAndGet(1);
        }));

        plugin.send(player, "command.paste.success", count.get(), System.currentTimeMillis() - currentMillSeconds);
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
            return Slimefun.getRegistry().getAllSlimefunItems()
                    .stream()
                    .filter(item -> {
                        Material type = item.getItem().getType();
                        return type.isBlock() && type != Material.AIR;
                    })
                    .map(SlimefunItem::getId).toList();
        }

        List<String> left = new ArrayList<>();
        for (String flag : FLAGS) {
            if (!CommandUtil.hasFlag(args, flag)) {
                left.add("-" + flag);
            }
        }
        return left;
    }
}
