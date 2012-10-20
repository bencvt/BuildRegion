package com.bencvt.minecraft.client.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;

import com.bencvt.minecraft.client.buildregion.ReadonlyBuildModeValue;
import com.bencvt.minecraft.client.buildregion.ui.RenderBase;
import com.bencvt.minecraft.client.buildregion.ui.RenderPlane;

/**
 * Represent a plane, specified by an axis and an integer coordinate along
 * that axis.
 * 
 * @author bencvt
 */
public class RegionPlane extends RegionBase {
    private final Axis axis;

    public RegionPlane(ReadonlyVector3 origin, Axis axis) {
        super(origin);
        if (axis == null) {
            throw new IllegalArgumentException();
        }
        this.axis = axis;
    }

    @Override
    public RegionBase copyUsing(ReadonlyVector3 origin, Axis axis) {
        return new RegionPlane(origin.copy(), axis);
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
    public RenderBase createShape(ReadonlyBuildModeValue buildMode) {
        return new RenderPlane(
                buildMode.getColorVisible(),
                buildMode.getColorHidden(),
                axis,
                getCoord(axis));
    }
}
