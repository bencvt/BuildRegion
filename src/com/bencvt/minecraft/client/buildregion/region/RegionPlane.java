package com.bencvt.minecraft.client.buildregion.region;

import com.bencvt.minecraft.client.buildregion.BuildMode;
import com.bencvt.minecraft.client.buildregion.ui.RenderBase;
import com.bencvt.minecraft.client.buildregion.ui.RenderPlane;

import libshapedraw.primitive.ReadonlyVector3;

/**
 * Represent a plane, specified by an axis and an integer coordinate along
 * that axis.
 * 
 * @author bencvt
 */
public class RegionPlane extends RegionBase {
    private final Axis axis;
    private int coord;

    public RegionPlane(Axis axis, ReadonlyVector3 coords) {
        this.axis = axis;
        if (axis == Axis.X) {
            coord = (int) coords.getX();
        } else if (axis == Axis.Y) {
            coord = (int) coords.getY();
        } else if (axis == Axis.Z) {
            coord = (int) coords.getZ();
        } else {
            throw new IllegalStateException();
        }
    }

    public Axis getAxis() {
        return axis;
    }

    public int getCoord() {
        return coord;
    }
    public RegionPlane addCoord(int amount) {
        coord += amount;
        return this;
    }

    @Override
    public boolean isInsideRegion(double x, double y, double z) {
        if (axis == Axis.X) {
            return coord == (int) x;
        } else if (axis == Axis.Y) {
            return coord == (int) y;
        } else if (axis == Axis.Z) {
            return coord == (int) z;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public double distance(ReadonlyVector3 pos) {
        if (axis == Axis.X) {
            return Math.abs(pos.getX() - coord);
        } else if (axis == Axis.Y) {
            return Math.abs(pos.getY() - coord);
        } else if (axis == Axis.Z) {
            return Math.abs(pos.getZ() - coord);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return "plane " + axis.toString().toLowerCase() + "=" + coord;
    }

    @Override
    public RenderBase createShape(BuildMode buildMode) {
        return new RenderPlane(buildMode.gridColor.copy(), axis, coord);
    }
}
