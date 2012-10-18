package com.bencvt.minecraft.client.buildregion.region;

import com.bencvt.minecraft.client.buildregion.BuildMode;
import com.bencvt.minecraft.client.buildregion.ui.RenderBase;
import com.bencvt.minecraft.client.buildregion.ui.RenderPlane;

import libshapedraw.primitive.ReadonlyVector3;

/**
 * Represent a plane, specified by an axis and an integer coordinate along
 * that axis.
 * 
 * @author bencvt
 */
public class RegionPlane extends RegionBase {
    private final Axis axis;

    public RegionPlane(Axis axis, ReadonlyVector3 origin) {
        super(origin);
        this.axis = axis;
    }

    @Override
    public RegionType getRegionMode() {
        return RegionType.PLANE;
    }

    @Override
    public boolean isValidAxis(Axis axis) {
        return axis == this.axis;
    }

    @Override
    public boolean isInsideRegion(double x, double y, double z) {
        if (axis == Axis.X) {
            return (int) getOrigin().getX() == (int) x;
        } else if (axis == Axis.Y) {
            return (int) getOrigin().getY() == (int) y;
        } else if (axis == Axis.Z) {
            return (int) getOrigin().getZ() == (int) z;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return "plane " + axis.toString().toLowerCase() + "=" + (int) getCoord(axis);
    }

    @Override
    public RenderBase createShape() {
        return new RenderPlane(
                BuildMode.activeLineColorVisible,
                BuildMode.activeLineColorHidden,
                axis,
                getCoord(axis));
    }
}
