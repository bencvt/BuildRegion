package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

/**
 * A geometric region in space.
 * 
 * @author bencvt
 */
public abstract class RegionBase {
    private final Vector3 origin;

    protected RegionBase(ReadonlyVector3 origin) {
        this.origin = origin.copy();
        onOriginUpdate();
    }

    // ========
    // Methods for child classes to override (don't forget toString!)
    // ========

    public abstract RegionType getRegionType();

    /**
     * Deep copy this region but use a different origin. The axis parameter
     * may be used too, though it isn't applicable to all region types.
     */
    public abstract RegionBase copyUsing(ReadonlyVector3 origin, Axis axis);

    /**
     * Called whenever one of the origin's components is modified; this is the
     * hook to force origin coordinates to be a whole (or half) number.
     */
    protected abstract void onOriginUpdate();

    /**
     * @return true if it makes sense to shift the region along the specified
     *         axis. Will only ever be false if the region is infinite in one
     *         or more directions.
     */
    public boolean canShiftAlongAxis(Axis axis) {
        return axis != null;
    }

    /**
     * @return true if the position (x,y,z) is inside this region
     */
    public abstract boolean isInsideRegion(double x, double y, double z);

    public abstract double size();

    // ========
    // Accessors and mutators
    // ========

    public final ReadonlyVector3 getOriginReadonly() {
        return origin;
    }

    protected final Vector3 getOrigin() {
        return origin;
    }

    public final double getCoord(Axis axis) {
        if (axis == Axis.X) {
            return getOrigin().getX();
        } else if (axis == Axis.Y) {
            return getOrigin().getY();
        } else if (axis == Axis.Z) {
            return getOrigin().getZ();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public final void setCoord(Axis axis, double value) {
        if (axis == Axis.X) {
            getOrigin().setX(value);
        } else if (axis == Axis.Y) {
            getOrigin().setY(value);
        } else if (axis == Axis.Z) {
            getOrigin().setZ(value);
        } else {
            throw new IllegalArgumentException();
        }
        onOriginUpdate();
    }

    public final void shiftCoord(Axis axis, double amount) {
        setCoord(axis, getCoord(axis) + amount);
    }

    // ========
    // Utility methods
    // ========

    protected static final void truncateHalfUnits(Vector3 v) {
        v.scale(2.0).truncate().scale(0.5);
    }

    protected static final double truncateHalfUnits(double d) {
        return truncateWholeUnits(d * 2.0) * 0.5;
    }

    protected static final void truncateWholeUnits(Vector3 v) {
        v.truncate();
    }

    protected static final double truncateWholeUnits(double d) {
        return (int) d;
    }

    protected static final String strXYZ(Vector3 v) {
        return "x=" + strTruncateIfPossible(v.getX()) +
                ", y=" + strTruncateIfPossible(v.getY()) +
                ", z=" + strTruncateIfPossible(v.getZ());
    }

    protected static final String strTruncateIfPossible(double d) {
        int i = (int) d;
        return d == i ? Integer.toString(i) : Double.toString(d);
    }
}
