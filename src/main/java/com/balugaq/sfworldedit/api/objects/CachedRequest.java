package com.balugaq.sfworldedit.api.objects;

import lombok.Getter;

import javax.annotation.Nonnull;

// confirm system, will implement in the future.
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
