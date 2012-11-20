package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.Axis;

/**
 * Cardinal direction in 3 dimensions.
 * 
 * @author bencvt
 */
public enum Direction3D {
    DOWN  (0, Axis.Y, -1),
    UP    (1, Axis.Y, +1),
    NORTH (2, Axis.Z, -1),
    SOUTH (3, Axis.Z, +1),
    WEST  (4, Axis.X, -1),
    EAST  (5, Axis.X, +1);

    /** Value as set by the Minecraft client ray tracer. */
    public final int value;
    public final Axis axis;
    public final int axisDirection;
    private Direction3D back;
    private Direction3D left;
    private Direction3D right;
    static {
        DOWN.back = UP;
        DOWN.left = DOWN;
        DOWN.right = DOWN;
        UP.back = DOWN;
        UP.left = UP;
        UP.right = UP;
        NORTH.back = SOUTH;
        NORTH.left = WEST;
        NORTH.right = EAST;
        SOUTH.back = NORTH;
        SOUTH.left = EAST;
        SOUTH.right = WEST;
        WEST.back = EAST;
        WEST.left = SOUTH;
        WEST.right = NORTH;
        EAST.back = WEST;
        EAST.left = NORTH;
        EAST.right = SOUTH;
    }

    private Direction3D(int value, Axis axis, int axisDirection) {
        this.value = value;
        this.axis = axis;
        this.axisDirection = axisDirection;
    }

    public Direction3D getRelative(RelativeDirection3D relDir) {
        if (relDir == RelativeDirection3D.UP) {
            return UP;
        } else if (relDir == RelativeDirection3D.DOWN) {
            return DOWN;
        } else if (relDir == RelativeDirection3D.BACK) {
            return back;
        } else if (relDir == RelativeDirection3D.LEFT) {
            return left;
        } else if (relDir == RelativeDirection3D.RIGHT) {
            return right;
        } else {
            return this;
        }
    }

    public int getNeighborX(int x) {
        return (axis == Axis.X ? x + axisDirection : x);
    }
    public int getNeighborY(int y) {
        return (axis == Axis.Y ? y + axisDirection : y);
    }
    public int getNeighborZ(int z) {
        return (axis == Axis.Z ? z + axisDirection : z);
    }

    public static Direction3D fromValue(int value) {
        for (Direction3D dir : Direction3D.values()) {
            if (dir.value == value) {
                return dir;
            }
        }
        return null;
    }

    /**
     * @param yaw angle in degrees
     * @param pitch angle in degrees and in range [-90.0, 90.0]
     * @return the cardinal direction matching the angles, will never be null
     */
    public static Direction3D fromYawPitch(double yaw, double pitch) {
        if (pitch > 45.0) {
            return DOWN;
        } else if (pitch < -45.0) {
            return UP;
        }

        yaw = yaw % 360;
        if (yaw < 0.0) {
            yaw += 360.0;
        }

        if (yaw < 45.0) {
            return SOUTH;
        } else if (yaw < 90.0 + 45.0) {
            return WEST;
        } else if (yaw < 180.0 + 45.0) {
            return NORTH;
        } else if (yaw < 270.0 + 45.0) {
            return EAST;
        } else {
            return SOUTH;
        }
    }

    /**
     * @param yaw angle in degrees
     * @param pitch angle in degrees and in range [-90.0, 90.0]
     * @return the cardinal direction matching the angles, or null if ambiguous
     */
    public static Direction3D fromYawPitchUnambiguously(double yaw, double pitch) {
        final double A = 26.25;

        if (pitch > 90.0 - A) {
            return DOWN;
        } else if (pitch < -90.0 + A) {
            return UP;
        } else if (pitch > 45.0 || pitch < -45.0) {
            return null;
        }

        yaw = yaw % 360;
        if (yaw < 0.0) {
            yaw += 360.0;
        }

        if (yaw < A) {
            return SOUTH;
        } else if (yaw < 90.0 - A) {
            return null;
        } else if (yaw < 90.0 + A) {
            return WEST;
        } else if (yaw < 180.0 - A) {
            return null;
        } else if (yaw < 180.0 + A) {
            return NORTH;
        } else if (yaw < 270.0 - A) {
            return null;
        } else if (yaw < 270.0 + A) {
            return EAST;
        } else if (yaw < 360.0 - A) {
            return null;
        } else {
            return SOUTH;
        }
    }
}
