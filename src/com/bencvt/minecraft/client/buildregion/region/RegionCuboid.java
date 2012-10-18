package com.bencvt.minecraft.client.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;

import com.bencvt.minecraft.client.buildregion.BuildMode;
import com.bencvt.minecraft.client.buildregion.ui.RenderBase;

public class RegionCuboid extends RegionBase {

    public RegionCuboid(ReadonlyVector3 origin) {
        super(origin);
        // TODO Auto-generated constructor stub
    }

    @Override
    public RegionMode getRegionMode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isInsideRegion(double x, double y, double z) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public RenderBase createShape() {
        // TODO Auto-generated method stub
        return null;
    }
}
