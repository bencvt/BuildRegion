package com.bencvt.minecraft.client.buildregion.region;

import com.bencvt.minecraft.client.buildregion.BuildMode;
import com.bencvt.minecraft.client.buildregion.ui.RenderBase;

import libshapedraw.primitive.ReadonlyVector3;

/**
 * A geometric region in space.
 * 
 * @author bencvt
 */
public abstract class RegionBase {
    /**
     * @return true if the position (x,y,z) is inside this region
     */
    public abstract boolean isInsideRegion(double x, double y, double z);

    /**
     * @return the (approximate) distance between pos and this region
     */
    public abstract double distance(ReadonlyVector3 pos);

    /**
     * @return a new Shape instance for rendering this region in the UI
     */
    public abstract RenderBase createShape(BuildMode buildMode);
}
