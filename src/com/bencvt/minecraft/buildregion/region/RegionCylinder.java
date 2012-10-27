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
    private final Vector3 halfHeightAndRadii;

    protected RegionCylinder(ReadonlyVector3 origin, Axis axis, double height, double radiusA, double radiusB) {
        super(origin);
        setAxis(axis);
        halfHeightAndRadii = new Vector3();
        setHeight(height);
        setRadiusA(radiusA);
        setRadiusB(radiusB);
    }

    @Override
    public RegionType getRegionType() {
        return RegionType.CYLINDER;
    }

    @Override
    public RegionBase copyUsing(ReadonlyVector3 newOrigin, Axis newAxis) {
        if (axis == newAxis) {
            return new RegionCylinder(newOrigin, newAxis, getHeight(), getRadiusA(), getRadiusB());
        } else {
            return new RegionCylinder(newOrigin, newAxis, getHeight(), getRadiusB(), getRadiusA());
        }
    }

    @Override
    protected void onOriginUpdate() {
        Units.HALF.clamp(getOriginMutable());
        if (axis != null) {
            // axis can be null during the constructor
            axis.setVectorComponent(getOriginMutable(), Units.WHOLE.clamp(axis.getVectorComponent(getOriginReadonly())));
        }
    }

    @Override
    public boolean isInsideRegion(double x, double y, double z) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double getSize() {
        return Math.PI * 2.0 * halfHeightAndRadii.getX() * halfHeightAndRadii.getY() * halfHeightAndRadii.getZ();
    }

    @Override
    public boolean getAABB(Vector3 lower, Vector3 upper) {
        lower.set(getOriginReadonly()).subtract(halfHeightAndRadii);
        upper.set(getOriginReadonly()).add(halfHeightAndRadii);
        return true;
    }

    @Override
    public Units getUnits(Axis axis) {
        return axis == this.axis ? Units.WHOLE : Units.HALF;
    }

    @Override
    public boolean expand(Axis axis, double amount) {
        if (axis == this.axis) {
            amount *= 0.5;
            // TODO: adjust origin too
        }
        double prev = axis.getVectorComponent(halfHeightAndRadii);
        Units.HALF.clampAtom(axis.addVectorComponent(halfHeightAndRadii, amount));
        return axis.getVectorComponent(halfHeightAndRadii) != prev;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        double radiusA = getRadiusA();
        double radiusB = getRadiusB();
        if (radiusA == radiusB) {
            b.append("cylinder @ ");
            b.append(Units.HALF.v2s(getOriginReadonly()));
            b.append("\nradius ").append(getRadiusAxisA().toString().toLowerCase());
            b.append('/').append(getRadiusAxisB().toString().toLowerCase());
            b.append('=');
            b.append(Units.HALF.d2s(radiusA));
        } else {
            b.append("cylindroid @ ");
            b.append(Units.HALF.v2s(getOriginReadonly()));
            b.append("\nradius ").append(getRadiusAxisA().toString().toLowerCase());
            b.append('=');
            b.append(Units.HALF.d2s(radiusA));
            b.append(", ").append(getRadiusAxisB().toString().toLowerCase());
            b.append('=');
            b.append(Units.HALF.d2s(radiusB));
        }
        b.append(" height ").append(axis.toString().toLowerCase());
        b.append('=');
        b.append(Units.WHOLE.d2s(getHeight()));
        return b.toString();
    }

    public Axis getAxis() {
        return axis;
    }
    public void setAxis(Axis axis) {
        if (axis == null) {
            throw new IllegalArgumentException();
        }
        this.axis = axis;
        onOriginUpdate();
    }

    public double getHeight() {
        return axis.getVectorComponent(halfHeightAndRadii) * 2.0;
    }
    public void setHeight(double height) {
        axis.setVectorComponent(halfHeightAndRadii, Units.HALF.clampAtom(height * 0.5));
    }

    public double getRadiusA() {
        return axis.next().getVectorComponent(halfHeightAndRadii);
    }
    public void setRadiusA(double radiusA) {
        axis.next().setVectorComponent(halfHeightAndRadii, Units.HALF.clampAtom(radiusA));
    }

    public double getRadiusB() {
        return axis.next().next().getVectorComponent(halfHeightAndRadii);
    }
    public void setRadiusB(double radiusB) {
        axis.next().next().setVectorComponent(halfHeightAndRadii, Units.HALF.clampAtom(radiusB));
    }

    public Axis getRadiusAxisA() {
        return axis.next();
    }

    public Axis getRadiusAxisB() {
        return axis.next().next();
    }

    public ReadonlyVector3 getHalfHeightAndRadiiReadonly() {
        return halfHeightAndRadii;
    }
}
