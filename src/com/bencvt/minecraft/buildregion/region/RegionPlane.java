package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

/**
 * Represent a plane, specified by an axis and an integer coordinate along
 * that axis.
 * 
 * @author bencvt
 */
public class RegionPlane extends RegionBase {
    private Axis axis;

    protected RegionPlane(ReadonlyVector3 origin, Axis axis) {
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
    public RegionBase copyUsing(ReadonlyVector3 newOrigin, Axis newAxis) {
        return new RegionPlane(newOrigin.copy(), newAxis);
    }

    @Override
    protected void onOriginUpdate() {
        Units.WHOLE.clamp(getOriginMutable());
    }

    @Override
    public boolean canAdjustAlongAxis(boolean expand, Axis axis) {
        return !expand && axis == this.axis;
    }

    @Override
    public boolean isInsideRegion(double x, double y, double z) {
        if (axis == Axis.X) {
            return (int) getOriginReadonly().getX() == (int) x;
        } else if (axis == Axis.Y) {
            return (int) getOriginReadonly().getY() == (int) y;
        } else if (axis == Axis.Z) {
            return (int) getOriginReadonly().getZ() == (int) z;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public double getSize() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public boolean getAABB(Vector3 lower, Vector3 upper) {
        return false;
    }

    @Override
    public Units getUnits(Axis axis) {
        return Units.WHOLE;
    }

    @Override
    public boolean expand(Axis axis, double amount) {
        return false;
    }

    @Override
    public String toString() {
        return i18n("enum.regiontype.plane") + " " + i18n(axis) + "=" + (int) getCoord();
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
        return axis.getVectorComponent(getOriginReadonly());
    }
    public void setCoord(double value) {
        setOriginCoord(axis, value);
    }
}
