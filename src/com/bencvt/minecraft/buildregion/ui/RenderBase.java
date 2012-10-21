package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.MinecraftAccess;
import libshapedraw.animation.trident.Timeline;
import libshapedraw.animation.trident.Timeline$RepeatBehavior;
import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import libshapedraw.shape.GLUSphere;
import libshapedraw.shape.Shape;
import libshapedraw.transform.ShapeRotate;
import libshapedraw.transform.ShapeScale;
import libshapedraw.transform.ShapeTranslate;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import com.bencvt.minecraft.buildregion.region.RegionBase;

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
    /** Shifting over too far a distance looks ugly; just fade out/in if over this distance. */
    public static final double SHIFT_MAX_SQUARED = Math.pow(32.0, 2.0);
    public static final float LINE_WIDTH = 2.0F;

    public static final Color ORIGIN_MARKER_COLOR_VISIBLE = Color.WHITE.copy().setAlpha(0.5);
    public static final Color ORIGIN_MARKER_COLOR_HIDDEN = Color.WHITE.copy().setAlpha(0.125);
    public static final float ORIGIN_MARKER_RADIUS = 3.0F/16.0F;
    protected static final ShapeRotate SPHERE_UPRIGHT = new ShapeRotate(90.0, 1.0, 0.0, 0.0);
    protected static final ShapeTranslate CENTER_WITHIN_BLOCK = new ShapeTranslate(0.5, 0.5, 0.5);

    private final ReadonlyColor lineColorVisible;
    private final ReadonlyColor lineColorHidden;
    private double alphaBase; // [0.0, 1.0] alpha scaling factor to apply to all lines
    private final ShapeScale shapeScale; // transform the entire shape
    private final GLUSphere originMarker;
    private Timeline timelineFade;
    private Timeline timelineShift;
    private boolean renderOriginMarkerNow;
    private final boolean renderOriginMarkerNormally;

    protected RenderBase(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden, boolean renderOriginMarkerNormally) {
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
        originMarker = createOriginMarker();
        this.renderOriginMarkerNormally = renderOriginMarkerNormally;
    }

    private GLUSphere createOriginMarker() {
        // TODO: also create a tick marker that's rendered every 10 blocks along the axes, so long as it's inside the shape
        // TODO: setting... markers: off origin ticks
        GLUSphere marker = new GLUSphere(getOrigin(),
                ORIGIN_MARKER_COLOR_VISIBLE,
                ORIGIN_MARKER_COLOR_HIDDEN,
                ORIGIN_MARKER_RADIUS);
        marker.setSlices(16).setStacks(16).getGLUQuadric().setDrawStyle(GLU.GLU_LINE);
        marker.addTransform(SPHERE_UPRIGHT).addTransform(CENTER_WITHIN_BLOCK);
        return marker;
    }

    public ReadonlyColor getLineColorVisible() {
        return lineColorVisible;
    }

    public ReadonlyColor getLineColorHidden() {
        return lineColorHidden;
    }

    /**
     * Attempt to update this instance to match the specified region,
     * animating the change as appropriate.
     * 
     * @return false if the ShapeManager should just fade this instance out
     *         and create a new RenderBase instance instead.
     */
    public abstract boolean updateIfPossible(RegionBase region);

    @Override
    protected final void renderShape(MinecraftAccess mc) {
        if (alphaBase <= 0.0) {
            return;
        }
        if (renderOriginMarkerNormally || renderOriginMarkerNow) {
            originMarker.render(mc);
        }
        renderShell(mc);
        GL11.glLineWidth(LINE_WIDTH);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        // renderLines is responsible for setting the line color
        renderLines(mc, getLineColorVisible());
        if (getLineColorHidden() != null) {
            GL11.glDepthFunc(GL11.GL_GREATER);
            renderLines(mc, getLineColorHidden());
        }
    }

    protected void renderShell(MinecraftAccess mc) {
        // do nothing
    }

    protected abstract void renderLines(MinecraftAccess mc, ReadonlyColor lineColor);

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

    /**
     * Adjust to the player moving around. For large or infinite shapes, this
     * is the input to use to limit the number of lines rendered.
     */
    public abstract void updateObserverPosition(ReadonlyVector3 observerPosition);

    public void shift(ReadonlyVector3 newOrigin) {
        if (timelineShift != null && !timelineShift.isDone()) {
            timelineShift.abort();
        }
        timelineShift = new Timeline(getOrigin());
        timelineShift.addPropertyToInterpolate("x", getOrigin().getX(), newOrigin.getX());
        timelineShift.addPropertyToInterpolate("y", getOrigin().getY(), newOrigin.getY());
        timelineShift.addPropertyToInterpolate("z", getOrigin().getZ(), newOrigin.getZ());
        timelineShift.setDuration(ANIM_DURATION);
        timelineShift.play();
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

    public boolean isRenderOriginMarkerNow() {
        return renderOriginMarkerNow;
    }
    public void setRenderOriginMarkerNow(boolean renderOriginMarkerNow) {
        this.renderOriginMarkerNow = renderOriginMarkerNow;
    }
}
