package com.balugaq.sfworldedit.api.objects.enums;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public enum Facing {
    PX("+x"),
    PY("+y"),
    PZ("+z"),
    NX("-x"),
    NY("-y"),
    NZ("-z"),
    ;

    private final String direction;

    Facing(String direction) {
        this.direction = direction;
    }

    public @Nullable Facing opposite() {
        return switch (this) {
            case PX -> NX;
            case PY -> NY;
            case PZ -> NZ;
            case NX -> PX;
            case NY -> PY;
            case NZ -> PZ;
            default -> null;
        };
    }

    public @Nullable Facing rotate(int times) {
        return switch (times % 4) {
            case 0 -> this;
            case 1 -> switch (this) {
                case PX -> PY;
                case PY -> PZ;
                case PZ -> NX;
                case NX -> NY;
                case NY -> NZ;
                case NZ -> PX;
                default -> null;
            };
            case 2 -> opposite();
            case 3 -> switch (this) {
                case PX -> NZ;
                case PY -> PX;
                case PZ -> PY;
                case NX -> PZ;
                case NY -> NX;
                case NZ -> NY;
                default -> null;
            };
            default -> null;
        };
    }
}
