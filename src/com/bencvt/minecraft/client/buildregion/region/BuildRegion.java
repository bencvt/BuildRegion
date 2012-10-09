package com.bencvt.minecraft.client.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;

/**
 * A geometric region in space.
 * 
 * @author bencvt
 */
public abstract class BuildRegion {
    /**
     * @return true if the position (x,y,z) is inside this region
     */
    public abstract boolean isInsideRegion(double x, double y, double z);

    /**
     * @return the (approximate) distance between pos and this region
     */
    public abstract double distance(ReadonlyVector3 pos);
}
