package com.balugaq.sfworldedit.implementation.slimefun;

import com.balugaq.sfworldedit.implementation.SFWorldedit;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;

@UtilityClass
public class SFWItems {
    public static final SlimefunItemStack SFW_WORLDEDITOR = SFWorldedit.getInstance().getLocalizationService().getItem(
            "SFW_WORLDEDITOR",
            Material.DIAMOND_AXE
    );
}
