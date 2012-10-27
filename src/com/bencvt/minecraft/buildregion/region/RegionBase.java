package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

/**
 * A geometric region in space.
 * 
 * @author bencvt
 */
public abstract class RegionBase {
    // TODO: remember the type (but not the dimensions) of the last region used by the player between sessions
    //XXX: public static final RegionBase DEFAULT_REGION = new RegionPlane(Vector3.ZEROS, Axis.X);
    //public static final RegionBase DEFAULT_REGION = new RegionCuboid(new Vector3(0, 0, 0), new Vector3(1, 6, 6));
    public static final RegionBase DEFAULT_REGION = new RegionCylinder(new Vector3(0, 0, 0), Axis.Y, 12, 2.5, 5);
    //public static final RegionBase DEFAULT_REGION = new RegionSphere(new Vector3(0, 0, 0), new Vector3(5.5, 8, 8));

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
    public abstract RegionBase copyUsing(ReadonlyVector3 newOrigin, Axis newAxis);

    /**
     * Called whenever one of the origin's components is modified; this is the
     * hook to force origin coordinates to be a whole (or half) number.
     */
    protected abstract void onOriginUpdate();

    /**
     * @return true if it makes sense to move/expand the region along the
     *         specified axis. Will only ever be false if the region is
     *         infinite in one or more directions.
     */
    public boolean canAdjustAlongAxis(boolean expand, Axis axis) {
        return axis != null;
    }

    /**
     * @return true if the position (x,y,z) is inside this region
     */
    public abstract boolean isInsideRegion(double x, double y, double z);

    public abstract double getSize();

    /**
     * Get the axis-aligned minimum bounding box around this region.
     * @return true if bounds were set, false if this region is infinite
     */
    public abstract boolean getAABB(Vector3 lower, Vector3 upper);

    /**
     * @return the unit restriction applied to the specified axis.
     */
    public abstract Units getUnits(Axis axis);

    public abstract boolean expand(Axis axis, double amount);

    // ========
    // Accessors and mutators
    // ========

    public final boolean isRegionType(Class<? extends RegionBase> type) {
        return this != DEFAULT_REGION && type.isInstance(this);
    }

    public final ReadonlyVector3 getOriginReadonly() {
        return origin;
    }

    protected final Vector3 getOriginMutable() {
        return origin;
    }

    public final void setOriginCoord(Axis axis, double value) {
        axis.setVectorComponent(origin, value);
        onOriginUpdate();
    }

    public final void addOriginCoord(Axis axis, double amount) {
        axis.addVectorComponent(origin, amount);
        onOriginUpdate();
    }
}
