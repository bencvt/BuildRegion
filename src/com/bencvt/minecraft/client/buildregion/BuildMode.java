package com.bencvt.minecraft.client.buildregion;

import com.bencvt.minecraft.client.buildregion.region.Axis;

import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;

/**
 * Defines how the current region should prevent or allow the player from
 * interacting with blocks inside of it.
 * 
 * @author bencvt
 */
public enum BuildMode {
    INSIDE  (Color.DODGER_BLUE, Color.DODGER_BLUE.copy().setAlpha(3.0/8.0)),
    OUTSIDE (Color.CRIMSON, Color.CRIMSON.copy().setAlpha(3.0/8.0)),
    DISPLAY (Color.MEDIUM_SEA_GREEN, Color.SILVER.copy().setAlpha(3.0/8.0));

    public final ReadonlyColor lineColorVisible;
    public final ReadonlyColor lineColorHidden;

    private BuildMode(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden) {
        this.lineColorVisible = lineColorVisible;
        this.lineColorHidden = lineColorHidden;
    }

    public BuildMode nextMode() {
        if (this == INSIDE) {
            return OUTSIDE;
        } else if (this == OUTSIDE) {
            return DISPLAY;
        } else {
            return INSIDE;
        }
    }

    public static BuildMode defaultMode() {
        return INSIDE;
    }
}
