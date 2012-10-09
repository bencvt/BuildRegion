package com.bencvt.minecraft.client.buildregion.ui;

import libshapedraw.LibShapeDraw;
import libshapedraw.animation.trident.Timeline;
import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

import com.bencvt.minecraft.client.buildregion.BuildMode;
import com.bencvt.minecraft.client.buildregion.region.Axis;
import com.bencvt.minecraft.client.buildregion.region.RegionBase;

/**
 * Manage LibShapeDraw Shape objects and animations... i.e., the in-world GUI.
 * 
 * @author bencvt
 */
public class ShapeManager {
    public static final double ANIM_SCALE_FADE = 1.0 - 1.0/16.0;
    public static final long ANIM_DURATION = 500;

    private final LibShapeDraw libShapeDraw;
    private RenderBase mainShape;
    private RenderBase prevShape;
    private Timeline animFadeIn;
    private Timeline animFadeOut;
    private Timeline animShift;
    private Timeline animGridColor;

    public ShapeManager(LibShapeDraw libShapeDraw) {
        this.libShapeDraw = libShapeDraw;
    }

    public void animateFadeOut() {
        if (mainShape == null) {
            return;
        }
        if (prevShape != null) {
            libShapeDraw.getShapes().remove(prevShape);
        }
        prevShape = mainShape;
        mainShape = null;
        final Vector3 scale = prevShape.getShapeScale().getScaleXYZ();

        if (animFadeIn != null && !animFadeIn.isDone()) {
            animFadeIn.abort();
        }

        animFadeOut = new Timeline(prevShape);
        animFadeOut.addPropertyToInterpolate("alphaBase", prevShape.getAlphaBase(), 0.0);
        animFadeOut.addPropertyToInterpolate(Timeline.property("x")
                .on(scale).from(scale.getX()).to(ANIM_SCALE_FADE));
        animFadeOut.addPropertyToInterpolate(Timeline.property("y")
                .on(scale).from(scale.getY()).to(ANIM_SCALE_FADE));
        animFadeOut.addPropertyToInterpolate(Timeline.property("z")
                .on(scale).from(scale.getZ()).to(ANIM_SCALE_FADE));
        animFadeOut.setDuration(ANIM_DURATION);
        animFadeOut.play();
    }

    public void animateFadeIn(BuildMode buildMode, RegionBase buildRegion) {
        if (mainShape != null) {
            libShapeDraw.getShapes().remove(mainShape);
        }
        mainShape = buildRegion.createShape(buildMode);
        libShapeDraw.addShape(mainShape);
        mainShape.setAlphaBase(0.0);
        final Vector3 scale = mainShape.getShapeScale().getScaleXYZ()
                .set(ANIM_SCALE_FADE, ANIM_SCALE_FADE, ANIM_SCALE_FADE);

        animFadeIn = new Timeline(mainShape);
        animFadeIn.addPropertyToInterpolate("alphaBase", 0.0, 1.0);
        animFadeIn.addPropertyToInterpolate(Timeline.property("x")
                .on(scale).from(scale.getX()).to(1.0));
        animFadeIn.addPropertyToInterpolate(Timeline.property("y")
                .on(scale).from(scale.getY()).to(1.0));
        animFadeIn.addPropertyToInterpolate(Timeline.property("z")
                .on(scale).from(scale.getZ()).to(1.0));
        animFadeIn.setDuration(ANIM_DURATION);
        animFadeIn.play();
    }

    public void animateShift(Axis axis, double newCoord) {
        if (mainShape == null) {
            return;
        }
        mainShape.setShiftAxis(axis);

        if (animShift != null && !animShift.isDone()) {
            animShift.abort();
        }

        animShift = new Timeline(mainShape);
        animShift.addPropertyToInterpolate("shiftCoord", mainShape.getShiftCoord(), newCoord);
        animShift.setDuration(ANIM_DURATION);
        animShift.play();
    }

    public void animateGridColor(BuildMode buildMode) {
        if (mainShape == null) {
            return;
        }
        final Color[] colors = {
                mainShape.getLineColorVisible(),
                mainShape.getLineColorHidden()};
        final ReadonlyColor[] endColors = {
                buildMode.lineColorVisible,
                buildMode.lineColorHidden};

        if (animGridColor != null && !animGridColor.isDone()) {
            animGridColor.abort();
        }

        animGridColor = new Timeline(mainShape);
        for (int i = 0; i < colors.length; i++) {
            animGridColor.addPropertyToInterpolate(Timeline.property("red")
                    .on(colors[i])
                    .from(colors[i].getRed())
                    .to(endColors[i].getRed()));
            animGridColor.addPropertyToInterpolate(Timeline.property("green")
                    .on(colors[i])
                    .from(colors[i].getGreen())
                    .to(endColors[i].getGreen()));
            animGridColor.addPropertyToInterpolate(Timeline.property("blue")
                    .on(colors[i])
                    .from(colors[i].getBlue())
                    .to(endColors[i].getBlue()));
            animGridColor.addPropertyToInterpolate(Timeline.property("alpha")
                    .on(colors[i])
                    .from(colors[i].getAlpha())
                    .to(endColors[i].getAlpha()));
        }
        animGridColor.setDuration(ANIM_DURATION);
        animGridColor.play();
    }

    public void updateProjection(ReadonlyVector3 playerCoords) {
        if (mainShape != null) {
            mainShape.updateProjection(playerCoords);
        }
    }
}
