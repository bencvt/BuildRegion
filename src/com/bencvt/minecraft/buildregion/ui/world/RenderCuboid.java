package com.bencvt.minecraft.buildregion.ui.world;

import libshapedraw.MinecraftAccess;
import libshapedraw.animation.trident.Timeline;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

import org.lwjgl.opengl.GL11;

import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.region.RegionCuboid;

/**
 * A LibShapeDraw Shape representing a cuboid. This is purely cosmetic; see
 * RegionCuboid for the object that actually defines the cuboid.
 * <p>
 * The cuboid is rendered as a translucent box. The edges are rendered as
 * thicker than normal lines. The sides are filled with block-sized grid lines,
 * unless the side is too long.
 * 
 * @author bencvt
 */
public class RenderCuboid extends RenderBase {
    /** Somewhat arbitrary limit for the number of grid lines. */
    public static final int MAX_GRID_SIZE = 100;

    private final Vector3 lower;
    private final Vector3 upper;
    private Timeline timelineShiftCorners;

    protected RenderCuboid(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden, RegionCuboid region) {
        super(lineColorVisible, lineColorHidden, true);
        onUpdateOrigin(getOrigin().set(region.getOriginReadonly()));
        lower = new Vector3();
        upper = new Vector3();
        region.getAABB(lower, upper);
    }

    @Override
    protected ReadonlyVector3 getCornerReadonly() {
        return lower;
    }

    @Override
    protected void renderShell(MinecraftAccess mc) {
        final double x0 = lower.getX() + CUBE_MARGIN;
        final double x1 = upper.getX() + 1 - CUBE_MARGIN;
        final double y0 = lower.getY() + CUBE_MARGIN;
        final double y1 = upper.getY() + 1 - CUBE_MARGIN;
        final double z0 = lower.getZ() + CUBE_MARGIN;
        final double z1 = upper.getZ() + 1 - CUBE_MARGIN;

        GL11.glDisable(GL11.GL_CULL_FACE);
        getLineColorVisible().glApply(getAlphaBase() * ALPHA_SHELL);

        // bottom
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(x0, y0, z0);
        GL11.glVertex3d(x0, y0, z1);
        GL11.glVertex3d(x1, y0, z1);
        GL11.glVertex3d(x1, y0, z0);
        GL11.glEnd();

        // top
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(x0, y1, z0);
        GL11.glVertex3d(x0, y1, z1);
        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x1, y1, z0);
        GL11.glEnd();

        // west
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(x0, y0, z0);
        GL11.glVertex3d(x0, y0, z1);
        GL11.glVertex3d(x0, y1, z1);
        GL11.glVertex3d(x0, y1, z0);
        GL11.glEnd();

        // east
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(x1, y0, z0);
        GL11.glVertex3d(x1, y0, z1);
        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x1, y1, z0);
        GL11.glEnd();

        // north
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(x0, y0, z0);
        GL11.glVertex3d(x0, y1, z0);
        GL11.glVertex3d(x1, y1, z0);
        GL11.glVertex3d(x1, y0, z0);
        GL11.glEnd();

        // south
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(x0, y0, z1);
        GL11.glVertex3d(x0, y1, z1);
        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x1, y0, z1);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    @Override
    protected void renderLines(MinecraftAccess mc, ReadonlyColor lineColor) {
        double x0 = lower.getX() + CUBE_MARGIN;
        double x1 = upper.getX() + 1 - CUBE_MARGIN;
        double y0 = lower.getY() + CUBE_MARGIN;
        double y1 = upper.getY() + 1 - CUBE_MARGIN;
        double z0 = lower.getZ() + CUBE_MARGIN;
        double z1 = upper.getZ() + 1 - CUBE_MARGIN;

        // border
        GL11.glLineWidth(LINE_WIDTH * 2.0F);
        lineColor.glApply(getAlphaBase());
        renderBox(mc, x0, x1, y0, y1, z0, z1);

        // grid lines
        GL11.glLineWidth(LINE_WIDTH);
        lineColor.glApply(getAlphaBase() * ALPHA_SIDE);
        mc.startDrawing(GL11.GL_LINES);
        double x0G = x0 + MINI_MARGIN;
        double x1G = x1 - MINI_MARGIN;
        double y0G = y0 + MINI_MARGIN;
        double y1G = y1 - MINI_MARGIN;
        double z0G = z0 + MINI_MARGIN;
        double z1G = z1 - MINI_MARGIN;
        if (y1 - y0 <= MAX_GRID_SIZE) {
            for (double yA = y0; yA < y1; yA += 1.0) {
                mc.addVertex(x0G, yA, z0G).addVertex(x0G, yA, z1G);
                mc.addVertex(x1G, yA, z0G).addVertex(x1G, yA, z1G);
                mc.addVertex(x0G, yA, z0G).addVertex(x1G, yA, z0G);
                mc.addVertex(x0G, yA, z1G).addVertex(x1G, yA, z1G);
            }
            for (double yA = y1; yA > y0; yA -= 1.0) {
                mc.addVertex(x0G, yA, z0G).addVertex(x0G, yA, z1G);
                mc.addVertex(x1G, yA, z0G).addVertex(x1G, yA, z1G);
                mc.addVertex(x0G, yA, z0G).addVertex(x1G, yA, z0G);
                mc.addVertex(x0G, yA, z1G).addVertex(x1G, yA, z1G);
            }
        }
        if (x1 - x0 <= MAX_GRID_SIZE) {
            for (double xA = x0; xA < x1; xA += 1.0) {
                mc.addVertex(xA, y0G, z0G).addVertex(xA, y0G, z1G);
                mc.addVertex(xA, y1G, z0G).addVertex(xA, y1G, z1G);
                mc.addVertex(xA, y0G, z0G).addVertex(xA, y1G, z0G);
                mc.addVertex(xA, y0G, z1G).addVertex(xA, y1G, z1G);
            }
            for (double xA = x1; xA > x0; xA -= 1.0) {
                mc.addVertex(xA, y0G, z0G).addVertex(xA, y0G, z1G);
                mc.addVertex(xA, y1G, z0G).addVertex(xA, y1G, z1G);
                mc.addVertex(xA, y0G, z0G).addVertex(xA, y1G, z0G);
                mc.addVertex(xA, y0G, z1G).addVertex(xA, y1G, z1G);
            }
        }
        if (z1 - z0 <= MAX_GRID_SIZE) {
            for (double zA = z0; zA < z1; zA += 1.0) {
                mc.addVertex(x0G, y0G, zA).addVertex(x0G, y1G, zA);
                mc.addVertex(x1G, y0G, zA).addVertex(x1G, y1G, zA);
                mc.addVertex(x0G, y0G, zA).addVertex(x1G, y0G, zA);
                mc.addVertex(x0G, y1G, zA).addVertex(x1G, y1G, zA);
            }
            for (double zA = z1; zA > z0; zA -= 1.0) {
                mc.addVertex(x0G, y0G, zA).addVertex(x0G, y1G, zA);
                mc.addVertex(x1G, y0G, zA).addVertex(x1G, y1G, zA);
                mc.addVertex(x0G, y0G, zA).addVertex(x1G, y0G, zA);
                mc.addVertex(x0G, y1G, zA).addVertex(x1G, y1G, zA);
            }
        }
        mc.finishDrawing();
    }

    @Override
    public boolean updateIfPossible(RegionBase region, boolean animate) {
        if (!region.isRegionType(RegionCuboid.class)) {
            return false;
        }
        if (animate && getOriginReadonly().distanceSquared(region.getOriginReadonly()) > SHIFT_MAX_SQUARED) {
            return false;
        }
        RegionCuboid cuboid = (RegionCuboid) region;
        animateShiftOrigin(region.getOriginReadonly(), animate);
        if (timelineShiftCorners != null && !timelineShiftCorners.isDone()) {
            timelineShiftCorners.abort();
            timelineShiftCorners = null;
        }
        if (!animate) {
            lower.set(cuboid.getLowerCornerReadonly());
            upper.set(cuboid.getUpperCornerReadonly());
            return true;
        }
        timelineShiftCorners = new Timeline(this);
        timelineShiftCorners.addPropertyToInterpolate(Timeline.property("x")
                .on(lower).from(lower.getX()).to(cuboid.getLowerCornerReadonly().getX()));
        timelineShiftCorners.addPropertyToInterpolate(Timeline.property("y")
                .on(lower).from(lower.getY()).to(cuboid.getLowerCornerReadonly().getY()));
        timelineShiftCorners.addPropertyToInterpolate(Timeline.property("z")
                .on(lower).from(lower.getZ()).to(cuboid.getLowerCornerReadonly().getZ()));
        timelineShiftCorners.addPropertyToInterpolate(Timeline.property("x")
                .on(upper).from(upper.getX()).to(cuboid.getUpperCornerReadonly().getX()));
        timelineShiftCorners.addPropertyToInterpolate(Timeline.property("y")
                .on(upper).from(upper.getY()).to(cuboid.getUpperCornerReadonly().getY()));
        timelineShiftCorners.addPropertyToInterpolate(Timeline.property("z")
                .on(upper).from(upper.getZ()).to(cuboid.getUpperCornerReadonly().getZ()));
        timelineShiftCorners.setDuration(ANIM_DURATION);
        timelineShiftCorners.play();
        return true;
    }

    @Override
    public void updateObserverPosition(ReadonlyVector3 observerPosition) {
        // do nothing
    }
}
