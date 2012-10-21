package com.bencvt.minecraft.buildregion;

import libshapedraw.animation.trident.Timeline;
import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;

/**
 * Encapsulate a non-null BuildMode value along with associated Colors that are
 * automatically animated every time the BuildMode changes.
 * 
 * @author bencvt
 */
public class BuildModeValue implements ReadonlyBuildModeValue {
    public static final long ANIM_DURATION = 500;

    private BuildMode value;
    private final Color colorVisible;
    private final Color colorHidden;
    private Timeline timeline;

    public BuildModeValue(BuildMode value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        this.value = value;
        this.colorVisible = value.colorVisible.copy();
        this.colorHidden = value.colorHidden.copy();
    }

    @Override
    public ReadonlyColor getColorVisible() {
        return colorVisible;
    }

    @Override
    public ReadonlyColor getColorHidden() {
        return colorHidden;
    }

    @Override
    public BuildMode getValue() {
        return value;
    }

    public void setValueNoAnimation(BuildMode value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        this.value = value;

        killAnimation();
        colorVisible.set(value.colorVisible);
        colorHidden.set(value.colorHidden);
    }

    public void setValue(BuildMode value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        this.value = value;

        killAnimation();
        timeline = new Timeline();
        animateColor(colorVisible, value.colorVisible);
        animateColor(colorHidden, value.colorHidden);
        timeline.setDuration(ANIM_DURATION);
        timeline.play();
    }

    private void killAnimation() {
        if (timeline != null && !timeline.isDone()) {
            timeline.abort();
        }
        timeline = null;
    }

    private void animateColor(Color color, ReadonlyColor toColor) {
        timeline.addPropertyToInterpolate(Timeline.property("red")
                .on(color)
                .from(color.getRed())
                .to(toColor.getRed()));
        timeline.addPropertyToInterpolate(Timeline.property("green")
                .on(color)
                .from(color.getGreen())
                .to(toColor.getGreen()));
        timeline.addPropertyToInterpolate(Timeline.property("blue")
                .on(color)
                .from(color.getBlue())
                .to(toColor.getBlue()));
        timeline.addPropertyToInterpolate(Timeline.property("alpha")
                .on(color)
                .from(color.getAlpha())
                .to(toColor.getAlpha()));
    }
}
