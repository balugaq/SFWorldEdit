package com.balugaq.sfworldedit.implementation.slimefun;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;

@UtilityClass
public class SFWItems {
    public static final SlimefunItemStack SFW_WORLDEDITOR = new SlimefunItemStack(
            "SFW_WORLDEDITOR",
            Material.DIAMOND_AXE,
            "§bSlimefun World Editor",
            "",
            "Left-click to select position 1",
            "Right-click to select position 2"
    );
}
