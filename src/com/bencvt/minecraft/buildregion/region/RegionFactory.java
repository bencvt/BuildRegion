package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.Vector3;

public class RegionFactory {
    private final RegionPlane plane;
    private final RegionCuboid cuboid;
    private final RegionCylinder cylinder;    
    private final RegionSphere sphere;    

    public RegionFactory(RegionBase proto) {
        if (proto == null) {
            throw new IllegalArgumentException();
        }
        if (proto == RegionBase.DEFAULT_REGION) {
            // TODO: reposition in front of player
        }
        // TODO: coerce the active Region into instances for the other types with reasonable defaults
        plane = (proto instanceof RegionPlane) ? (RegionPlane) proto : new RegionPlane(new Vector3(34,67,-32), Axis.Z);
        cuboid = (proto instanceof RegionCuboid) ? (RegionCuboid) proto : new RegionCuboid(new Vector3(8,83,321),new Vector3(10,7,5), Axis.Y);
        cylinder = (proto instanceof RegionCylinder) ? (RegionCylinder) proto : new RegionCylinder(new Vector3(-48,72,-93), Axis.Y, 7, 12, 10);
        sphere = (proto instanceof RegionSphere) ? (RegionSphere) proto : new RegionSphere(new Vector3(108,83,221),new Vector3(10,7,5), Axis.Z);
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
