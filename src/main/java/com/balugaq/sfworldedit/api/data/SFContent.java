package com.balugaq.sfworldedit.api.data;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import lombok.Getter;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Map;

@Getter
public class SFContent extends BukkitContent {
    private final String id;
    private final Map<Integer, ItemStack> menu;
    private final Map<String, String> data;
    private final boolean ticking;

    public SFContent(@Nonnull Location location, @Nonnull BlockState state, String id, Map<Integer, ItemStack> menu, Map<String, String> data, boolean ticking) {
        super(location, state);
        this.id = id;
        this.menu = menu;
        this.data = data;
        this.ticking = ticking;
    }

    public static int getIdentifier() {
        return BukkitContent.getIdentifier() | 4;
    }

    @Override
    public void action() {
        super.action();
        Slimefun.getDatabaseManager().getBlockDataController().createBlock(getLocation(), this.id);
        if (!ticking) {
            Slimefun.getTickerTask().disableTicker(getLocation());
        }
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                StorageCacheUtils.setData(getLocation(), entry.getKey(), entry.getValue());
            }
        }
        if (menu != null) {
            BlockMenu blockMenu = StorageCacheUtils.getMenu(getLocation());
            for (Map.Entry<Integer, ItemStack> entry : menu.entrySet()) {
                blockMenu.replaceExistingItem(entry.getKey(), entry.getValue());
            }
        }
    }
}
