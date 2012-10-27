package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

public enum Units {
    WHOLE,
    HALF,
    ANY;

    public void clamp(Vector3 v) {
        if (this == WHOLE) {
            v.floor();
        } else if (this == HALF) {
            v.scale(2.0).floor().scale(0.5);
        }
        // else do nothing
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

    public String vectorToString(ReadonlyVector3 v) {
        return new StringBuilder().append("x=").append(doubleToString(v.getX()))
                .append(", y=").append(doubleToString(v.getY()))
                .append(", z=").append(doubleToString(v.getZ()))
                .toString();
    }

    public String vectorToStringCompact(ReadonlyVector3 v) {
        return new StringBuilder().append(doubleToString(v.getX()))
                .append(",").append(doubleToString(v.getY()))
                .append(",").append(doubleToString(v.getZ()))
                .toString();
    }

    public static final String doubleToString(double d) {
        int i = (int) Math.floor(d);
        return d == i ? Integer.toString(i) : Double.toString(d);
    }
}
