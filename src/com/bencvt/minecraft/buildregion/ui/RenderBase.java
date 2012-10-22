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
    /**
     * How small to shrink the shape when fading in/out.
     */
    public static final double ANIM_SCALE_FADE = 1.0 - 1.0/16.0;
    /**
     * Shifting over too far a distance looks bizarre because things move too
     * quickly. If shapes are further away than this threshold, the animation
     * should be a fade out/in instead of a shift.
     */
    public static final double SHIFT_MAX_SQUARED = Math.pow(32.0, 2.0);
    /**
     * Small offset used when rendering cubes to avoid ugly intersections with
     * block boundaries.
     */
    public static final double CUBE_MARGIN = 1.0/32.0;
    /**
     * For shapes that are aligned to an axis, the sides of grid cubes (i.e.,
     * the lines parallel to the shape's axis) are rendered more subtly.
     */
    public static final double ALPHA_SIDE = 0.5;
    public static final float LINE_WIDTH = 2.0F;
    public static final Color MARKER_COLOR_VISIBLE = Color.WHITE.copy().setAlpha(0.5);
    public static final Color MARKER_COLOR_HIDDEN = Color.WHITE.copy().setAlpha(0.25);
    public static final double MARKER_MARGIN = 1.0/8.0;
    /**
     * Static helper transforms for rendering GLU shapes.
     */
    protected static final ShapeTranslate CENTER_WITHIN_BLOCK = new ShapeTranslate(0.5, 0.5, 0.5);
    protected static final ShapeRotate SPHERE_UPRIGHT = new ShapeRotate(90.0, 1.0, 0.0, 0.0);

    private final ReadonlyColor lineColorVisible;
    private final ReadonlyColor lineColorHidden;
    private double alphaBase; // [0.0, 1.0] alpha scaling factor to apply to all lines
    private final ShapeScale shapeScale; // transform the entire shape
    private boolean renderMarkersNow;
    private final boolean renderMarkersNormally;
    private Timeline timelineShift;
    private Timeline timelineFade;

    protected RenderBase(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden, boolean renderOriginMarkerNormally) {
        super(Vector3.ZEROS.copy()); // child class responsible for setting origin
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
        this.renderMarkersNormally = renderOriginMarkerNormally;
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
        renderShell(mc);
        GL11.glLineWidth(LINE_WIDTH);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        // The rendering methods are responsible for setting the line color
        renderMarkers(mc, MARKER_COLOR_VISIBLE);
        renderLines(mc, getLineColorVisible());
        if (getLineColorHidden() != null) {
            GL11.glDepthFunc(GL11.GL_GREATER);
            renderMarkers(mc, MARKER_COLOR_HIDDEN);
            renderLines(mc, getLineColorHidden());
        }
    }

    /**
     * If a shell surrounding the region is appropriate for the region type,
     * the child class can override this method.
     */
    protected void renderShell(MinecraftAccess mc) {
        // do nothing
    }

    /**
     * Render a marker at the region's origin.
     * <p>
     * TODO: Also render markers every 10 blocks along the axes, so long as
     *       it's inside the shape. This will be a handy way to measure
     *       distances.
     * TODO: Replace the renderMarkersNormally/renderMarkersNow flags with a
     *       user preference for markers: {off, origin, ticks}
     */
    protected void renderMarkers(MinecraftAccess mc, ReadonlyColor lineColor) {
        if (!renderMarkersNormally && !renderMarkersNow) {
            return;
        }
        lineColor.glApply(alphaBase);
        final double x0 = getOriginReadonly().getX() + MARKER_MARGIN;
        final double x1 = getOriginReadonly().getX() + 1 - MARKER_MARGIN;
        final double y0 = getOriginReadonly().getY() + MARKER_MARGIN;
        final double y1 = getOriginReadonly().getY() + 1 - MARKER_MARGIN;
        final double z0 = getOriginReadonly().getZ() + MARKER_MARGIN;
        final double z1 = getOriginReadonly().getZ() + 1 - MARKER_MARGIN;
        // bottom
        mc.startDrawing(GL11.GL_LINE_LOOP);
        mc.addVertex(x0, y0, z0);
        mc.addVertex(x1, y0, z0);
        mc.addVertex(x1, y0, z1);
        mc.addVertex(x0, y0, z1);
        mc.finishDrawing();
        // top
        mc.startDrawing(GL11.GL_LINE_LOOP);
        mc.addVertex(x0, y1, z0);
        mc.addVertex(x1, y1, z0);
        mc.addVertex(x1, y1, z1);
        mc.addVertex(x0, y1, z1);
        mc.finishDrawing();
        // sides
        mc.startDrawing(GL11.GL_LINES);
        mc.addVertex(x0, y0, z0).addVertex(x0, y1, z0);
        mc.addVertex(x1, y0, z0).addVertex(x1, y1, z0);
        mc.addVertex(x1, y0, z1).addVertex(x1, y1, z1);
        mc.addVertex(x0, y0, z1).addVertex(x0, y1, z1);
        mc.finishDrawing();
    }

    /**
     * Render a grid of lines inside blocks that are inside the region.
     */
    protected abstract void renderLines(MinecraftAccess mc, ReadonlyColor lineColor);

    /**
     * Adjust to the player moving around. For large or infinite shapes, this
     * is the input to use to limit the number of lines rendered.
     */
    public abstract void updateObserverPosition(ReadonlyVector3 observerPosition);

    public boolean isRenderMarkersNow() {
        return renderMarkersNow;
    }
    public void setRenderMarkersNow(boolean renderMarkersNow) {
        this.renderMarkersNow = renderMarkersNow;
    }

    // ========
    // Animation
    // ========

    // Public getter/setter required for property interpolation (animation).
    public double getAlphaBase() {
        return alphaBase;
    }
    public void setAlphaBase(double alphaBase) {
        this.alphaBase = Math.max(0.0, Math.min(alphaBase, 1.0));
    }

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
}
