package com.bencvt.minecraft.buildregion.ui.world;

import libshapedraw.MinecraftAccess;
import libshapedraw.animation.trident.Timeline;
import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import libshapedraw.shape.Shape;
import libshapedraw.transform.ShapeScale;

import org.lwjgl.opengl.GL11;

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
     * Even smaller offset for when the elements being separated should in
     * theory be drawn on top of each other, but aren't, again to avoid ugly
     * intersections.
     */
    public static final double MINI_MARGIN = 1.0/128.0;
    /**
     * For shapes that are aligned to an axis, the sides of grid cubes (i.e.,
     * the lines parallel to the shape's axis) are rendered more subtly.
     */
    public static final double CORNER_MARKER_OFFSET = -1.0/16.0;
    public static final double CORNER_MARKER_SIZE = 1.0/4.0;
    public static final double ALPHA_SIDE = 1.0/2.0;
    public static final double ALPHA_SHELL = 1.0/4.0;
    public static final double ALPHA_MARKER_SPLIT = 5.0/16.0;
    public static final float LINE_WIDTH = 2.0F;
    public static final Color MARKER_COLOR_VISIBLE = Color.WHITE.copy().setAlpha(0.5);
    public static final Color MARKER_COLOR_HIDDEN = Color.WHITE.copy().setAlpha(0.25);
    public static final double MARKER_MARGIN = 1.0/8.0;

    private final ReadonlyColor lineColorVisible;
    private final ReadonlyColor lineColorHidden;
    private double alphaBase; // [0.0, 1.0] alpha scaling factor to apply to all lines
    private final ShapeScale shapeScale; // transform the entire shape
    private final boolean renderMarkers;
    private final Vector3 actualOrigin;
    private Timeline timelineShiftOrigin;
    private Timeline timelineFade;

    protected RenderBase(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden, boolean renderOriginMarker) {
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
        this.renderMarkers = renderOriginMarker;
        actualOrigin = getOriginReadonly().copy();
    }

    /**
     * Clean up any external resources owned by this Shape (e.g. OpenGL VBOs).
     * Called whenever this Shape is removed.
     */
    public void cleanup() {
        // do nothing
    }

    /**
     * To keep the origin marker rendering consistent, this method must be
     * called whenever the origin is being updated, either directly or via a
     * Timeline.
     */
    public final void onUpdateOrigin(ReadonlyVector3 newOrigin) {
        actualOrigin.set(newOrigin);
    }

    protected ReadonlyVector3 getCornerReadonly() {
        return null;
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
     * Render markers at the region's origin and at the region's lower corner.
     */
    protected void renderMarkers(MinecraftAccess mc, ReadonlyColor lineColor) {
        if (!renderMarkers) {
            return;
        }

        double x = getOriginReadonly().getX();
        double y = getOriginReadonly().getY();
        double z = getOriginReadonly().getZ();
        double x0 = x + MARKER_MARGIN;
        double y0 = y + MARKER_MARGIN;
        double z0 = z + MARKER_MARGIN;
        double x1 = x + 1 - MARKER_MARGIN;
        double y1 = y + 1 - MARKER_MARGIN;
        double z1 = z + 1 - MARKER_MARGIN;
        boolean splitX = actualOrigin.getX() != (int) actualOrigin.getX();
        boolean splitY = actualOrigin.getY() != (int) actualOrigin.getY();
        boolean splitZ = actualOrigin.getZ() != (int) actualOrigin.getZ();

        // When the origin marker evenly straddles 2 (or 4, or 8) blocks, draw
        // pairs of squares to split the marker.
        if (splitX || splitY || splitZ) {
            lineColor.glApply(alphaBase * ALPHA_MARKER_SPLIT);
            if (splitX) {
                x += 0.5 - MINI_MARGIN;
                mc.startDrawing(GL11.GL_LINE_LOOP);
                mc.addVertex(x, y0, z0);
                mc.addVertex(x, y1, z0);
                mc.addVertex(x, y1, z1);
                mc.addVertex(x, y0, z1);
                mc.finishDrawing();
                x += MINI_MARGIN + MINI_MARGIN;
                mc.startDrawing(GL11.GL_LINE_LOOP);
                mc.addVertex(x, y0, z0);
                mc.addVertex(x, y1, z0);
                mc.addVertex(x, y1, z1);
                mc.addVertex(x, y0, z1);
                mc.finishDrawing();
            }
            if (splitY) {
                y += 0.5 - MINI_MARGIN;
                mc.startDrawing(GL11.GL_LINE_LOOP);
                mc.addVertex(x0, y, z0);
                mc.addVertex(x1, y, z0);
                mc.addVertex(x1, y, z1);
                mc.addVertex(x0, y, z1);
                mc.finishDrawing();
                y += MINI_MARGIN + MINI_MARGIN;
                mc.startDrawing(GL11.GL_LINE_LOOP);
                mc.addVertex(x0, y, z0);
                mc.addVertex(x1, y, z0);
                mc.addVertex(x1, y, z1);
                mc.addVertex(x0, y, z1);
                mc.finishDrawing();
            }
            if (splitZ) {
                z += 0.5 - MINI_MARGIN;
                mc.startDrawing(GL11.GL_LINE_LOOP);
                mc.addVertex(x0, y0, z);
                mc.addVertex(x1, y0, z);
                mc.addVertex(x1, y1, z);
                mc.addVertex(x0, y1, z);
                mc.finishDrawing();
                z += MINI_MARGIN + MINI_MARGIN;
                mc.startDrawing(GL11.GL_LINE_LOOP);
                mc.addVertex(x0, y0, z);
                mc.addVertex(x1, y0, z);
                mc.addVertex(x1, y1, z);
                mc.addVertex(x0, y1, z);
                mc.finishDrawing();
            }
        }

        lineColor.glApply(alphaBase);
        renderBox(mc, x0, x1, y0, y1, z0, z1);

        // lower corner mini-marker
        if (getCornerReadonly() != null) {
            x0 = getCornerReadonly().getX() + CORNER_MARKER_OFFSET;
            x1 = x0 + CORNER_MARKER_SIZE;
            y0 = getCornerReadonly().getY() + CORNER_MARKER_OFFSET;
            y1 = y0 + CORNER_MARKER_SIZE;
            z0 = getCornerReadonly().getZ() + CORNER_MARKER_OFFSET;
            z1 = z0 + CORNER_MARKER_SIZE;
            mc.startDrawing(GL11.GL_LINE_LOOP);
            mc.addVertex(x0, y0, z0);
            mc.addVertex(x1, y0, z0);
            mc.addVertex(x0, y0, z1);
            mc.finishDrawing();
            mc.startDrawing(GL11.GL_LINES);
            mc.addVertex(x0, y0, z0).addVertex(x0, y1, z0);
            mc.addVertex(x1, y0, z0).addVertex(x0, y1, z0);
            mc.addVertex(x0, y0, z1).addVertex(x0, y1, z0);
            mc.finishDrawing();
        }
    }

    protected void renderBox(MinecraftAccess mc, double x0, double x1, double y0, double y1, double z0, double z1) {
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
     * Attempt to update this instance to match the specified region,
     * animating the change as appropriate.
     * 
     * @param region the new region, will not be null
     * @param animate if true animate smoothly, if false update instantly
     * @return false if the ShapeManager should just fade this instance out
     *         and create a new RenderBase instance instead.
     */
    public abstract boolean updateIfPossible(RegionBase region, boolean animate);

    /**
     * Adjust to the player moving around. For large or infinite shapes, this
     * is the input to use to limit the number of lines rendered.
     */
    public abstract void updateObserverPosition(ReadonlyVector3 observerPosition);

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

    /** @param animate if true animate smoothly, if false update instantly */
    protected void animateShiftOrigin(ReadonlyVector3 newOrigin, boolean animate) {
        onUpdateOrigin(newOrigin);
        if (timelineShiftOrigin != null && !timelineShiftOrigin.isDone()) {
            timelineShiftOrigin.abort();
            timelineShiftOrigin = null;
        }
        if (!animate) {
            getOrigin().set(newOrigin);
        }
        timelineShiftOrigin = new Timeline(getOrigin());
        timelineShiftOrigin.addPropertyToInterpolate("x", getOrigin().getX(), newOrigin.getX());
        timelineShiftOrigin.addPropertyToInterpolate("y", getOrigin().getY(), newOrigin.getY());
        timelineShiftOrigin.addPropertyToInterpolate("z", getOrigin().getZ(), newOrigin.getZ());
        timelineShiftOrigin.setDuration(ANIM_DURATION);
        timelineShiftOrigin.play();
    }

    /** @param animate if true animate smoothly, if false update instantly */
    public void animateFadeIn(boolean animate) {
        animateFadeStop();
        if (animate) {
            setAlphaBase(0.0);
            shapeScale.getScaleXYZ().set(ANIM_SCALE_FADE, ANIM_SCALE_FADE, ANIM_SCALE_FADE);
            animateFadeStart(1.0, 1.0);
        } else {
            setAlphaBase(1.0);
            shapeScale.getScaleXYZ().set(1.0, 1.0, 1.0);
        }
    }
    /** @param animate if true animate smoothly, if false update instantly */
    public void animateFadeOut(boolean animate) {
        animateFadeStop();
        if (animate) {
            animateFadeStart(0.0, ANIM_SCALE_FADE);
        } else {
            setAlphaBase(0.0);
            shapeScale.getScaleXYZ().set(ANIM_SCALE_FADE, ANIM_SCALE_FADE, ANIM_SCALE_FADE);
        }
    }
    private void animateFadeStop() {
        if (timelineFade != null && !timelineFade.isDone()) {
            timelineFade.abort();
        }
        timelineFade = null;
    }
    private void animateFadeStart(double toAlphaBase, double toScale) {
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
