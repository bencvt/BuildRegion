package com.bencvt.minecraft.client.buildregion.region;

import com.bencvt.minecraft.client.buildregion.Controller;

public class RegionFactory {
    private final RegionPlane plane;
    private final RegionCuboid cuboid;
    // TODO: other types

    public RegionFactory(RegionBase proto) {
        if (proto == null) {
            throw new IllegalArgumentException();
        }
        // TODO: coerce the active Region into instances for the other types with reasonable defaults
        plane = (proto instanceof RegionPlane) ? (RegionPlane) proto : null;
        cuboid = (proto instanceof RegionCuboid) ? (RegionCuboid) proto : null;
    }

    public RegionPlane getPlane() {
        return plane;
    }

    public RegionCuboid getCuboid() {
        return cuboid;
    }

    public RegionBase getRegionAs(RegionType regionType) {
        switch (regionType) {
        case PLANE: return plane;
        case CUBOID: return cuboid;
        default: return null;
        }
    }
}
