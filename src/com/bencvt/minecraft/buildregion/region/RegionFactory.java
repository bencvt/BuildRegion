package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

public class RegionFactory {
    private final RegionPlane plane;
    private final RegionCuboid cuboid;
    private final RegionCylinder cylinder;    
    private final RegionSphere sphere;    

    public RegionFactory(RegionBase proto, ReadonlyVector3 defaultOrigin) {
        if (proto == null) {
            throw new IllegalArgumentException();
        }

        // Convert proto to a cuboid.
        Vector3 lower = new Vector3();
        Vector3 upper = new Vector3();
        proto.getAABB(lower, upper);
        final Axis axis = proto.getAxis();
        cuboid = new RegionCuboid(lower, upper, axis);
        if (proto == RegionBase.DEFAULT_REGION) {
            // Reposition cuboid's origin in front of player.
            cuboid.setOriginCoords(defaultOrigin);
        }
        final ReadonlyVector3 origin = cuboid.getOriginReadonly();

        // Convert the cuboid region into the other types.
        plane = new RegionPlane(origin, axis);
        cylinder = new RegionCylinder(origin, axis,
                cuboid.getSize(axis),
                cuboid.getSize(axis.next())/2.0,
                cuboid.getSize(axis.next().next())/2.0);
        sphere = new RegionSphere(origin, new Vector3(
                cuboid.getSize(axis)/2.0,
                cuboid.getSize(axis.next())/2.0,
                cuboid.getSize(axis.next().next())/2.0), axis);
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
