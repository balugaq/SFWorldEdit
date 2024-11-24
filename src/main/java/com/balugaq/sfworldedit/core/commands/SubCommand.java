package com.balugaq.sfworldedit.core.commands;

import org.bukkit.command.TabExecutor;

import javax.annotation.Nonnull;

public abstract class SubCommand implements TabExecutor {
    @Nonnull
    public abstract String getKey();
}
