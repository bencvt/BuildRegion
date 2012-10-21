package com.bencvt.minecraft.buildregion.ui;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import libshapedraw.MinecraftAccess;
import libshapedraw.animation.trident.Timeline;
import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import libshapedraw.shape.GLUSphere;
import libshapedraw.transform.ShapeRotate;
import libshapedraw.transform.ShapeScale;

import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.region.RegionPlane;
import com.bencvt.minecraft.buildregion.region.RegionSphere;

/**
 * A LibShapeDraw Shape representing a sphere. This is purely cosmetic; see
 * RegionSphere for the object that actually defines the sphere.
 * <p>
 * The sphere is rendered as a wireframe grid, aligned to block boundaries.
 * It's also rendered as a non-aligned translucent shell.
 * 
 * @author bencvt
 */
public class RenderSphere extends RenderBase {
    private static final Color SHELL_COLOR = Color.PALE_GOLDENROD.copy().scaleAlpha(1.0/4.0);

    private final Vector3 lower; // cached AABB for rendering
    private final Vector3 upper; // cached AABB for rendering
    private final Vector3 radii;
    private final GLUSphere shell;
    private final ShapeScale shellScale;
    private Timeline timelineResize;

    public RenderSphere(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden, RegionSphere region) {
        super(lineColorVisible, lineColorHidden, true);
        getOrigin().set(region.getOriginReadonly());
        radii = region.getRadiiReadonly().copy();
        shell = new GLUSphere(getOrigin(), SHELL_COLOR, null, 1.0F);
        shell.getGLUQuadric().setDrawStyle(GLU.GLU_LINE);
        shellScale = new ShapeScale(radii);
        shell.addTransform(CENTER_WITHIN_BLOCK).addTransform(SPHERE_UPRIGHT).addTransform(shellScale);
        lower = new Vector3();
        upper = new Vector3();
        region.getAABB(lower, upper);
    }

    @Override
    protected void renderShell(MinecraftAccess mc) {
        shell.render(mc);
    }

    @Override
    protected void renderLines(MinecraftAccess mc, ReadonlyColor lineColor) {
        GL11.glColor4d(
                lineColor.getRed(),
                lineColor.getGreen(),
                lineColor.getBlue(),
                lineColor.getAlpha());
        final double x0 = lower.getX();
        final double x1 = upper.getX();
        final double y0 = lower.getY();
        final double y1 = upper.getY();
        final double z0 = lower.getZ();
        final double z1 = upper.getZ();
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
        mc.addVertex(x0, y0, z0);
        mc.addVertex(x0, y1, z0);
        mc.addVertex(x1, y0, z0);
        mc.addVertex(x1, y1, z0);
        mc.addVertex(x1, y0, z1);
        mc.addVertex(x1, y1, z1);
        mc.addVertex(x0, y0, z1);
        mc.addVertex(x0, y1, z1);
        mc.finishDrawing();

        /*
        final double x1 = upper.getX();
        final double y1 = upper.getY();
        final double z1 = upper.getZ();
        for (double x0 = lower.getX(); x0<=upper.getX(); x0+=2){
            for (double y0 = lower.getY(); y0<=upper.getY(); y0+=2){
                for (double z0 = lower.getZ(); z0<=upper.getZ(); z0+=2){

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
                    mc.addVertex(x0, y0, z0);
                    mc.addVertex(x0, y1, z0);
                    mc.addVertex(x1, y0, z0);
                    mc.addVertex(x1, y1, z0);
                    mc.addVertex(x1, y0, z1);
                    mc.addVertex(x1, y1, z1);
                    mc.addVertex(x0, y0, z1);
                    mc.addVertex(x0, y1, z1);
                    mc.finishDrawing();
                }
            }
        }*/
    }

    @Override
    public boolean updateIfPossible(RegionBase region) {
        if (!(region instanceof RegionSphere)) {
            return false;
        }
        if (getOriginReadonly().distanceSquared(region.getOriginReadonly()) > SHIFT_MAX_SQUARED) {
            return false;
        }
        RegionSphere sphere = (RegionSphere) region;
        shift(sphere.getOriginReadonly());
        if (timelineResize != null && !timelineResize.isDone()) {
            timelineResize.abort();
        }
        timelineResize = new Timeline(radii);
        timelineResize.addPropertyToInterpolate("x", radii.getX(), sphere.getRadiusX());
        timelineResize.addPropertyToInterpolate("y", radii.getY(), sphere.getRadiusY());
        timelineResize.addPropertyToInterpolate("z", radii.getZ(), sphere.getRadiusZ());
        timelineResize.setDuration(ANIM_DURATION);
        timelineResize.play();
        return true;
    }

    @Override
    public void updateObserverPosition(ReadonlyVector3 observerPosition) {
        // TODO: eventually use this to better support huge shapes, culling distant grid points
    }
}
