package com.balugaq.sfworldedit.api.data;

import com.balugaq.sfworldedit.utils.WorldUtils;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;

@Getter
public class BukkitContent extends Content {
    @Nullable
    private BlockState state = null;

    public BukkitContent(@Nonnull Location location, @Nonnull BlockState state) {
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

    public static int getIdentifier() {
        return 3;
    }

    @Override
    public void action() {
        if (state != null) {
            WorldUtils.copyBlockState(state, this.getLocation().getBlock());
        }
        Slimefun.getDatabaseManager().getBlockDataController().removeBlock(getLocation());
    }
}
