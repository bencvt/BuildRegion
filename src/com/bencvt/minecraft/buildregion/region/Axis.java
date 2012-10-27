package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

/**
 * X/Y/Z
 * 
 * @author bencvt
 */
public enum Axis {
    X, Y, Z;

    public Axis next() {
        if (this == X) {
            return Y;
        } else if (this == Y) {
            return Z;
        } else {
            return X;
        }
    }

    public double getVectorComponent(ReadonlyVector3 v) {
        if (this == X) {
            return v.getX();
        } else if (this == Y) {
            return v.getY();
        } else {
            return v.getZ();
        }
    }

    public Vector3 setVectorComponent(Vector3 v, double value) {
        if (this == X) {
            return v.setX(value);
        } else if (this == Y) {
            return v.setY(value);
        } else {
            return v.setZ(value);
        }
    }

    public Vector3 addVectorComponent(Vector3 v, double amount) {
        if (this == X) {
            return v.addX(amount);
        } else if (this == Y) {
            return v.addY(amount);
        } else {
            return v.addZ(amount);
        }
    }
}
