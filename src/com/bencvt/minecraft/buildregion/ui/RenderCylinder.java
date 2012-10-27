package com.bencvt.minecraft.buildregion.ui;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.GLU;

import libshapedraw.MinecraftAccess;
import libshapedraw.animation.trident.Timeline;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

import com.bencvt.minecraft.buildregion.region.Axis;
import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.region.RegionCylinder;
import com.bencvt.minecraft.buildregion.region.RegionSphere;

public class RenderCylinder extends RenderBase {
    private final Axis axis;
    private final Vector3 halfHeightAndRadii;
    private final Cylinder shell;
    private Timeline timelineResize;

    public RenderCylinder(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden, RegionCylinder region) {
        super(lineColorVisible, lineColorHidden, true);
        onUpdateOrigin(getOrigin().set(region.getOriginReadonly()));
        axis = region.getAxis();
        halfHeightAndRadii = region.getHalfHeightAndRadiiReadonly().copy();
        shell = new Cylinder();
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

    @Override
    protected void renderLines(MinecraftAccess mc, ReadonlyColor lineColor) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean updateIfPossible(RegionBase region) {
        if (!region.isRegionType(RegionCylinder.class)) {
            return false;
        }
        if (getOriginReadonly().distanceSquared(region.getOriginReadonly()) > SHIFT_MAX_SQUARED) {
            return false;
        }
        RegionCylinder cylinder = (RegionCylinder) region;
        if (axis != cylinder.getAxis()) {
            return false;
        }
        animateShiftOrigin(cylinder.getOriginReadonly());
        if (timelineResize != null && !timelineResize.isDone()) {
            timelineResize.abort();
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

    @Override
    public void updateObserverPosition(ReadonlyVector3 observerPosition) {
        // TODO: eventually use this to better support huge shapes, culling distant grid points
    }
}
