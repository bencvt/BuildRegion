package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.Axis;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

/**
 * Represent a sphere -- actually an ellipsoid -- specified by an origin
 * 3-tuple and a radii 3-tuple. The components of each tuple are reduced to
 * half units. Radii are non-negative and >=0.5.
 * 
 * @author bencvt
 */
public class RegionSphere extends RegionBase {
    private final Vector3 radii;

    protected RegionSphere(ReadonlyVector3 origin, ReadonlyVector3 radii, Axis axis) {
        super(origin, axis);
        this.radii = new Vector3();
        setRadiusX(radii.getX());
        setRadiusY(radii.getY());
        setRadiusZ(radii.getZ());
    }

    @Override
    public RegionType getRegionType() {
        return RegionType.SPHERE;
    }

    @Override
    public RegionBase copyUsing(ReadonlyVector3 newOrigin, Axis newAxis) {
        // TODO: rotate
        return new RegionSphere(newOrigin, radii, newAxis);
    }

    @Override
    protected void onOriginUpdate() {
        Units.HALF.clamp(getOriginMutable());
    }

    @Override
    public boolean isInsideRegion(double x, double y, double z) {
        return    Math.pow(((int) x) - getOriginReadonly().getX(), 2.0) / Math.pow(radii.getX(), 2.0)
                + Math.pow(((int) y) - getOriginReadonly().getY(), 2.0) / Math.pow(radii.getY(), 2.0)
                + Math.pow(((int) z) - getOriginReadonly().getZ(), 2.0) / Math.pow(radii.getZ(), 2.0)
                < 1.0;
    }

    @Override
    public double getSize() {
        return 4.0 / 3.0 * Math.PI * radii.getX() * radii.getY() * radii.getZ();
    }

    @Override
    public boolean getAABB(Vector3 lower, Vector3 upper) {
        lower.set(getOriginReadonly()).subtract(radii);
        upper.set(getOriginReadonly()).add(radii);
        // TODO: fix
        return true;
    }

    @Override
    public Units getUnits(Axis axis) {
        return Units.HALF;
    }

    @Override
    public boolean expand(Axis axis, double amount) {
        double prev = radii.getComponent(axis);
        Units.HALF.clampAtom(radii.addComponent(axis, amount));
        return radii.getComponent(axis) != prev;
    }

    @Override
    public String toString() {
        double r = radii.getX();
        if (r == radii.getY() && r == radii.getZ()) {
            return i18n("enum.regiontype.sphere.locked") + " @" +
                    Units.HALF.v2s(getOriginReadonly()) + "\n" +
                    i18n("radius") + " " + r;
        } else {
            return i18n("enum.regiontype.sphere.unlocked") + " @" +
                    Units.HALF.v2s(getOriginReadonly()) + "\n" +
                    i18n("radius") + " " + Units.HALF.v2s(radii);
        }
    }

    public ReadonlyVector3 getRadiiReadonly() {
        return radii;
    }

    public double getRadiusX() {
        return radii.getX();
    }
    public RegionSphere setRadiusX(double radiusX) {
        radii.setX(Math.max(0.5, Units.HALF.clamp(radiusX)));
        return this;
    }

    public double getRadiusY() {
        return radii.getY();
    }
    public RegionSphere setRadiusY(double radiusY) {
        radii.setY(Math.max(0.5, Units.HALF.clamp(radiusY)));
        return this;
    }

    public double getRadiusZ() {
        return radii.getZ();
    }
    public RegionSphere setRadiusZ(double radiusZ) {
        radii.setZ(Math.max(0.5, Units.HALF.clamp(radiusZ)));
        return this;
    }

    public RegionSphere set(ReadonlyVector3 origin, double radiusX, double radiusY, double radiusZ) {
        setOriginCoords(origin);
        return setRadiusX(radiusX).setRadiusY(radiusY).setRadiusZ(radiusZ);
    }
}
