package com.balugaq.sfworldedit.utils;

import com.balugaq.sfworldedit.implementation.SFWorldedit;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

@UtilityClass
public class Constants {
    public static final NamespacedKey KEY_ITEMGROUP_SFW_ITEMS = SFWorldedit.getInstance().newKey("sfw_items");
    public static final ItemStack ICON_ITEMGROUP_SFW_ITEMS = ItemStackUtil.toPureItemStack(SFWorldedit.getInstance().getLocalizationService().getItemStack(
            "icons.itemgroup_sfw_items",
            Material.DIAMOND_AXE
    ));
    public static final String PERMISSION_ADMIN = "sfworldedit.admin";
    public static final String PERMISSION_COMMAND_ADMIN = "sfworldedit.command.admin";
}
