package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;

/**
 * Represent a cylinder -- actually an elliptic cylinder, or cylindroid --
 * specified by an origin 3-tuple, a radii 2-tuple, an axis, and a height.
 * The components of each tuple are truncated to half units; the height is
 * truncated to whole units. The one component of the origin matching the axis
 * is also truncated to whole units. Radii are non-negative and >=0.5. Height
 * is non negative and >=1.0.
 * 
 * @author bencvt
 */
public class RegionCylinder extends RegionBase {
    private double radiusA;
    private double radiusB;
    private Axis axis;
    private double height;

    public RegionCylinder(ReadonlyVector3 origin, double radiusA, double radiusB, Axis axis, double height) {
        super(origin);
        // TODO Auto-generated constructor stub
    }

    @Override
    public RegionType getRegionType() {
        return RegionType.CYLINDER;
    }

    @Override
    public RegionBase copyUsing(ReadonlyVector3 origin, Axis axis) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void onOriginUpdate() {
        truncateHalfUnits(getOrigin());
        setCoord(axis, truncateWholeUnits(getCoord(axis)));
    }

    @Override
    public boolean isInsideRegion(double x, double y, double z) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double size() {
        return Math.PI * radiusA * radiusB * height;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (radiusA == radiusB) {
            b.append("cylinder @").append(getOrigin());
            b.append(" radius ").append(getRadiusAxisA().toString().toLowerCase());
            b.append('/').append(getRadiusAxisB().toString().toLowerCase());
            b.append('=').append(radiusA);
        } else {
            b.append("cylindroid @").append(getOrigin());
            b.append(" radius ").append(getRadiusAxisA().toString().toLowerCase());
            b.append('=').append(radiusA);
            b.append(", ").append(getRadiusAxisB().toString().toLowerCase());
            b.append('=').append(radiusB);
        }
        b.append(" height ").append(axis.toString().toLowerCase());
        b.append('=').append(height);
        return b.toString();
    }

    public Axis getRadiusAxisA() {
        if (axis == Axis.X) {
            return Axis.Y;
        } else if (axis == Axis.Y) {
            return Axis.X;
        } else if (axis == Axis.Z) {
            return Axis.X;
        } else {
            throw new IllegalStateException();
        }
    }

    public Axis getRadiusAxisB() {
        if (axis == Axis.X) {
            return Axis.Z;
        } else if (axis == Axis.Y) {
            return Axis.Z;
        } else if (axis == Axis.Z) {
            return Axis.Y;
        } else {
            throw new IllegalStateException();
        }
    }
}
