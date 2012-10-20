package com.bencvt.minecraft.client.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;

import com.bencvt.minecraft.client.buildregion.ReadonlyBuildModeValue;
import com.bencvt.minecraft.client.buildregion.ui.RenderBase;

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
    public RegionType getRegionMode() {
        return RegionType.CUBOID;
    }

    @Override
    public boolean isInsideRegion(double x, double y, double z) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public RenderBase createShape(ReadonlyBuildModeValue buildMode) {
        // TODO Auto-generated method stub
        return null;
    }
}
