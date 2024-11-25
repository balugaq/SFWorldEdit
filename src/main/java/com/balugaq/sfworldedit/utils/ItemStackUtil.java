package com.balugaq.sfworldedit.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

@UtilityClass
public class ItemStackUtil {
    @Nonnull
    public static ItemStack toPureItemStack(@Nonnull ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }

        return new ItemStack(itemStack);
    }
}
