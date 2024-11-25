package com.balugaq.sfworldedit.implementation;

import com.balugaq.sfworldedit.api.plugin.ISFWorldEdit;
import com.balugaq.sfworldedit.implementation.slimefun.SFWItemGroups;
import com.balugaq.sfworldedit.implementation.slimefun.SFWItems;
import com.balugaq.sfworldedit.implementation.slimefun.SFWRecipes;
import com.balugaq.sfworldedit.implementation.slimefun.items.SFWorleditor;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;

@UtilityClass
public class SFWItemsSetup {
    public static SFWorleditor WORLDEDITOR;

    static {
        WORLDEDITOR = new SFWorleditor(
                SFWItemGroups.SFW_ITEMS,
                SFWItems.SFW_WORLDEDITOR,
                RecipeType.NULL,
                SFWRecipes.EMPTY_RECIPE
        );
    }

    public static void setup(@Nonnull ISFWorldEdit plugin) {
        SFWItemGroups.setup(plugin);
        WORLDEDITOR.register(plugin);
    }
}
