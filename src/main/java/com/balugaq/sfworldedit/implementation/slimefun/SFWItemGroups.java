package com.balugaq.sfworldedit.implementation.slimefun;

import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.utils.Constants;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;

@UtilityClass
public class SFWItemGroups {
    public static final ItemGroup SFW_ITEMS = new ItemGroup(
            Constants.KEY_ITEMGROUP_SFW_ITEMS,
            Constants.ICON_ITEMGROUP_SFW_ITEMS
    );

    public static void setup(@Nonnull ISFWorldEdit plugin) {
        SFW_ITEMS.register(plugin);
    }
}
