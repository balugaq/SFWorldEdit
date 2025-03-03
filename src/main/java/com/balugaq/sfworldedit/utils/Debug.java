package com.balugaq.sfworldedit.utils;

import com.balugaq.sfworldedit.implementation.SFWorldedit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Debug {
    public static void info(String message) {
        SFWorldedit.getInstance().getLogger().info(message);
    }

    public static void warning(String message) {
        SFWorldedit.getInstance().getLogger().warning(message);
    }

    public static void warn(String message) {
        SFWorldedit.getInstance().getLogger().warning(message);
    }

    public static void severe(String message) {
        SFWorldedit.getInstance().getLogger().severe(message);
    }

    public static void error(String message) {
        SFWorldedit.getInstance().getLogger().severe(message);
    }

    public static void debug(@Nonnull String message) {
        SFWorldedit.getInstance().debug(message);
    }

    public static void debug(@Nullable Object object) {
        debug(object == null ? "null" : object.toString());
    }

    public static void trace(@Nonnull Throwable e) {
        e.printStackTrace();
    }
}
