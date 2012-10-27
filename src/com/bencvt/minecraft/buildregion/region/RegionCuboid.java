package com.bencvt.minecraft.buildregion.region;

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

    protected RegionCuboid(ReadonlyVector3 lowerCorner, ReadonlyVector3 upperCorner) {
        super(lowerCorner.copy().midpoint(upperCorner));
        this.lowerCorner = lowerCorner.copy();
        this.upperCorner = upperCorner.copy();
        normalize();
    }

    @Override
    public RegionType getRegionType() {
        return RegionType.CUBOID;
    }

    @Override
    public RegionBase copyUsing(ReadonlyVector3 newOrigin, Axis newAxis) {
        // ignore axis
        RegionCuboid result = new RegionCuboid(lowerCorner, upperCorner);
        result.getOrigin().set(newOrigin);
        result.onOriginUpdate();
        return result;
    }

    @Override
    protected void onOriginUpdate() {
        Units.HALF.clamp(getOrigin());
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
        Units.WHOLE.clamp(lowerCorner);
        Units.WHOLE.clamp(upperCorner);
        if (lowerCorner.getX() > upperCorner.getX() ||
                lowerCorner.getY() > upperCorner.getY() ||
                lowerCorner.getZ() > upperCorner.getZ()) {
            Vector3 tmp = lowerCorner.copy();
            lowerCorner.setMinimum(tmp, upperCorner);
            upperCorner.setMaximum(tmp, upperCorner);
        }
        getOrigin().set(lowerCorner).midpoint(upperCorner);
    }

    @Override
    public boolean isInsideRegion(double x, double y, double z) {
        normalize();
        x = Math.floor(x);
        y = Math.floor(y);
        z = Math.floor(z);
        return (x >= lowerCorner.getX() && x <= upperCorner.getX() &&
                y >= lowerCorner.getY() && y <= upperCorner.getY() &&
                z >= lowerCorner.getZ() && z <= upperCorner.getZ());
    }

    @Override
    public double getSize() {
        normalize();
        return ((upperCorner.getX() - lowerCorner.getX() + 1.0) *
                (upperCorner.getY() - lowerCorner.getY() + 1.0) *
                (upperCorner.getZ() - lowerCorner.getZ() + 1.0));
    }

    @Override
    public boolean getAABB(Vector3 lower, Vector3 upper) {
        normalize();
        lower.set(lowerCorner);
        upper.set(upperCorner);
        return true;
    }

    @Override
    public double shiftUnit() {
        return 1.0;
    }

    @Override
    public boolean expand(Axis axis, double amount) {
        boolean result = false;
        if (axis == Axis.X) {
            if (amount >= 0.0 || upperCorner.getX() > lowerCorner.getX()) {
                upperCorner.addX(amount);
                result = true;
            }
        } else if (axis == Axis.Y) {
            if (amount >= 0.0 || upperCorner.getY() > lowerCorner.getY()) {
                upperCorner.addY(amount);
                result = true;
            }
        } else if (axis == Axis.Z) {
            if (amount >= 0.0 || upperCorner.getZ() > lowerCorner.getZ()) {
                upperCorner.addZ(amount);
                result = true;
            }
        } else {
            throw new IllegalArgumentException();
        }
        normalize();
        return result;
    }

    @Override
    public String toString() {
        normalize();
        StringBuilder b = new StringBuilder().append("cuboid ")
                .append(Units.WHOLE.vectorToStringCompact(lowerCorner))
                .append(" to ")
                .append(Units.WHOLE.vectorToStringCompact(upperCorner))
                .append('\n')
                .append((int) (upperCorner.getX() - lowerCorner.getX()) + 1)
                .append('\u00d7')
                .append((int) (upperCorner.getY() - lowerCorner.getY()) + 1)
                .append('\u00d7')
                .append((int) (upperCorner.getZ() - lowerCorner.getZ()) + 1)
                .append(" = ");
        int size = (int) getSize();
        if (size == 1) {
            b.append("1 block");
        } else {
            b.append(size).append(" blocks");
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
        return upperCorner.getX() - lowerCorner.getX() + 1;
    }
    public double getSizeY() {
        return upperCorner.getY() - lowerCorner.getY() + 1;
    }
    public double getSizeZ() {
        return upperCorner.getZ() - lowerCorner.getZ() + 1;
    }

    public void setFromCornerSize(double cornerX, double cornerY, double cornerZ, double sizeX, double sizeY, double sizeZ) {
        Units.WHOLE.clamp(lowerCorner.set(cornerX, cornerY, cornerZ));
        upperCorner.set(
                lowerCorner.getX() - 1.0 + Math.max(1.0, Units.WHOLE.clamp(sizeX)),
                lowerCorner.getY() - 1.0 + Math.max(1.0, Units.WHOLE.clamp(sizeY)),
                lowerCorner.getZ() - 1.0 + Math.max(1.0, Units.WHOLE.clamp(sizeZ)));
        normalize();
    }
}
