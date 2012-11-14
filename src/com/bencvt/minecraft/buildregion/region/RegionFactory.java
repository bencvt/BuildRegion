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

        // Convert proto to a cuboid.
        Vector3 lower = new Vector3();
        Vector3 upper = new Vector3();
        proto.getAABB(lower, upper);
        final Axis axis = proto.getAxis();
        RegionCuboid cuboid = new RegionCuboid(lower, upper, axis);
        if (proto == RegionBase.DEFAULT_REGION) {
            // Reposition cuboid's origin in front of player.
            cuboid.setOriginCoords(defaultOrigin);
        } else if (proto.getRegionType() == RegionType.PLANE) {
            // Project player coordinates onto origin.
            cuboid.setOriginCoord(axis.next(), axis.next().getVectorComponent(defaultOrigin));
            cuboid.setOriginCoord(axis.next().next(), axis.next().next().getVectorComponent(defaultOrigin));
        }
        final ReadonlyVector3 origin = cuboid.getOriginReadonly();

        // Convert the cuboid region into the appropriate type.
        if (regionType == RegionType.PLANE) {
            region = new RegionPlane(origin, axis);
        } else if (regionType == RegionType.CUBOID) {
            region = cuboid;
        } else if (regionType == RegionType.CYLINDER) {
            region = new RegionCylinder(origin, axis,
                    cuboid.getSize(axis),
                    cuboid.getSize(axis.next())/2.0,
                    cuboid.getSize(axis.next().next())/2.0);
        } else if (regionType == RegionType.SPHERE) {
            region = new RegionSphere(origin, new Vector3(
                    cuboid.getSize(axis)/2.0,
                    cuboid.getSize(axis.next())/2.0,
                    cuboid.getSize(axis.next().next())/2.0), axis);
        } else {
            throw new UnsupportedOperationException(
                    "unimplemented region type " + String.valueOf(regionType));
        }
        return region;
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
