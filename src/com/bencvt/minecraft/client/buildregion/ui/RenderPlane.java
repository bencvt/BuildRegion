package com.bencvt.minecraft.client.buildregion.ui;

import libshapedraw.MinecraftAccess;
import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;

import org.lwjgl.opengl.GL11;

import com.bencvt.minecraft.client.buildregion.region.Axis;

/**
 * A LibShapeDraw Shape representing a plane. This is purely cosmetic; see
 * PlaneRegion for the object that actually defines the plane.
 * <p>
 * The plane is rendered as an infinite 1-block-thick wireframe grid. Only a
 * few cells of the grid are rendered, clustered around the origin, which can
 * change. Cells further away from the origin are increasingly transparent.
 * 
 * @author bencvt
 */
public class RenderPlane extends RenderBase {
    /** Small offset used when rendering cubes to avoid ugly intersections. */
    public static final double MARGIN = 1.0 / 32.0;
    /**
     * The sides of grid cubes (i.e., the lines parallel to the plane's axis)
     * are rendered more subtly.
     */
    public static final double ALPHA_SIDE = 0.5;
    /** How many grid cubes to render. */
    public static final int PLANE_RENDER_RADIUS = 10;
    /** Lazily instantiated lookup table. */
    private static double[][] alphaTable;

    private final Axis axis;

    // Internal arrays used by renderLines, persisted to the instance so we
    // don't keep pushing temporary objects to the heap every rendering frame.
    private final double[] baseCoords = {0.0, 0.0, 0.0};
    private final double[] curCoords = {0.0, 0.0, 0.0};

    public RenderPlane(Color lineColorVisible, Color lineColorHidden, Axis axis, double coord) {
        super(lineColorVisible, lineColorHidden);
        if (axis == null) {
            throw new NullPointerException();
        }
        this.axis = axis;
        setShiftAxis(axis);
        setShiftCoord(coord);
    }

    @Override
    protected void renderLines(MinecraftAccess mc, ReadonlyColor lineColor, double alphaLine) {
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
                GL11.glColor4d(
                        lineColor.getRed(),
                        lineColor.getGreen(),
                        lineColor.getBlue(),
                        lineColor.getAlpha() * alphaScale);
                // Because sides are rendered with a different transparency we
                // need to check the axis again.
                if (axis == Axis.X) {
                    // west
                    mc.startDrawing(GL11.GL_LINE_LOOP);
                    mc.addVertex(x0, y0, z0);
                    mc.addVertex(x0, y1, z0);
                    mc.addVertex(x0, y1, z1);
                    mc.addVertex(x0, y0, z1);
                    mc.finishDrawing();
                    // east
                    mc.startDrawing(GL11.GL_LINE_LOOP);
                    mc.addVertex(x1, y0, z0);
                    mc.addVertex(x1, y1, z0);
                    mc.addVertex(x1, y1, z1);
                    mc.addVertex(x1, y0, z1);
                    mc.finishDrawing();
                    // sides
                    GL11.glColor4d(
                            lineColor.getRed(),
                            lineColor.getGreen(),
                            lineColor.getBlue(),
                            lineColor.getAlpha() * alphaScale * ALPHA_SIDE);
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
                    mc.startDrawing(GL11.GL_LINE_LOOP);
                    mc.addVertex(x0, y1, z0);
                    mc.addVertex(x1, y1, z0);
                    mc.addVertex(x1, y1, z1);
                    mc.addVertex(x0, y1, z1);
                    mc.finishDrawing();
                    // sides
                    GL11.glColor4d(
                            lineColor.getRed(),
                            lineColor.getGreen(),
                            lineColor.getBlue(),
                            lineColor.getAlpha() * alphaScale * ALPHA_SIDE);
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
                    mc.startDrawing(GL11.GL_LINE_LOOP);
                    mc.addVertex(x0, y0, z1);
                    mc.addVertex(x1, y0, z1);
                    mc.addVertex(x1, y1, z1);
                    mc.addVertex(x0, y1, z1);
                    mc.finishDrawing();
                    // sides
                    GL11.glColor4d(
                            lineColor.getRed(),
                            lineColor.getGreen(),
                            lineColor.getBlue(),
                            lineColor.getAlpha() * alphaScale * ALPHA_SIDE);
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

    @Override
    public void setShiftAxis(Axis shiftAxis) {
        if (shiftAxis != axis) {
            throw new IllegalArgumentException("a plane can only shift along its axis");
        }
        super.setShiftAxis(shiftAxis);
    }

    /**
     * Keep up with the player, moving the shape along the plane.
     */
    @Override
    public void updateProjection(ReadonlyVector3 playerCoords) {
        if (axis == Axis.X) {
            getOrigin().setY(playerCoords.getY()).setZ(playerCoords.getZ());
        } else if (axis == Axis.Y) {
            getOrigin().setX(playerCoords.getX()).setZ(playerCoords.getZ());
        } else if (axis == Axis.Z) {
            getOrigin().setX(playerCoords.getX()).setY(playerCoords.getY());
        }
    }
}
