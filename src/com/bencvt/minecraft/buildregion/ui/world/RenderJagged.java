package com.bencvt.minecraft.buildregion.ui.world;

import java.util.ArrayList;
import java.util.HashMap;

import libshapedraw.MinecraftAccess;
import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

import org.lwjgl.opengl.GL11;

import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.region.Units;

/**
 * Intermediate base class for rendering a non-cuboid region. Assembles a list
 * of vertices defining block-aligned lines to outline the region.
 * 
 * TODO: make abstract and have RegionSphere and RegionCylinder derive from this class
 * 
 * @author bencvt
 */
public class RenderJagged extends RenderBase {
    protected final ArrayList<Vector3> vertexCache;

    protected RenderJagged(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden, RegionBase region) {
        super(lineColorVisible, lineColorHidden, true);
        onUpdateOrigin(getOrigin().set(region.getOriginReadonly()));
        vertexCache = new ArrayList<Vector3>();
        populateVertexCache(region);
    }

    protected void populateVertexCache(RegionBase region) {
        vertexCache.clear();

        final Vector3 lower = new Vector3();
        final Vector3 upper = new Vector3();
        region.getAABB(lower, upper);
        Units.WHOLE.clamp(lower);
        Units.WHOLE.clamp(upper);

        final int offX = (int) lower.getX();
        final int offY = (int) lower.getY();
        final int offZ = (int) lower.getZ();
        final int sizeX = (int) upper.getX() - offX + 1;
        final int sizeY = (int) upper.getY() - offY + 1;
        final int sizeZ = (int) upper.getZ() - offZ + 1;
        // TODO: enforce a size limit. Looping over millions of blocks is a bad idea, and more importantly having a large vertex cache will slow rendering to a crawl.

        // Loop through every block in the AABB, calling region.isInsideRegion
        // at least once for each. For interior blocks bordering an exterior
        // block, add the appropriate vertices to the cache.
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    if (!region.isInsideRegion(offX + x, offY + y, offZ + z)) {
                        continue;
                    }
                    // Some of these calls to isInsideRegion will be outside
                    // of the AABB, but that's not a problem.
                    boolean west  = region.isInsideRegion(offX + x - 1, offY + y, offZ + z);
                    boolean east  = region.isInsideRegion(offX + x + 1, offY + y, offZ + z);
                    boolean down  = region.isInsideRegion(offX + x, offY + y - 1, offZ + z);
                    boolean up    = region.isInsideRegion(offX + x, offY + y + 1, offZ + z);
                    boolean north = region.isInsideRegion(offX + x, offY + y, offZ + z - 1);
                    boolean south = region.isInsideRegion(offX + x, offY + y, offZ + z + 1);
                    if (west && east && down && up && north && south) {
                        // Interior block is completely surrounded by other
                        // interior blocks.
                        continue;
                    }
                    double x0 = offX + x + CUBE_MARGIN;
                    double x1 = offX + x + 1 - CUBE_MARGIN;
                    double y0 = offY + y + CUBE_MARGIN;
                    double y1 = offY + y + 1 - CUBE_MARGIN;
                    double z0 = offZ + z + CUBE_MARGIN;
                    double z1 = offZ + z + 1 - CUBE_MARGIN;
                    if (!west || !down) {
                        vertexCache.add(new Vector3(x0, y0, z0));
                        vertexCache.add(new Vector3(x0, y0, z1));
                    }
                    if (!west || !up) {
                        vertexCache.add(new Vector3(x0, y1, z0));
                        vertexCache.add(new Vector3(x0, y1, z1));
                    }
                    if (!west || !north) {
                        vertexCache.add(new Vector3(x0, y0, z0));
                        vertexCache.add(new Vector3(x0, y1, z0));
                    }
                    if (!west || !south) {
                        vertexCache.add(new Vector3(x0, y0, z1));
                        vertexCache.add(new Vector3(x0, y1, z1));
                    }
                    if (!down || !north) {
                        vertexCache.add(new Vector3(x0, y0, z0));
                        vertexCache.add(new Vector3(x1, y0, z0));
                    }
                    if (!down || !south) {
                        vertexCache.add(new Vector3(x0, y0, z1));
                        vertexCache.add(new Vector3(x1, y0, z1));
                    }
                    if (!up || !north) {
                        vertexCache.add(new Vector3(x0, y1, z0));
                        vertexCache.add(new Vector3(x1, y1, z0));
                    }
                    if (!up || !south) {
                        vertexCache.add(new Vector3(x0, y1, z1));
                        vertexCache.add(new Vector3(x1, y1, z1));
                    }
                    if (!east || !down) {
                        vertexCache.add(new Vector3(x1, y0, z0));
                        vertexCache.add(new Vector3(x1, y0, z1));
                    }
                    if (!east || !up) {
                        vertexCache.add(new Vector3(x1, y1, z0));
                        vertexCache.add(new Vector3(x1, y1, z1));
                    }
                    if (!east || !north) {
                        vertexCache.add(new Vector3(x1, y0, z0));
                        vertexCache.add(new Vector3(x1, y1, z0));
                    }
                    if (!east || !south) {
                        vertexCache.add(new Vector3(x1, y0, z1));
                        vertexCache.add(new Vector3(x1, y1, z1));
                    }
                }
            }
        }
    }

    @Override
    protected void renderLines(MinecraftAccess mc, ReadonlyColor lineColor) {
        lineColor.glApply(getAlphaBase() * 0.5);
        mc.startDrawing(GL11.GL_LINES);
        // TODO: we're essentially copying from buffer to buffer every rendering frame. Investigate whether it would be a significant performance boost to bypass Minecraft's tessellator, interacting with OpenGL directly instead.
        for (Vector3 vertex : vertexCache) {
            mc.addVertex(vertex);
        }
        mc.finishDrawing();
    }

    @Override
    public boolean updateIfPossible(RegionBase region, boolean animate) {
        // TODO: determine how to animate this
        return false;
    }

    @Override
    public void updateObserverPosition(ReadonlyVector3 observerPosition) {
        // TODO: maybe implement a "chunk" system to cache a section of vertices, based on player position.
    }
}
