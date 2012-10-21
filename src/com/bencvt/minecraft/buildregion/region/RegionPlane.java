package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;

/**
 * Represent a plane, specified by an axis and an integer coordinate along
 * that axis.
 * 
 * @author bencvt
 */
public class RegionPlane extends RegionBase {
    private Axis axis;

    public RegionPlane(ReadonlyVector3 origin, Axis axis) {
        super(origin);
        if (axis == null) {
            throw new IllegalArgumentException();
        }
        this.axis = axis;
    }

    @Override
    public RegionType getRegionType() {
        return RegionType.PLANE;
    }

    @Override
    public RegionBase copyUsing(ReadonlyVector3 origin, Axis axis) {
        return new RegionPlane(origin.copy(), axis);
    }

    @Override
    protected void onOriginUpdate() {
        truncateWholeUnits(getOrigin());
    }

    @Override
    public boolean canShiftAlongAxis(Axis axis) {
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
    public double size() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public String toString() {
        return "plane " + axis.toString().toLowerCase() + "=" + (int) getCoord();
    }

    public Axis getAxis() {
        return axis;
    }
    public void setAxis(Axis axis) {
        if (axis == null) {
            throw new IllegalArgumentException();
        }
        this.axis = axis;
    }

    public double getCoord() {
        return getCoord(axis);
    }
    public void setCoord(double value) {
        setCoord(axis, value);
    }
}
