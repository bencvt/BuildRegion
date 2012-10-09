package com.bencvt.minecraft.client.buildregion.ui;

import libshapedraw.MinecraftAccess;
import libshapedraw.primitive.Color;
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
    /** [0.0, 1.0] scale to apply to lines hidden by terrain */
    public static final double ALPHA_HIDDEN = 3.0 / 8.0;
    public static final float LINE_WIDTH = 2.0F;

    private final Color lineColor;
    private double alphaBase; // [0.0, 1.0] alpha scaling factor to apply to all lines
    private final ShapeScale shapeScale; // transform the entire shape
    private Axis shiftAxis;
    private double shiftCoord;

    public RenderBase(Color lineColor) {
        super(Vector3.ZEROS.copy());
        setRelativeToOrigin(false);
        this.lineColor = lineColor;
        setAlphaBase(1.0);
        shapeScale = new ShapeScale(1.0, 1.0, 1.0);
        addTransform(shapeScale);
        shiftCoord = 0.0;
    }

    @Override
    protected final void renderShape(MinecraftAccess mc) {
        if (alphaBase <= 0.0) {
            return;
        }
        GL11.glLineWidth(LINE_WIDTH);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        renderLines(mc, alphaBase);
        if (ALPHA_HIDDEN > 0.0) {
            GL11.glDepthFunc(GL11.GL_GREATER);
            renderLines(mc, alphaBase * ALPHA_HIDDEN);
        }
    }

    protected abstract void renderLines(MinecraftAccess mc, double alphaLine);

    public Color getLineColor() {
        return lineColor;
    }

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

    public Axis getShiftAxis() {
        return shiftAxis;
    }
    public void setShiftAxis(Axis shiftAxis) {
        if (shiftAxis == null) {
            throw new NullPointerException();
        }
        this.shiftAxis = shiftAxis;
        if (shiftAxis == Axis.X) {
            shiftCoord = getOrigin().getX();
        } else if (shiftAxis == Axis.Y) {
            shiftCoord = getOrigin().getY();
        } else if (shiftAxis == Axis.Z) {
            shiftCoord = getOrigin().getZ();
        } else {
            throw new IllegalStateException();
        }
    }

    public double getShiftCoord() {
        return shiftCoord;
    }
    public void setShiftCoord(double shiftCoord) {
        this.shiftCoord = shiftCoord;
        if (shiftAxis == Axis.X) {
            getOrigin().setX(shiftCoord);
        } else if (shiftAxis == Axis.Y) {
            getOrigin().setY(shiftCoord);
        } else if (shiftAxis == Axis.Z) {
            getOrigin().setZ(shiftCoord);
        } else {
            throw new IllegalStateException();
        }
    }

    public void updateProjection(ReadonlyVector3 playerCoords) {
        // do nothing
    }
}
