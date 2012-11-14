package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

public class RegionFactory {
    private final ReadonlyVector3 defaultOrigin;
    private final RegionBase original;
    private RegionBase region;

    public RegionFactory(RegionBase original, ReadonlyVector3 defaultOrigin) {
        if (original == null) {
            throw new IllegalArgumentException();
        }
        this.original = original;
        this.defaultOrigin = defaultOrigin;
    }

    public RegionBase convert(RegionType regionType) {
        if (regionType == null) {
            throw new IllegalArgumentException();
        }
        if (regionType == RegionType.NONE) {
            region = null;
            return null;
        }

        final RegionBase proto = region == null ? original : region;

        // Convert proto to a cuboid using half units.
        Vector3 lower = new Vector3();
        Vector3 upper = new Vector3();
        proto.getAABB(lower, upper);
        final Axis protoAxis = proto.getAxis();
        RegionCuboid aabb = new RegionCuboid(lower, upper, protoAxis) {
            @Override
            public Units getUnits(Axis axis) {
                return Units.HALF;
            }
        };
        if (proto == RegionBase.DEFAULT_REGION) {
            // Reposition cuboid's origin in front of player.
            aabb.setOriginCoords(defaultOrigin);
        } else if (proto.getRegionType() == RegionType.PLANE) {
            // Project player coordinates onto origin.
            aabb.setOriginCoord(protoAxis.next(), protoAxis.next().getVectorComponent(defaultOrigin));
            aabb.setOriginCoord(protoAxis.next().next(), protoAxis.next().next().getVectorComponent(defaultOrigin));
        }
        final ReadonlyVector3 origin = aabb.getOriginReadonly();

        // Convert the cuboid region into the appropriate type.
        if (regionType == RegionType.PLANE) {
            region = new RegionPlane(origin, protoAxis);
        } else if (regionType == RegionType.CUBOID) {
            region = aabb.copyUsing(origin, protoAxis);
        } else if (regionType == RegionType.CYLINDER) {
            Axis axis = getAxisForThinSquareCuboid(aabb);
            if (axis == null) {
                axis = protoAxis;
            }
            region = new RegionCylinder(origin, axis,
                    aabb.getSize(axis),
                    aabb.getSize(axis.next())/2.0,
                    aabb.getSize(axis.next().next())/2.0);
        } else if (regionType == RegionType.SPHERE) {
            Vector3 radii = new Vector3();
            Axis axis = getAxisForThinSquareCuboid(aabb);
            if (axis == null) {
                axis = protoAxis;
                axis.setVectorComponent(radii, aabb.getSize(axis)/2.0);
                axis.next().setVectorComponent(radii, aabb.getSize(axis.next())/2.0);
                axis.next().next().setVectorComponent(radii, aabb.getSize(axis.next().next())/2.0);
            } else {
                // Special case: create spheres from 1-thick square cuboids
                double radius = aabb.getSize(axis.next())/2.0;
                radii.set(radius, radius, radius);
            }
            region = new RegionSphere(origin, radii, axis);
        } else {
            throw new UnsupportedOperationException(
                    "unimplemented region type " + String.valueOf(regionType));
        }
        return region;
    }

    private Axis getAxisForThinSquareCuboid(RegionCuboid aabb) {
        double sizeX = aabb.getSizeX();
        double sizeY = aabb.getSizeY();
        double sizeZ = aabb.getSizeZ();
        if (sizeX == sizeY && sizeY == sizeZ) {
            return null;
        } else if (sizeX == 1.0 && sizeY == sizeZ) {
            return Axis.X;
        } else if (sizeY == 1.0 && sizeX == sizeZ) {
            return Axis.Y;
        } else if (sizeZ == 1.0 && sizeX == sizeY) {
            return Axis.Z;
        } else {
            return null;
        }
    }

    public RegionBase getOriginal() {
        return original;
    }

    public RegionBase getRegion() {
        return region;
    }

    public void reset() {
        region = null;
    }
}
