package com.bencvt.minecraft.client.buildregion.ui;

import java.util.HashMap;
import java.util.HashSet;

import libshapedraw.MinecraftAccess;
import libshapedraw.animation.trident.Timeline;
import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import libshapedraw.shape.Shape;
import libshapedraw.transform.ShapeScale;

import org.lwjgl.opengl.GL11;

import com.bencvt.minecraft.client.buildregion.region.Axis;

/**
 * A LibShapeDraw Shape representing a geometric region. This is purely
 * cosmetic; the Region classes actually define the region.
 * <p>
 * Regions are rendered as wireframe grids. Grid lines may be visible through
 * terrain and other game objects.
 * 
 * @author bencvt
 */
public abstract class RenderBase extends Shape {
    public static final long ANIM_DURATION = 500;
    public static final double ANIM_SCALE_FADE = 1.0 - 1.0/16.0;
    public static final float LINE_WIDTH = 2.0F;

    private final ReadonlyColor lineColorVisible;
    private final ReadonlyColor lineColorHidden;
    private double alphaBase; // [0.0, 1.0] alpha scaling factor to apply to all lines
    private final ShapeScale shapeScale; // transform the entire shape
    private Timeline timelineFade;
    private final HashMap<Axis, Timeline> timelineShift;

    public RenderBase(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden) {
        super(Vector3.ZEROS.copy());
        setRelativeToOrigin(false);
        if (lineColorVisible == null || lineColorHidden == null ||
                lineColorVisible == lineColorHidden) {
            throw new IllegalArgumentException("line colors must be non-null and unique");
        }
        this.lineColorVisible = lineColorVisible;
        this.lineColorHidden = lineColorHidden;
        setAlphaBase(1.0);
        shapeScale = new ShapeScale(1.0, 1.0, 1.0);
        addTransform(shapeScale);
        timelineShift = new HashMap<Axis, Timeline>();
    }

    public ReadonlyColor getLineColorVisible() {
        return lineColorVisible;
    }

    public ReadonlyColor getLineColorHidden() {
        return lineColorHidden;
    }

    @Override
    protected final void renderShape(MinecraftAccess mc) {
        if (alphaBase <= 0.0) {
            return;
        }
        GL11.glLineWidth(LINE_WIDTH);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        // renderLines is responsible for setting the line color
        renderLines(mc, getLineColorVisible(), alphaBase);
        if (getLineColorHidden() != null) {
            GL11.glDepthFunc(GL11.GL_GREATER);
            renderLines(mc, getLineColorHidden(), alphaBase);
        }
    }

    protected abstract void renderLines(MinecraftAccess mc, ReadonlyColor lineColor, double alphaLine);

    public double getAlphaBase() {
        return alphaBase;
    }
    public void setAlphaBase(double alphaBase) {
        if (alphaBase < 0.0 || alphaBase > 1.0) {
            throw new IllegalArgumentException();
        }
        this.alphaBase = alphaBase;
    }

    public ShapeScale getShapeScale() {
        return shapeScale;
    }

    public void updateObserverPosition(ReadonlyVector3 playerCoords) {
        // do nothing
    }

    public void shift(Axis axis, double newCoord) {
        Timeline timeline = timelineShift.get(axis);
        if (timeline != null && !timeline.isDone()) {
            timeline.abort();
        }
        timeline = new Timeline(getOrigin());
        if (axis == Axis.X) {
            timeline.addPropertyToInterpolate("x", getOrigin().getX(), newCoord);
        } else if (axis == Axis.Y) {
            timeline.addPropertyToInterpolate("y", getOrigin().getY(), newCoord);
        } else if (axis == Axis.Z) {
            timeline.addPropertyToInterpolate("z", getOrigin().getZ(), newCoord);
        } else {
            throw new IllegalArgumentException();
        }
        timeline.setDuration(ANIM_DURATION);
        timeline.play();
        timelineShift.put(axis, timeline);
    }

    public void fadeIn() {
        fadeStop();
        setAlphaBase(0.0);
        shapeScale.getScaleXYZ().set(ANIM_SCALE_FADE, ANIM_SCALE_FADE, ANIM_SCALE_FADE);
        fadeStart(1.0, 1.0);
    }
    public void fadeOut() {
        fadeStop();
        fadeStart(0.0, ANIM_SCALE_FADE);
    }
    private void fadeStop() {
        if (timelineFade != null && !timelineFade.isDone()) {
            timelineFade.abort();
        }
        timelineFade = null;
    }
    private void fadeStart(double toAlphaBase, double toScale) {
        timelineFade = new Timeline(this);
        timelineFade.addPropertyToInterpolate("alphaBase", getAlphaBase(), toAlphaBase);
        Vector3 scaleVec = shapeScale.getScaleXYZ();
        timelineFade.addPropertyToInterpolate(Timeline.property("x")
                .on(scaleVec).from(scaleVec.getX()).to(toScale));
        timelineFade.addPropertyToInterpolate(Timeline.property("y")
                .on(scaleVec).from(scaleVec.getY()).to(toScale));
        timelineFade.addPropertyToInterpolate(Timeline.property("z")
                .on(scaleVec).from(scaleVec.getZ()).to(toScale));
        timelineFade.setDuration(ANIM_DURATION);
        timelineFade.play();
    }
}
