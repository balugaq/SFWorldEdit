package com.balugaq.sfworldedit.api.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Setter
@Getter
public abstract class Content {
    private Location location;

    public Content(Location location) {
        this.location = location;
    }

    public abstract void action();
}
