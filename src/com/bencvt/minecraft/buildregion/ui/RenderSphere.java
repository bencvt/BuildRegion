package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.MinecraftAccess;
import libshapedraw.animation.trident.Timeline;
import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import libshapedraw.shape.GLUSphere;
import libshapedraw.transform.ShapeScale;

import org.lwjgl.util.glu.GLU;

import com.bencvt.minecraft.buildregion.region.RegionBase;
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
        onUpdateOrigin(getOrigin().set(region.getOriginReadonly()));
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
        // TODO
    }

    @Override
    public boolean updateIfPossible(RegionBase region) {
        if (!region.isRegionType(RegionSphere.class)) {
            return false;
        }
        if (getOriginReadonly().distanceSquared(region.getOriginReadonly()) > SHIFT_MAX_SQUARED) {
            return false;
        }
        RegionSphere sphere = (RegionSphere) region;
        animateShiftOrigin(sphere.getOriginReadonly());
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
