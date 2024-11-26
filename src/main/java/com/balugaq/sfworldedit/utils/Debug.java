package com.balugaq.sfworldedit.utils;

import com.balugaq.sfworldedit.implementation.SFWorldedit;

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

    public static void debug(String message) {
        SFWorldedit.getInstance().debug(message);
    }

    public static void trace(Throwable e) {
        e.printStackTrace();
    }
}
