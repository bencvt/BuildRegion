package com.bencvt.minecraft.buildregion;

import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;

/**
 * Defines how the current region should prevent or allow the player from
 * interacting with blocks inside of it.
 * 
 * @author bencvt
 */
public enum BuildMode {
    INSIDE  (Color.DODGER_BLUE,      Color.DODGER_BLUE.copy().setAlpha(3.0/8.0)),
    OUTSIDE (Color.CRIMSON,          Color.CRIMSON.copy().setAlpha(3.0/8.0)),
    DISPLAY (Color.MEDIUM_SEA_GREEN, Color.SILVER.copy().setAlpha(3.0/8.0));

    public final ReadonlyColor colorVisible;
    public final ReadonlyColor colorHidden;

    private BuildMode(ReadonlyColor colorVisible, ReadonlyColor colorHidden) {
        this.colorVisible = colorVisible;
        this.colorHidden = colorHidden;
    }

    public BuildMode getNextMode() {
        if (this == INSIDE) {
            return OUTSIDE;
        } else if (this == OUTSIDE) {
            return DISPLAY;
        } else {
            return INSIDE;
        }
    }
}
