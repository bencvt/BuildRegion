package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.MinecraftAccess;
import libshapedraw.animation.trident.Timeline;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.region.RegionCylinder;
import com.bencvt.minecraft.buildregion.region.RegionSphere;

public class RenderCylinder extends RenderBase {
    private final Vector3 halfHeightAndRadii;
    private Timeline timelineResize;

    public RenderCylinder(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden, RegionCylinder region) {
        super(lineColorVisible, lineColorHidden, true);
        onUpdateOrigin(getOrigin().set(region.getOriginReadonly()));
        halfHeightAndRadii = region.getHalfHeightAndRadiiReadonly().copy();
    }

    @Override
    protected void renderShell(MinecraftAccess mc) {
        // TODO Auto-generated method stub
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
