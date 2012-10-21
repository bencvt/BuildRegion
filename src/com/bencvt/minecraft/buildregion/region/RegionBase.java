package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

import com.bencvt.minecraft.buildregion.ReadonlyBuildModeValue;
import com.bencvt.minecraft.buildregion.ui.RenderBase;

/**
 * A geometric region in space.
 * 
 * @author bencvt
 */
public abstract class RegionBase {
    private final Vector3 origin = Vector3.ZEROS.copy();

    public RegionBase(ReadonlyVector3 origin) {
        this.origin.setX(origin.getX());
        this.origin.setY(origin.getY());
        this.origin.setZ(origin.getZ());        
    }

    public abstract RegionBase copyUsing(ReadonlyVector3 origin, Axis axis);

    public abstract RegionType getRegionType();

    public Vector3 getOrigin() {
        return origin;
    }

    public double getCoord(Axis axis) {
        validateAxis(axis);
        if (axis == Axis.X) {
            return getOrigin().getX();
        } else if (axis == Axis.Y) {
            return getOrigin().getY();
        } else if (axis == Axis.Z) {
            return getOrigin().getZ();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void setCoord(Axis axis, double value) {
        validateAxis(axis);
        if (axis == Axis.X) {
            getOrigin().setX(value);
        } else if (axis == Axis.Y) {
            getOrigin().setY(value);
        } else if (axis == Axis.Z) {
            getOrigin().setZ(value);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void shiftCoord(Axis axis, double amount) {
        setCoord(axis, getCoord(axis) + amount);
    }

    private void validateAxis(Axis axis) {
        if (!isValidAxis(axis)) {
            throw new IllegalArgumentException(String.valueOf(axis) +
                    " is an invalid axis for " + this);
        }
    }

    public boolean isValidAxis(Axis axis) {
        return axis != null;
    }

    /**
     * @return true if the position (x,y,z) is inside this region
     */
    public abstract boolean isInsideRegion(double x, double y, double z);

    /**
     * @return a new Shape instance for rendering this region in the UI
     */
    public abstract RenderBase createShape(ReadonlyBuildModeValue buildMode);
}
