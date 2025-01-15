package com.balugaq.sfworldedit.api.data;

import com.balugaq.sfworldedit.utils.WorldUtils;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.BlockState;

@Getter
public class BukkitContent extends Content {
    private BlockState state = null;

    public BukkitContent(Location location, BlockState state) {
        super(location);
        try {
            this.state = state.copy(location);
        } catch (NoSuchMethodError e) {
            try {
                this.state = state.copy();
            } catch (NoSuchMethodError e1) {
                this.state = state;
            }
        }
    }

    @Override
    public void action() {
        if (state != null) {
            WorldUtils.copyBlockState(state, this.getLocation().getBlock());
        }
        Slimefun.getDatabaseManager().getBlockDataController().removeBlock(getLocation());
    }
}
