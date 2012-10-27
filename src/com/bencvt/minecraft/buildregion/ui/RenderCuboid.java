package com.bencvt.minecraft.buildregion.ui;

import org.lwjgl.opengl.GL11;

import libshapedraw.MinecraftAccess;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.region.RegionCuboid;

public class RenderCuboid extends RenderBase {

    private final Vector3 lower;
    private final Vector3 upper;

    protected RenderCuboid(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden, RegionCuboid region) {
        super(lineColorVisible, lineColorHidden, true);
        getOrigin().set(region.getOriginReadonly());
        lower = new Vector3();
        upper = new Vector3();
        region.getAABB(lower, upper);
    }

    @Override
    protected void renderLines(MinecraftAccess mc, ReadonlyColor lineColor) {
        lineColor.glApply(getAlphaBase());
        final double x0 = lower.getX() + CUBE_MARGIN;
        final double x1 = upper.getX() + 1 - CUBE_MARGIN;
        final double y0 = lower.getY() + CUBE_MARGIN;
        final double y1 = upper.getY() + 1 - CUBE_MARGIN;
        final double z0 = lower.getZ() + CUBE_MARGIN;
        final double z1 = upper.getZ() + 1 - CUBE_MARGIN;
        // bottom
        mc.startDrawing(GL11.GL_LINE_LOOP);
        mc.addVertex(x0, y0, z0);
        mc.addVertex(x1, y0, z0);
        mc.addVertex(x1, y0, z1);
        mc.addVertex(x0, y0, z1);
        mc.finishDrawing();
        // top
        mc.startDrawing(GL11.GL_LINE_LOOP);
        mc.addVertex(x0, y1, z0);
        mc.addVertex(x1, y1, z0);
        mc.addVertex(x1, y1, z1);
        mc.addVertex(x0, y1, z1);
        mc.finishDrawing();
        // sides
        mc.startDrawing(GL11.GL_LINES);
        mc.addVertex(x0, y0, z0).addVertex(x0, y1, z0);
        mc.addVertex(x1, y0, z0).addVertex(x1, y1, z0);
        mc.addVertex(x1, y0, z1).addVertex(x1, y1, z1);
        mc.addVertex(x0, y0, z1).addVertex(x0, y1, z1);
        mc.finishDrawing();

        mc.startDrawing(GL11.GL_LINES);
        for (double xA = x0; xA < x1; xA += 1.0) {
            mc.addVertex(xA, y0, z0).addVertex(xA, y0, z1);
            mc.addVertex(xA, y1, z0).addVertex(xA, y1, z1);
            mc.addVertex(xA, y0, z0).addVertex(xA, y1, z0);
            mc.addVertex(xA, y0, z1).addVertex(xA, y1, z1);
        }
        for (double zA = z0; zA < z1; zA += 1.0) {
            mc.addVertex(x0, y0, zA).addVertex(x0, y1, zA);
            mc.addVertex(x1, y0, zA).addVertex(x1, y1, zA);
            mc.addVertex(x0, y0, zA).addVertex(x1, y0, zA);
            mc.addVertex(x0, y1, zA).addVertex(x1, y1, zA);
        }
        for (double yA = y0; yA < y1; yA += 1.0) {
            mc.addVertex(x0, yA, z0).addVertex(x0, yA, z1);
            mc.addVertex(x1, yA, z0).addVertex(x1, yA, z1);
            mc.addVertex(x0, yA, z0).addVertex(x1, yA, z0);
            mc.addVertex(x0, yA, z1).addVertex(x1, yA, z1);
        }
        mc.finishDrawing();
    }

    @Override
    public boolean updateIfPossible(RegionBase region) {
        if (!region.isRegionType(RegionCuboid.class)) {
            return false;
        }
        if (getOriginReadonly().distanceSquared(region.getOriginReadonly()) > SHIFT_MAX_SQUARED) {
            return false;
        }
        return false;
        // TODO:
        //RegionCuboid cuboid = (RegionCuboid) region;
        //shift(cuboid.getOriginReadonly());
        //and adjust the lower/upper corners
    }

    @Override
    public void updateObserverPosition(ReadonlyVector3 observerPosition) {
        // TODO: eventually use this to better support huge shapes, culling distant grid points
    }
}
