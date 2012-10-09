package com.bencvt.minecraft.client.buildregion.ui;

import libshapedraw.LibShapeDraw;
import libshapedraw.animation.trident.Timeline;
import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import libshapedraw.transform.ShapeScale;

import com.bencvt.minecraft.client.buildregion.BuildMode;
import com.bencvt.minecraft.client.buildregion.region.Axis;

/**
 * Manage LibShapeDraw Shape objects and animations... i.e., the in-world GUI.
 * 
 * @author bencvt
 */
public class ShapeManager {
    public static final double ANIM_SCALE_FADE = 1.0 - 1.0/16.0;
    public static final long ANIM_DURATION = 500;

    private final PlaneShape mainPlaneShape;
    private final ShapeScale mainPlaneShapeScale; // only used for animFadeIn
    private final PlaneShape prevPlaneShape;      // only used for animFadeOut
    private final ShapeScale prevPlaneShapeScale; // only used for animFadeOut
    private Timeline animFadeIn;
    private Timeline animFadeOut;
    private Timeline animShift;
    private Timeline animGridColor;

    public ShapeManager(LibShapeDraw api) {
        ReadonlyColor gridColor = BuildMode.defaultMode().gridColor;
        mainPlaneShape = new PlaneShape(
                gridColor.copy(),
                gridColor.copy(),
                gridColor.copy().scaleAlpha(0.5),
                2.0F, 3.0/8.0);
        mainPlaneShape.setVisible(false);
        mainPlaneShapeScale = new ShapeScale();
        mainPlaneShape.addTransform(mainPlaneShapeScale);
        api.addShape(mainPlaneShape);

        prevPlaneShape = new PlaneShape(mainPlaneShape);
        prevPlaneShape.setVisible(false);
        prevPlaneShapeScale = new ShapeScale();
        prevPlaneShape.addTransform(prevPlaneShapeScale);
        api.addShape(prevPlaneShape);
    }

    public void animateFadeIn(Axis axis, ReadonlyVector3 origin, int coord) {
        if (animFadeIn != null && !animFadeIn.isDone()) {
            animFadeIn.abort();
        }
        if (animShift != null && !animShift.isDone()) {
            // because animShift interpolates on coord, which we're about to set
            animShift.abort();
            animShift = null;
        }

        mainPlaneShape.setAxisAndOrigin(axis, origin);
        mainPlaneShape.setCoord(coord);
        mainPlaneShape.setVisible(true);
        mainPlaneShape.setAlphaBase(0.0);
        final Vector3 scale = mainPlaneShapeScale.getScaleXYZ()
                .set(ANIM_SCALE_FADE, ANIM_SCALE_FADE, ANIM_SCALE_FADE);

        animFadeIn = new Timeline(mainPlaneShape);
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

    public void animateFadeOut() {
        if (animFadeOut != null && !animFadeOut.isDone()) {
            animFadeOut.abort();
        }

        mainPlaneShape.setVisible(false);
        prevPlaneShape.copyFrom(mainPlaneShape);
        prevPlaneShape.setVisible(true);
        final Vector3 scale = prevPlaneShapeScale.getScaleXYZ()
                .set(mainPlaneShapeScale.getScaleXYZ());

        animFadeOut = new Timeline(prevPlaneShape);
        animFadeOut.addPropertyToInterpolate("alphaBase", prevPlaneShape.getAlphaBase(), 0.0);
        animFadeOut.addPropertyToInterpolate(Timeline.property("x")
                .on(scale).from(scale.getX()).to(ANIM_SCALE_FADE));
        animFadeOut.addPropertyToInterpolate(Timeline.property("y")
                .on(scale).from(scale.getY()).to(ANIM_SCALE_FADE));
        animFadeOut.addPropertyToInterpolate(Timeline.property("z")
                .on(scale).from(scale.getZ()).to(ANIM_SCALE_FADE));
        animFadeOut.setDuration(ANIM_DURATION);
        animFadeOut.play();
    }

    public void animateShift(double newCoord) {
        if (animShift != null && !animShift.isDone()) {
            animShift.abort();
        }

        mainPlaneShape.setVisible(true);

        animShift = new Timeline(mainPlaneShape);
        animShift.addPropertyToInterpolate("coord", mainPlaneShape.getCoord(), newCoord);
        animShift.setDuration(ANIM_DURATION);
        animShift.play();
    }

    public void animateGridColor(ReadonlyColor newColor) {
        if (animGridColor != null && !animGridColor.isDone()) {
            animGridColor.abort();
        }

        final Color[] modifyColors = {
                mainPlaneShape.getColorFront(),
                mainPlaneShape.getColorBack(),
                mainPlaneShape.getColorSides()};

        animGridColor = new Timeline(mainPlaneShape);
        for (Color c : modifyColors) {
            animGridColor.addPropertyToInterpolate(Timeline.property("red")
                    .on(c).from(c.getRed()).to(newColor.getRed()));
            animGridColor.addPropertyToInterpolate(Timeline.property("green")
                    .on(c).from(c.getGreen()).to(newColor.getGreen()));
            animGridColor.addPropertyToInterpolate(Timeline.property("blue")
                    .on(c).from(c.getBlue()).to(newColor.getBlue()));
        }
        animGridColor.setDuration(ANIM_DURATION);
        animGridColor.play();
    }

    public void updateProjection(ReadonlyVector3 playerCoords) {
        mainPlaneShape.updateProjection(playerCoords);
    }
}
