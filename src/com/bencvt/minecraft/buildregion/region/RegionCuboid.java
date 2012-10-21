package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;

public class RegionCuboid extends RegionBase {

    public RegionCuboid(ReadonlyVector3 origin) {
        super(origin);
        // TODO Auto-generated constructor stub
    }

    @Override
    public RegionBase copyUsing(ReadonlyVector3 origin, Axis axis) {
        // ignore axis
        return new RegionCuboid(origin.copy());
    }

    @Override
    public RegionType getRegionType() {
        return RegionType.CUBOID;
    }

    @Override
    public boolean isInsideRegion(double x, double y, double z) {
        // TODO Auto-generated method stub
        return false;
    }
}
