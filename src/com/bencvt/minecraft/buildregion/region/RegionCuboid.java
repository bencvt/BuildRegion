package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.Axis;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

/**
 * Represent a cuboid, specified by an AABB: two 3-tuples, one for the lower
 * corner and one for the upper. The components of each tuple are reduced to
 * whole units.
 * 
 * @author bencvt
 */
public class RegionCuboid extends RegionBase {
    private final Vector3 lowerCorner;
    private final Vector3 upperCorner;
    private final double atom;

    protected RegionCuboid(ReadonlyVector3 lowerCorner, ReadonlyVector3 upperCorner, Axis axis) {
        super(lowerCorner.copy().midpoint(upperCorner), axis);
        this.lowerCorner = lowerCorner.copy();
        this.upperCorner = upperCorner.copy();
        atom = getUnits(null).atom;
        normalize();
    }

    @Override
    public RegionType getRegionType() {
        return RegionType.CUBOID;
    }

    @Override
    public RegionBase copyUsing(ReadonlyVector3 newOrigin, Axis newAxis) {
        RegionCuboid result = new RegionCuboid(lowerCorner, upperCorner, newAxis);
        if (getAxis() != newAxis) {
            String a = getAxis().toString() + newAxis.toString();
            if (!a.contains("X")) {
                result.resize(getSizeX(), getSizeZ(), getSizeY());
            } else if (!a.contains("Y")) {
                result.resize(getSizeZ(), getSizeY(), getSizeX());
            } else {
                result.resize(getSizeY(), getSizeX(), getSizeZ());
            }
        }
        return result.setOriginCoords(newOrigin);
    }

    private void resize(double sizeX, double sizeY, double sizeZ) {
        upperCorner.set(lowerCorner).add(sizeX - atom, sizeY - atom, sizeZ - atom);
    }

    @Override
    protected void onOriginUpdate() {
        getUnits(null).half().clamp(getOriginMutable());
        if (lowerCorner == null) {
            // we were called by constructor
            return;
        }
        Vector3 offset = getOriginReadonly().copy();
        normalize();
        offset.subtract(getOriginReadonly());
        if (!offset.isZero()) {
            lowerCorner.add(offset);
            upperCorner.add(offset);
            normalize();
        }
    }

    private void normalize() {
        getUnits(null).clamp(lowerCorner);
        getUnits(null).clamp(upperCorner);
        if (lowerCorner.getX() > upperCorner.getX() ||
                lowerCorner.getY() > upperCorner.getY() ||
                lowerCorner.getZ() > upperCorner.getZ()) {
            Vector3 tmp = lowerCorner.copy();
            lowerCorner.setMinimum(tmp, upperCorner);
            upperCorner.setMaximum(tmp, upperCorner);
        }
        getOriginMutable().set(lowerCorner).midpoint(upperCorner);
    }

    @Override
    public boolean isInsideRegion(double x, double y, double z) {
        normalize();
        x = getUnits(Axis.X).clamp(x);
        y = getUnits(Axis.Y).clamp(y);
        z = getUnits(Axis.Z).clamp(z);
        return (x >= lowerCorner.getX() && x <= upperCorner.getX() &&
                y >= lowerCorner.getY() && y <= upperCorner.getY() &&
                z >= lowerCorner.getZ() && z <= upperCorner.getZ());
    }

    @Override
    public double getSize() {
        normalize();
        return getSizeX() * getSizeY() * getSizeZ();
    }

    @Override
    public boolean getAABB(Vector3 lower, Vector3 upper) {
        normalize();
        lower.set(lowerCorner);
        upper.set(upperCorner);
        return true;
    }

    @Override
    public Units getUnits(Axis axis) {
        return Units.WHOLE;
    }

    @Override
    public boolean expand(Axis axis, double amount) {
        if (amount >= 0.0 || upperCorner.getComponent(axis) > lowerCorner.getComponent(axis)) {
            upperCorner.addComponent(axis, amount);
            normalize();
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        normalize();
        StringBuilder b = new StringBuilder().append(i18n("enum.regiontype.cuboid.unlocked"))
                .append(' ')
                .append(getUnits(null).v2sCompact(lowerCorner))
                .append(' ').append(i18n("to")).append(' ')
                .append(getUnits(null).v2sCompact(upperCorner))
                .append('\n')
                .append(getUnits(Axis.X).d2sCompact(getSizeX()))
                .append('\u00d7')
                .append(getUnits(Axis.Y).d2sCompact(getSizeY()))
                .append('\u00d7')
                .append(getUnits(Axis.Z).d2sCompact(getSizeZ()))
                .append(" = ");
        double size = getSize();
        if (size == 1.0) {
            b.append(i18n("block", 1));
        } else {
            b.append(i18n("blocks", getUnits(null).d2sCompact(size)));
        }
        return b.toString();
    }

    public ReadonlyVector3 getLowerCornerReadonly() {
        return lowerCorner;
    }

    public ReadonlyVector3 getUpperCornerReadonly() {
        return upperCorner;
    }

    public double getSizeX() {
        return upperCorner.getX() - lowerCorner.getX() + atom;
    }
    public double getSizeY() {
        return upperCorner.getY() - lowerCorner.getY() + atom;
    }
    public double getSizeZ() {
        return upperCorner.getZ() - lowerCorner.getZ() + atom;
    }
    public double getSize(Axis axis) {
        if (axis == Axis.X) {
            return getSizeX();
        } else if (axis == Axis.Y) {
            return getSizeY();
        } else if (axis == Axis.Z) {
            return getSizeZ();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public RegionCuboid set(ReadonlyVector3 lowerCorner, double sizeX, double sizeY, double sizeZ) {
        getUnits(null).clamp(this.lowerCorner.set(lowerCorner));
        upperCorner.set(
                this.lowerCorner.getX() + getUnits(Axis.X).clampAtom(sizeX) - atom,
                this.lowerCorner.getY() + getUnits(Axis.Y).clampAtom(sizeY) - atom,
                this.lowerCorner.getZ() + getUnits(Axis.Z).clampAtom(sizeZ) - atom);
        normalize();
        return this;
    }
}
