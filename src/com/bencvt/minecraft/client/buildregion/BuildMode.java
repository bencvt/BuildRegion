package com.bencvt.minecraft.client.buildregion;

import com.bencvt.minecraft.client.buildregion.region.Axis;

import libshapedraw.animation.trident.Timeline;
import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;

/**
 * Defines how the current region should prevent or allow the player from
 * interacting with blocks inside of it.
 * 
 * @author bencvt
 */
public enum BuildMode {
    INSIDE  (Color.DODGER_BLUE, Color.DODGER_BLUE.copy().setAlpha(3.0/8.0)),
    OUTSIDE (Color.CRIMSON, Color.CRIMSON.copy().setAlpha(3.0/8.0)),
    DISPLAY (Color.MEDIUM_SEA_GREEN, Color.SILVER.copy().setAlpha(3.0/8.0));

    public final ReadonlyColor lineColorVisible;
    public final ReadonlyColor lineColorHidden;

    private BuildMode(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden) {
        this.lineColorVisible = lineColorVisible;
        this.lineColorHidden = lineColorHidden;
    }

    public BuildMode getNextMode() {
        if (this == INSIDE) {
            return OUTSIDE;
        } else if (this == OUTSIDE) {
            return DISPLAY;
        } else {
            return INSIDE;
        }
    }

    private static BuildMode activeMode = INSIDE;
    private static Timeline timeline;
    public static final long ANIM_DURATION = 500;
    public static final Color activeLineColorVisible = getActiveMode().lineColorVisible.copy();
    public static final Color activeLineColorHidden = getActiveMode().lineColorHidden.copy();

    public static BuildMode getActiveMode() {
        return activeMode;
    }
    public static void setActiveMode(BuildMode activeMode) {
        BuildMode.activeMode = activeMode;
        final Color[] colors = {
                activeLineColorVisible,
                activeLineColorHidden};
        final ReadonlyColor[] endColors = {
                (activeMode == null ? Color.BLACK : activeMode.lineColorVisible),
                (activeMode == null ? Color.BLACK : activeMode.lineColorHidden)};
        if (timeline != null && !timeline.isDone()) {
            timeline.abort();
        }
        timeline = new Timeline();
        for (int i = 0; i < colors.length; i++) {
            timeline.addPropertyToInterpolate(Timeline.property("red")
                    .on(colors[i])
                    .from(colors[i].getRed())
                    .to(endColors[i].getRed()));
            timeline.addPropertyToInterpolate(Timeline.property("green")
                    .on(colors[i])
                    .from(colors[i].getGreen())
                    .to(endColors[i].getGreen()));
            timeline.addPropertyToInterpolate(Timeline.property("blue")
                    .on(colors[i])
                    .from(colors[i].getBlue())
                    .to(endColors[i].getBlue()));
            timeline.addPropertyToInterpolate(Timeline.property("alpha")
                    .on(colors[i])
                    .from(colors[i].getAlpha())
                    .to(endColors[i].getAlpha()));
        }
        timeline.setDuration(500);
        timeline.play();
    }
}
