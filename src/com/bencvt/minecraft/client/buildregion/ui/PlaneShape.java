package com.bencvt.minecraft.client.buildregion.ui;

import libshapedraw.MinecraftAccess;
import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import libshapedraw.shape.Shape;

import org.lwjgl.opengl.GL11;

import com.bencvt.minecraft.client.buildregion.region.Axis;

/**
 * A LibShapeDraw Shape representing a plane. This is purely cosmetic; see
 * PlaneRegion for the object that actually defines the plane.
 * <p>
 * The plane is rendered as an infinite 1-block-thick wireframe grid. Only a
 * few cells of the grid are rendered, clustered around the origin, which can
 * change. Cells further away from the origin are increasingly transparent.
 * Grid lines may be visible through terrain and other game objects.
 * 
 * @author bencvt
 */
public class PlaneShape extends Shape {
    /** Small offset used when rendering cubes to avoid ugly intersections */
    public static final double MARGIN = 1.0 / 32.0;
    public static final int PLANE_RENDER_RADIUS = 10;
    private static double[][] alphaTable; // lazily instantiated

    private Axis axis;
    private double coord;
    private Color colorFront;
    private Color colorBack;
    private Color colorSides;
    private float lineWidth;
    private double alphaHidden; // [0.0, 1.0] scale to apply to lines hidden by terrain
    private double alphaBase; // [0.0, 1.0] scale to apply to all lines

    // Internal arrays used by renderLines, persisted to the instance so we
    // don't keep pushing temporary objects to the heap every rendering frame.
    private final double[] baseCoords = {0.0, 0.0, 0.0};
    private final double[] curCoords = {0.0, 0.0, 0.0};

    public PlaneShape(Color colorFront, Color colorBack, Color colorSides, float lineWidth, double alphaHidden) {
        super(Vector3.ZEROS.copy());
        setRelativeToOrigin(false);
        axis = null;
        this.colorFront = colorFront;
        this.colorBack = colorBack;
        this.colorSides = colorSides;
        this.lineWidth = lineWidth;
        this.alphaHidden = alphaHidden;
        setAlphaBase(1.0);
    }

    public PlaneShape(PlaneShape other) {
        super(Vector3.ZEROS.copy());
        setRelativeToOrigin(false);
        copyFrom(other);
    }

    public void copyFrom(PlaneShape other) {
        getOrigin().set(other.getOrigin());
        axis = other.axis;
        coord = other.coord;
        colorFront = other.colorFront.copy();
        colorBack = other.colorBack.copy();
        colorSides = other.colorSides.copy();
        lineWidth = other.lineWidth;
        alphaHidden = other.alphaHidden;
        setAlphaBase(other.alphaBase);
    }

    @Override
    protected void renderShape(MinecraftAccess mc) {
        if (axis == null || alphaBase <= 0.0) {
            return;
        }
        GL11.glLineWidth(lineWidth);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        renderLines(mc, alphaBase);
        if (alphaHidden > 0.0) {
            GL11.glDepthFunc(GL11.GL_GREATER);
            renderLines(mc, alphaBase * alphaHidden);
        }
    }

    private void renderLines(MinecraftAccess mc, double alphaLine) {
        curCoords[0] = baseCoords[0] = getOriginReadonly().getX();
        curCoords[1] = baseCoords[1] = getOriginReadonly().getY();
        curCoords[2] = baseCoords[2] = getOriginReadonly().getZ();
        final int dim0;
        final int dim1;
        if (axis == Axis.X) {
            dim0 = 1;
            dim1 = 2;
        } else if (axis == Axis.Y) {
            dim0 = 0;
            dim1 = 2;
        } else if (axis == Axis.Z) {
            dim0 = 0;
            dim1 = 1;
        } else {
            throw new IllegalStateException();
        }
        baseCoords[dim0] = (int) baseCoords[dim0];
        baseCoords[dim1] = (int) baseCoords[dim1];

        for (int off0 = -PLANE_RENDER_RADIUS; off0 <= PLANE_RENDER_RADIUS; off0++) {
            curCoords[dim0] = baseCoords[dim0] + off0;
            for (int off1 = -PLANE_RENDER_RADIUS; off1 <= PLANE_RENDER_RADIUS; off1++) {
                curCoords[dim1] = baseCoords[dim1] + off1;
                final double alphaScale = alphaLine * getAlphaScale(off0, off1);
                if (alphaScale <= 0.0) {
                    continue;
                }
                final double x0 = curCoords[0] + MARGIN;
                final double x1 = curCoords[0] + 1 - MARGIN;
                final double y0 = curCoords[1] + MARGIN;
                final double y1 = curCoords[1] + 1 - MARGIN;
                final double z0 = curCoords[2] + MARGIN;
                final double z1 = curCoords[2] + 1 - MARGIN;
                GL11.glColor4d(colorBack.getRed(), colorBack.getGreen(), colorBack.getBlue(), colorBack.getAlpha() * alphaScale);
                // Because the front, back, and sides can have different
                // colors we need to check the axis again.
                if (axis == Axis.X) {
                    // west
                    mc.startDrawing(GL11.GL_LINE_LOOP);
                    mc.addVertex(x0, y0, z0);
                    mc.addVertex(x0, y1, z0);
                    mc.addVertex(x0, y1, z1);
                    mc.addVertex(x0, y0, z1);
                    mc.finishDrawing();
                    // east
                    GL11.glColor4d(colorFront.getRed(), colorFront.getGreen(), colorFront.getBlue(), colorFront.getAlpha() * alphaScale);
                    mc.startDrawing(GL11.GL_LINE_LOOP);
                    mc.addVertex(x1, y0, z0);
                    mc.addVertex(x1, y1, z0);
                    mc.addVertex(x1, y1, z1);
                    mc.addVertex(x1, y0, z1);
                    mc.finishDrawing();
                    // sides
                    GL11.glColor4d(colorSides.getRed(), colorSides.getGreen(), colorSides.getBlue(), colorSides.getAlpha() * alphaScale);
                    mc.startDrawing(GL11.GL_LINES);
                    mc.addVertex(x0, y0, z0);
                    mc.addVertex(x1, y0, z0);
                    mc.addVertex(x0, y1, z0);
                    mc.addVertex(x1, y1, z0);
                    mc.addVertex(x0, y1, z1);
                    mc.addVertex(x1, y1, z1);
                    mc.addVertex(x0, y0, z1);
                    mc.addVertex(x1, y0, z1);
                    mc.finishDrawing();
                } else if (axis == Axis.Y) {
                    // bottom
                    mc.startDrawing(GL11.GL_LINE_LOOP);
                    mc.addVertex(x0, y0, z0);
                    mc.addVertex(x1, y0, z0);
                    mc.addVertex(x1, y0, z1);
                    mc.addVertex(x0, y0, z1);
                    mc.finishDrawing();
                    // top
                    GL11.glColor4d(colorFront.getRed(), colorFront.getGreen(), colorFront.getBlue(), colorFront.getAlpha() * alphaScale);
                    mc.startDrawing(GL11.GL_LINE_LOOP);
                    mc.addVertex(x0, y1, z0);
                    mc.addVertex(x1, y1, z0);
                    mc.addVertex(x1, y1, z1);
                    mc.addVertex(x0, y1, z1);
                    mc.finishDrawing();
                    // sides
                    GL11.glColor4d(colorSides.getRed(), colorSides.getGreen(), colorSides.getBlue(), colorSides.getAlpha() * alphaScale);
                    mc.startDrawing(GL11.GL_LINES);
                    mc.addVertex(x0, y0, z0);
                    mc.addVertex(x0, y1, z0);
                    mc.addVertex(x1, y0, z0);
                    mc.addVertex(x1, y1, z0);
                    mc.addVertex(x1, y0, z1);
                    mc.addVertex(x1, y1, z1);
                    mc.addVertex(x0, y0, z1);
                    mc.addVertex(x0, y1, z1);
                    mc.finishDrawing();
                } else if (axis == Axis.Z) {
                    // north
                    mc.startDrawing(GL11.GL_LINE_LOOP);
                    mc.addVertex(x0, y0, z0);
                    mc.addVertex(x1, y0, z0);
                    mc.addVertex(x1, y1, z0);
                    mc.addVertex(x0, y1, z0);
                    mc.finishDrawing();
                    // south
                    GL11.glColor4d(colorFront.getRed(), colorFront.getGreen(), colorFront.getBlue(), colorFront.getAlpha() * alphaScale);
                    mc.startDrawing(GL11.GL_LINE_LOOP);
                    mc.addVertex(x0, y0, z1);
                    mc.addVertex(x1, y0, z1);
                    mc.addVertex(x1, y1, z1);
                    mc.addVertex(x0, y1, z1);
                    mc.finishDrawing();
                    // sides
                    GL11.glColor4d(colorSides.getRed(), colorSides.getGreen(), colorSides.getBlue(), colorSides.getAlpha() * alphaScale);
                    mc.startDrawing(GL11.GL_LINES);
                    mc.addVertex(x0, y0, z0);
                    mc.addVertex(x0, y0, z1);
                    mc.addVertex(x1, y0, z0);
                    mc.addVertex(x1, y0, z1);
                    mc.addVertex(x1, y1, z0);
                    mc.addVertex(x1, y1, z1);
                    mc.addVertex(x0, y1, z0);
                    mc.addVertex(x0, y1, z1);
                    mc.finishDrawing();
                }
            }
        }
    }

    private static double getAlphaScale(int off0, int off1) {
        //return Math.max(0.0, (PLANE_RENDER_RADIUS + 1 - Math.sqrt(off0*off0 + off1*off1)) / (PLANE_RENDER_RADIUS + 1));
        if (alphaTable == null) {
            final int R = PLANE_RENDER_RADIUS + 1;
            alphaTable = new double[R][R];
            for (int i = 0; i < R; i++) {
                for (int j = 0; j < R; j++) {
                    alphaTable[i][j] = Math.max(0.0, (R - Math.sqrt(i*i + j*j)) / R);
                }
            }
        }
        return alphaTable[Math.abs(off0)][Math.abs(off1)];
    }

    public Axis getAxis() {
        return axis;
    }
    public void setAxisAndOrigin(Axis axis, ReadonlyVector3 origin) {
        this.axis = axis;
        getOrigin().set(origin);
    }

    public double getCoord() {
        return coord;
    }
    public void setCoord(double coord) {
        this.coord = coord;
        if (axis == Axis.X) {
            getOrigin().setX(coord);
        } else if (axis == Axis.Y) {
            getOrigin().setY(coord);
        } else if (axis == Axis.Z) {
            getOrigin().setZ(coord);
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Keep up with the player, moving the shape along the plane.
     */
    public void updateProjection(ReadonlyVector3 playerCoords) {
        if (axis == Axis.X) {
            getOrigin().setY(playerCoords.getY()).setZ(playerCoords.getZ());
        } else if (axis == Axis.Y) {
            getOrigin().setX(playerCoords.getX()).setZ(playerCoords.getZ());
        } else if (axis == Axis.Z) {
            getOrigin().setX(playerCoords.getX()).setY(playerCoords.getY());
        }
    }

    public double getAlphaBase() {
        return alphaBase;
    }
    public PlaneShape setAlphaBase(double alphaBase) {
        this.alphaBase = alphaBase;
        return this;
    }

    public Color getColorFront() {
        return colorFront;
    }
    public Color getColorBack() {
        return colorBack;
    }
    public Color getColorSides() {
        return colorSides;
    }
}
