package com.bencvt.minecraft.buildregion.region;


public class RegionFactory {
    private final RegionPlane plane;
    private final RegionCuboid cuboid;
    private final RegionCylinder cylinder;    
    private final RegionSphere sphere;    
    // TODO: other types

    public RegionFactory(RegionBase proto) {
        if (proto == null) {
            throw new IllegalArgumentException();
        }
        // TODO: coerce the active Region into instances for the other types with reasonable defaults
        plane = (proto instanceof RegionPlane) ? (RegionPlane) proto : null;
        cuboid = (proto instanceof RegionCuboid) ? (RegionCuboid) proto : null;
        cylinder = (proto instanceof RegionCylinder) ? (RegionCylinder) proto : null;
        sphere = (proto instanceof RegionSphere) ? (RegionSphere) proto : null;
    }

    public RegionPlane getPlane() {
        return plane;
    }

    public RegionCuboid getCuboid() {
        return cuboid;
    }

    public RegionCylinder getCylinder() {
        return cylinder;
    }

    public RegionSphere getSphere() {
        return sphere;
    }

    public RegionBase getRegionAs(RegionType regionType) {
        switch (regionType) {
        case PLANE: return plane;
        case CUBOID: return cuboid;
        case CYLINDER: return cylinder;
        case SPHERE: return sphere;
        default: return null;
        }
    }
}
