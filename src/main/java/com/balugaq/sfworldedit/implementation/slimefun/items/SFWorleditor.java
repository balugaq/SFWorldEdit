package com.balugaq.sfworldedit.implementation.slimefun.items;

import com.balugaq.sfworldedit.implementation.SFWorldedit;
import com.balugaq.sfworldedit.utils.WorldUtils;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.ToolUseHandler;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class SFWorleditor extends SlimefunItem {
    public SFWorleditor(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    public void preRegister() {
        addItemHandler(
                // On player left click
                (ToolUseHandler) (blockBreakEvent, itemStack, i, list) -> {
                    blockBreakEvent.setCancelled(true);
                    Block block = blockBreakEvent.getBlock();
                    if (block == null) {
                        return;
                    }

                    Player player = blockBreakEvent.getPlayer();
                    SFWorldedit.getInstance().getCommandManager().setPos1(player.getUniqueId(), block.getLocation());
                    Location pos1 = SFWorldedit.getInstance().getCommandManager().getPos1(player.getUniqueId());
                    Location pos2 = SFWorldedit.getInstance().getCommandManager().getPos2(player.getUniqueId());
                    if (pos2 != null) {
                        SFWorldedit.getInstance().send(player, "command.setpos1.success-with-range", WorldUtils.locationToString(pos1), WorldUtils.locationRange(pos1, pos2));
                    } else {
                        SFWorldedit.getInstance().send(player, "command.setpos1.success", WorldUtils.locationToString(pos1));
                    }
                },

                // On player right click
                (ItemUseHandler) playerRightClickEvent -> {
                    playerRightClickEvent.cancel();
                    Optional<Block> optional = playerRightClickEvent.getClickedBlock();
                    if (optional.isPresent()) {
                        Block block = optional.get();
                        if (block == null) {
                            return;
                        }

                        Player player = playerRightClickEvent.getPlayer();

                        SFWorldedit.getInstance().getCommandManager().setPos2(player.getUniqueId(), block.getLocation());
                        Location pos1 = SFWorldedit.getInstance().getCommandManager().getPos1(player.getUniqueId());
                        Location pos2 = SFWorldedit.getInstance().getCommandManager().getPos2(player.getUniqueId());
                        if (pos1 != null) {
                            SFWorldedit.getInstance().send(player, "command.setpos2.success-with-range", WorldUtils.locationToString(pos1), WorldUtils.locationRange(pos1, pos2));
                        } else {
                            SFWorldedit.getInstance().send(player, "command.setpos2.success", WorldUtils.locationToString(pos2));
                        }
                    }
                });
    }
}
