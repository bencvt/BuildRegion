package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

/**
 * Represent a cuboid, specified by an origin 3-tuple and a radii 3-tuple.
 * The components of each tuple are reduced to half units. Radii are
 * non-negative and >=0.5.
 * <p>
 * It's more intuitive to define a cuboid in terms of two corner 3-tuples, so
 * accessors and mutators are provided for this. The two pseudo 3-tuples are
 * reduced to whole units; the internal origin and radii may or may not be half
 * units but their sum will be whole units.
 * 
 * @author bencvt
 */
public class RegionCuboid extends RegionBase {
    private final Vector3 radii;

    public RegionCuboid(ReadonlyVector3 origin, ReadonlyVector3 radii) {
        super(origin);
        this.radii = radii.copy().absolute();
        enforceHalfUnits(this.radii);
    }

    @Override
    public RegionType getRegionType() {
        return RegionType.CUBOID;
    }

    @Override
    public RegionBase copyUsing(ReadonlyVector3 origin, Axis axis) {
        // ignore axis
        return new RegionCuboid(origin.copy(), radii);
    }

    @Override
    protected void onOriginUpdate() {
        enforceHalfUnits(getOrigin());
    }

    @Override
    public boolean isInsideRegion(double x, double y, double z) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double size() {
        return 8.0 * radii.getX() * radii.getY() * radii.getZ();
    }

    @Override
    public boolean getAABB(Vector3 lower, Vector3 upper) {
        lower.set(getOriginReadonly()).subtract(radii);
        upper.set(getOriginReadonly()).add(radii);
        return true;
    }

    @Override
    public String toString() {
        return "cuboid"; // TODO
    }
}
