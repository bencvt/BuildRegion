package com.bencvt.minecraft.buildregion;

import libshapedraw.primitive.ReadonlyColor;

/**
 * A read-only view of a BuildModeValue instance.
 * 
 * @author bencvt
 */
public interface ReadonlyBuildModeValue {
    public ReadonlyColor getColorVisible();
    public ReadonlyColor getColorHidden();
    public BuildMode getValue();
}
