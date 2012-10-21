package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

/**
 * Represent a cylinder -- actually an elliptic cylinder, or cylindroid --
 * specified by an origin 3-tuple, a radii 2-tuple, an axis, and a height.
 * The components of each tuple are reduced to half units; the height is
 * reduced to whole units. The one component of the origin matching the axis
 * is also reduced to whole units. Radii are non-negative and >=0.5. Height
 * is non negative and >=1.0.
 * 
 * @author bencvt
 */
public class RegionCylinder extends RegionBase {
    private Axis axis;
    private double height;
    private double radiusA;
    private double radiusB;

    public RegionCylinder(ReadonlyVector3 origin, Axis axis, double height, double radiusA, double radiusB) {
        super(origin);
        this.axis = axis;
        this.height = height;
        this.radiusA = radiusA;
        this.radiusB = radiusB;
        // TODO reduce values
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
        enforceHalfUnits(getOrigin());
        if (axis != null) { // can be null during the constructor
            setCoord(axis, enforceWholeUnits(getCoord(axis)));
        }
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
    public boolean getAABB(Vector3 lower, Vector3 upper) {
        Vector3 radii;
        if (axis == Axis.X) {
            radii = new Vector3(height/2.0, radiusA, radiusB);
        } else if (axis == Axis.Y) {
            radii = new Vector3(radiusA, height/2.0, radiusB);
        } else if (axis == Axis.Z) {
            radii = new Vector3(radiusA, radiusB, height/2.0);
        } else {
            throw new IllegalStateException();
        }
        lower.set(getOriginReadonly()).subtract(radii);
        upper.set(getOriginReadonly()).add(radii);
        return true;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (radiusA == radiusB) {
            b.append("cylinder @ ").append(strXYZ(getOrigin()));
            b.append("\nradius ").append(getRadiusAxisA().toString().toLowerCase());
            b.append('/').append(getRadiusAxisB().toString().toLowerCase());
            b.append('=').append(radiusA);
        } else {
            b.append("cylindroid @ ").append(strXYZ(getOrigin()));
            b.append("\nradius ").append(getRadiusAxisA().toString().toLowerCase());
            b.append('=').append(radiusA);
            b.append(", ").append(getRadiusAxisB().toString().toLowerCase());
            b.append('=').append(radiusB);
        }
        b.append(" height ").append(axis.toString().toLowerCase());
        b.append('=').append(height);
        return b.toString();
    }

    public Axis getAxis() {
        return axis;
    }
    public void setAxis(Axis axis) {
        this.axis = axis;
    }

    public double getHeight() {
        return height;
    }
    public void setHeight(double height) {
        this.height = height;
    }

    public double getRadiusA() {
        return radiusA;
    }
    public void setRadiusA(double radiusA) {
        this.radiusA = radiusA;
    }

    public double getRadiusB() {
        return radiusB;
    }
    public void setRadiusB(double radiusB) {
        this.radiusB = radiusB;
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

    public static Axis getRadiusAxisA(Axis axis) {
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
}
