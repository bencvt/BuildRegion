package com.bencvt.minecraft.buildregion.ui.world;

import java.util.List;

import libshapedraw.MinecraftAccess;
import libshapedraw.animation.trident.Timeline;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.GLU;

import com.bencvt.minecraft.buildregion.region.Axis;
import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.region.RegionCylinder;

/**
 * A LibShapeDraw Shape representing a cylinder. This is purely cosmetic; see
 * RegionCylinder for the object that actually defines the cylinder.
 * <p>
 * The cylinder is rendered as a wireframe grid, aligned to block boundaries.
 * It's also rendered as a non-aligned translucent shell.
 * 
 * @author bencvt
 */
public class RenderCylinder extends RenderVertexBuffer {
    private final Axis axis;
    private final Vector3 halfHeightAndRadii;
    private final Cylinder shell;
    private Timeline timelineResize;

    public RenderCylinder(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden, RegionCylinder region) {
        super(lineColorVisible, lineColorHidden, region);
        axis = region.getAxis();
        halfHeightAndRadii = region.getHalfHeightAndRadiiReadonly().copy();
        shell = new Cylinder();
    }
/*
    @Override
    protected void renderShell(MinecraftAccess mc) {
        GL11.glPushMatrix();
        GL11.glTranslated(
                getOriginReadonly().getX() + 0.5 + axisOffset(Axis.X),
                getOriginReadonly().getY() + 0.5 + axisOffset(Axis.Y),
                getOriginReadonly().getZ() + 0.5 + axisOffset(Axis.Z));
        if (axis == Axis.X) {
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScaled(
                    halfHeightAndRadii.getZ(),
                    halfHeightAndRadii.getY(),
                    halfHeightAndRadii.getX() * 2.0);
        } else if (axis == Axis.Y) {
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glScaled(
                    halfHeightAndRadii.getX(),
                    halfHeightAndRadii.getZ(),
                    halfHeightAndRadii.getY() * 2.0);
        } else {
            GL11.glScaled(
                    halfHeightAndRadii.getX(),
                    halfHeightAndRadii.getY(),
                    halfHeightAndRadii.getZ() * 2.0);
        }

        GL11.glLineWidth(1.0F);
        getLineColorVisible().glApply(getAlphaBase());
        shell.setDrawStyle(GLU.GLU_LINE);
        shell.draw(1.0F, 1.0F, 1.0F, 24, 1);
        GL11.glLineWidth(LINE_WIDTH);

        GL11.glDisable(GL11.GL_CULL_FACE);
        getLineColorVisible().glApply(getAlphaBase() * ALPHA_SHELL);
        shell.setDrawStyle(GLU.GLU_FILL);
        shell.draw(1.0F, 1.0F, 1.0F, 24, 1);
        GL11.glEnable(GL11.GL_CULL_FACE);

        GL11.glPopMatrix();
    }

    private double axisOffset(Axis axis) {
        if (axis == this.axis) {
            if (axis == Axis.Y) {
                return axis.getVectorComponent(halfHeightAndRadii);
            }   
            return -axis.getVectorComponent(halfHeightAndRadii);
        }
        return 0.0;
    }
*/

    /*
    @Override
    public boolean updateIfPossible(RegionBase region, boolean animate) {
        if (!region.isRegionType(RegionCylinder.class)) {
            return false;
        }
        if (animate && getOriginReadonly().distanceSquared(region.getOriginReadonly()) > SHIFT_MAX_SQUARED) {
            return false;
        }
        RegionCylinder cylinder = (RegionCylinder) region;
        if (axis != cylinder.getAxis()) {
            return false;
        }
        animateShiftOrigin(cylinder.getOriginReadonly(), animate);
        if (timelineResize != null && !timelineResize.isDone()) {
            timelineResize.abort();
            timelineResize = null;
        }
        if (!animate) {
            halfHeightAndRadii.set(cylinder.getHalfHeightAndRadiiReadonly());
            return true;
        }
        timelineResize = new Timeline(halfHeightAndRadii);
        timelineResize.addPropertyToInterpolate("x",
                halfHeightAndRadii.getX(),
                cylinder.getHalfHeightAndRadiiReadonly().getX());
        timelineResize.addPropertyToInterpolate("y",
                halfHeightAndRadii.getY(),
                cylinder.getHalfHeightAndRadiiReadonly().getY());
        timelineResize.addPropertyToInterpolate("z",
                halfHeightAndRadii.getZ(),
                cylinder.getHalfHeightAndRadiiReadonly().getZ());
        timelineResize.setDuration(ANIM_DURATION);
        timelineResize.play();
        return true;
    }
    */

    // XXX: temporary
    private void boxAt(List<Vector3> vertexCache, int x, int y, int z) {
        double x0 = x + CUBE_MARGIN;
        double x1 = x + 1 - CUBE_MARGIN;
        double y0 = y + CUBE_MARGIN;
        double y1 = y + 1 - CUBE_MARGIN;
        double z0 = z + CUBE_MARGIN;
        double z1 = z + 1 - CUBE_MARGIN;
        vertexCache.add(new Vector3(x0, y0, z0));
        vertexCache.add(new Vector3(x0, y0, z1));
        vertexCache.add(new Vector3(x0, y1, z0));
        vertexCache.add(new Vector3(x0, y1, z1));
        vertexCache.add(new Vector3(x0, y0, z0));
        vertexCache.add(new Vector3(x0, y1, z0));
        vertexCache.add(new Vector3(x0, y0, z1));
        vertexCache.add(new Vector3(x0, y1, z1));
        vertexCache.add(new Vector3(x0, y0, z0));
        vertexCache.add(new Vector3(x1, y0, z0));
        vertexCache.add(new Vector3(x0, y0, z1));
        vertexCache.add(new Vector3(x1, y0, z1));
        vertexCache.add(new Vector3(x0, y1, z0));
        vertexCache.add(new Vector3(x1, y1, z0));
        vertexCache.add(new Vector3(x0, y1, z1));
        vertexCache.add(new Vector3(x1, y1, z1));
        vertexCache.add(new Vector3(x1, y0, z0));
        vertexCache.add(new Vector3(x1, y0, z1));
        vertexCache.add(new Vector3(x1, y1, z0));
        vertexCache.add(new Vector3(x1, y1, z1));
        vertexCache.add(new Vector3(x1, y0, z0));
        vertexCache.add(new Vector3(x1, y1, z0));
        vertexCache.add(new Vector3(x1, y0, z1));
        vertexCache.add(new Vector3(x1, y1, z1));
    }
/*
    @Override
    protected void populateVertexCacheWork(List<Vector3> vertexCache, RegionBase region, int offX, int offY, int offZ, int sizeX, int sizeY, int sizeZ) {
        RegionCylinder cylinder = (RegionCylinder) region;
        // TODO
    }
*/
}
