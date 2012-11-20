package com.bencvt.minecraft.buildregion.ui.world;

import java.util.Arrays;
import java.util.List;

import libshapedraw.MinecraftAccess;
import libshapedraw.animation.trident.Timeline;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

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
public class RenderSphere extends RenderVertexBuffer {
    private final Vector3 radii;
    private final Sphere shell;
    private Timeline timelineResize;

    public RenderSphere(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden, RegionSphere region) {
        super(lineColorVisible, lineColorHidden, region);
        radii = region.getRadiiReadonly().copy();
        shell = new Sphere();
    }

    @Override
    protected ReadonlyVector3 getCornerReadonly() {
        return getOriginReadonly();
    }

    @Override
    protected void renderShell(MinecraftAccess mc) {
        GL11.glPushMatrix();
        GL11.glTranslated(
                getOriginReadonly().getX() + 0.5,
                getOriginReadonly().getY() + 0.5,
                getOriginReadonly().getZ() + 0.5);
        GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
        GL11.glScaled(radii.getX(), radii.getZ(), radii.getY());

        GL11.glLineWidth(1.0F);
        getLineColorVisible().glApply(getAlphaBase());
        shell.setDrawStyle(GLU.GLU_LINE);
        shell.draw(1.0F, 24, 24);
        GL11.glLineWidth(LINE_WIDTH);

        getLineColorVisible().glApply(getAlphaBase() * ALPHA_SHELL);
        shell.setDrawStyle(GLU.GLU_FILL);
        shell.setOrientation(GLU.GLU_INSIDE);
        shell.draw(1.0F, 24, 24);
        shell.setOrientation(GLU.GLU_OUTSIDE);
        shell.draw(1.0F, 24, 24);

        GL11.glPopMatrix();
    }

    @Override
    public boolean updateIfPossible(RegionBase region, boolean animate) {
        if (!region.isRegionType(RegionSphere.class)) {
            return false;
        }
        if (animate && getOriginReadonly().distanceSquared(region.getOriginReadonly()) > SHIFT_MAX_SQUARED) {
            return false;
        }
        RegionSphere sphere = (RegionSphere) region;
        // TODO
        //this.vertexOffset.animateStart(toTranslate, durationMs)
        //  - getAABBLowerCornerReadonly()
        //  - getAABBUpperCornerReadonly()
        //  - populateVertexCache()
        //  - vertexOffset.animateStart()
        animateShiftOrigin(sphere.getOriginReadonly(), animate);
        if (timelineResize != null && !timelineResize.isDone()) {
            timelineResize.abort();
            timelineResize = null;
        }
        if (!animate) {
            radii.set(sphere.getRadiiReadonly());
            return true;
        }
        timelineResize = new Timeline(radii);
        timelineResize.addPropertyToInterpolate("x", radii.getX(), sphere.getRadiusX());
        timelineResize.addPropertyToInterpolate("y", radii.getY(), sphere.getRadiusY());
        timelineResize.addPropertyToInterpolate("z", radii.getZ(), sphere.getRadiusZ());
        timelineResize.setDuration(ANIM_DURATION);
        timelineResize.play();
        return true;
    }

    /*
    @Override
    protected void populateVertexCacheWork(List<Vector3> vertexCache, RegionBase region, int offX, int offY, int offZ, int sizeX, int sizeY, int sizeZ) {
        // TODO
    }
    */
}
