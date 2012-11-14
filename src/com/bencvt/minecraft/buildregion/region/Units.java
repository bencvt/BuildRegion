package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

public enum Units {
    WHOLE(1.0),
    HALF(0.5),
    ANY(1.0);

    /**
     * The basic "smallest" value for this unit restriction type.
     */
    public final double atom;

    private Units(double atom) {
        this.atom = atom;
    }

    public Units half() {
        if (this == WHOLE) {
            return HALF;
        } else {
            return ANY;
        }
    }

    public Vector3 clamp(Vector3 v) {
        if (this == WHOLE) {
            v.floor();
        } else if (this == HALF) {
            v.scale(2.0).floor().scale(0.5);
        }
        // else do nothing
        return v;
    }

    public Vector3 clampAtom(Vector3 v) {
        clamp(v);
        if (v.getX() < atom) {
            v.setX(atom);
        }
        if (v.getY() < atom) {
            v.setY(atom);
        }
        if (v.getZ() < atom) {
            v.setZ(atom);
        }
        return v;
    }

    public double clamp(double d) {
        if (this == WHOLE) {
            return Math.floor(d);
        } else if (this == HALF) {
            return Math.floor(d * 2.0) * 0.5;
        } else {
            return d;
        }
    }

    public double clampAtom(double d) {
        return Math.max(atom, clamp(d));
    }

    public String v2s(ReadonlyVector3 v) {
        return new StringBuilder().append("x=").append(d2s(v.getX()))
                .append(", y=").append(d2s(v.getY()))
                .append(", z=").append(d2s(v.getZ()))
                .toString();
    }

    public String v2sCompact(ReadonlyVector3 v) {
        return new StringBuilder().append(d2sCompact(v.getX()))
                .append(",").append(d2sCompact(v.getY()))
                .append(",").append(d2sCompact(v.getZ()))
                .toString();
    }

    public String d2s(double d) {
        return this == WHOLE ? d2sCompact(d) : Double.toString(d);
    }

    public String d2sCompact(double d) {
        int i = (int) Math.floor(d);
        return d == i ? Integer.toString(i) : Double.toString(d);
    }
}
