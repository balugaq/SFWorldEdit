package com.balugaq.sfworldedit.api.objects;

import lombok.Getter;

import javax.annotation.Nonnull;

@Getter
public class CachedRequest {
    private final Runnable request;

    public CachedRequest(@Nonnull Runnable request) {
        this.request = request;
    }

    public void execute() {
        request.run();
    }
}
