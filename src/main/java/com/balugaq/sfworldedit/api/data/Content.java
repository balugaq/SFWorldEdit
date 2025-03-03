package com.balugaq.sfworldedit.api.data;

import lombok.Getter;
import org.bukkit.Location;

import java.net.MalformedURLException;

@Getter
public abstract class Content {
    private final Location location;

    public Content(Location location) {
        this.location = location;
    }

    public abstract void action();
}
