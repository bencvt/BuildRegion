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
    INSIDE  (Color.DODGER_BLUE),
    OUTSIDE (Color.CRIMSON),
    DISPLAY (Color.LEMON_CHIFFON);

    public final ReadonlyColor gridColor;

    private BuildMode(ReadonlyColor gridColor) {
        this.gridColor = gridColor;
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
