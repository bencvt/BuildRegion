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
    protected RegionPlane(ReadonlyVector3 origin, Axis axis) {
        super(origin, axis);
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
        return !expand && axis == this.getAxis();
    }

    @Override
    public boolean isInsideRegion(double x, double y, double z) {
        if (getAxis() == Axis.X) {
            return (int) getOriginReadonly().getX() == (int) x;
        } else if (getAxis() == Axis.Y) {
            return (int) getOriginReadonly().getY() == (int) y;
        } else if (getAxis() == Axis.Z) {
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
        // make a 7x7x1 box
        lower.set(getOriginReadonly());
        upper.set(getOriginReadonly());
        getAxis().next().addVectorComponent(lower, -3);
        getAxis().next().addVectorComponent(upper,  3);
        getAxis().next().next().addVectorComponent(lower, -3);
        getAxis().next().next().addVectorComponent(upper,  3);
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
        return i18n("enum.regiontype.plane") + " " + i18n(getAxis()) + "=" + (int) getCoord();
    }

    public double getCoord() {
        return getAxis().getVectorComponent(getOriginReadonly());
    }
    public RegionPlane setCoord(double value) {
        setOriginCoord(getAxis(), value);
        return this;
    }

    public RegionPlane set(ReadonlyVector3 origin, Axis axis) {
        setAxis(axis).setOriginCoords(origin);
        return this;
    }
}
