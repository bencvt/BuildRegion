package com.bencvt.minecraft.buildregion.ui.world;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import libshapedraw.MinecraftAccess;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import libshapedraw.transform.ShapeTranslate;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.region.Units;

/**
 * Intermediate base class for rendering a region using a vertex buffer.
 * Assembles a list of vertices defining block-aligned lines to outline the
 * region. This is a performance gain as the region doesn't have to be fully
 * analyzed every render frame.
 * <p>
 * If the client supports it, also use an OpenGL VBO (Vertex Buffer Object) to
 * push the vertices to VRAM, significantly increasing performance as we don't
 * have to re-send the vertices to OpenGL every render frame.
 * <p>
 * TODO: make abstract and have RegionSphere and RegionCylinder derive from this class
 * TODO: move VBO optimization to LibShapeDraw eventually
 * 
 * @author bencvt
 */
public class RenderVertexBuffer extends RenderBase {
    /** The VBO handle. */
    private int vboId;

    /** Also maintain a regular buffer in case the client doesn't support VBOs. */
    private final List<Vector3> vertexCache;

    /** To support animation, all vertices are relative to the lower corner of the AABB. */
    protected final ShapeTranslate vertexOffset;

    private final Vector3 aabbUpperCorner; 

    protected RenderVertexBuffer(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden, RegionBase region) {
        super(lineColorVisible, lineColorHidden, true);
        onUpdateOrigin(getOrigin().set(region.getOriginReadonly()));
        vertexCache = new ArrayList<Vector3>();
        vertexOffset = new ShapeTranslate();
        aabbUpperCorner = new Vector3();
        populateVertexCache(region);
        addTransform(vertexOffset);
    }

    @Override
    public void cleanup() {
        removeVBO();
        // No need to clear vertexCache; it's in local memory and will be GC'd.
    }

    @Override
    protected void renderLines(MinecraftAccess mc, ReadonlyColor lineColor) {
        lineColor.glApply(getAlphaBase() * 0.5);
        if (vboId == 0) {
            mc.startDrawing(GL11.GL_LINES);
            for (Vector3 vertex : vertexCache) {
                mc.addVertex(vertex);
            }
            mc.finishDrawing();
        } else {
            ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vboId);
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
            GL11.glDrawArrays(GL11.GL_LINES, 0, vertexCache.size());
            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
        }
    }

    @Override
    public boolean updateIfPossible(RegionBase region, boolean animate) {
        // No animation supported in this base class as we can't guarantee that
        // even if the AABBs are the same dimension the blocks inside are the
        // same.
        // 
        // Derived classes should (depending on the region type) be able to
        // guarantee this and thus support animation. Re-override this method.
        // The following helper methods may be relevant:
        //  - getAABBLowerCornerReadonly()
        //  - getAABBUpperCornerReadonly()
        //  - vertexOffset.animateStart()
        return false;
    }

    @Override
    public void updateObserverPosition(ReadonlyVector3 observerPosition) {
        // TODO: maybe implement a "chunk" system to cache a section of vertices, based on player position.
    }

    protected ReadonlyVector3 getAABBLowerCornerReadonly() {
        return vertexOffset.getTranslateXYZ();
    }

    protected ReadonlyVector3 getAABBUpperCornerReadonly() {
        return aabbUpperCorner;
    }

    protected void populateVertexCache(RegionBase region) {
        removeVBO();
        vertexCache.clear();

        Vector3 aabbLowerCorner = vertexOffset.getTranslateXYZ();
        region.getAABB(aabbLowerCorner, aabbUpperCorner);
        Units.WHOLE.clamp(aabbLowerCorner);
        Units.WHOLE.clamp(aabbUpperCorner);

        int offX = (int) aabbLowerCorner.getX();
        int offY = (int) aabbLowerCorner.getY();
        int offZ = (int) aabbLowerCorner.getZ();
        int sizeX = (int) aabbUpperCorner.getX() - offX + 1;
        int sizeY = (int) aabbUpperCorner.getY() - offY + 1;
        int sizeZ = (int) aabbUpperCorner.getZ() - offZ + 1;

        populateVertexCacheWork(vertexCache, region, offX, offY, offZ, sizeX, sizeY, sizeZ);
        createVBO();
    }

    /**
     * Loop through every block in the AABB, calling region.isInsideRegion at
     * least once for each. For interior blocks bordering an exterior block,
     * add the appropriate vertices to the cache.
     * <p>
     * Derived classes should override this method to use a more intelligent
     * algorithm if possible, tailored to the derived class's target region
     * type.
     * <p>
     * TODO: Enforce a size limit. Looping over millions of blocks in the main
     *       thread is a bad idea. And even if the client can chug through
     *       that, we still end up with a large vertex cache. This can slow
     *       rendering to a crawl, even with a VBO.
     */
    protected static void populateVertexCacheWork(List<Vector3> vertexCache, RegionBase region, int offX, int offY, int offZ, int sizeX, int sizeY, int sizeZ) {
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
                    // No offset for the vertex coords. The vertexOffset
                    // transform will handle that during rendering.
                    double x0 = x + CUBE_MARGIN;
                    double x1 = x + 1 - CUBE_MARGIN;
                    double y0 = y + CUBE_MARGIN;
                    double y1 = y + 1 - CUBE_MARGIN;
                    double z0 = z + CUBE_MARGIN;
                    double z1 = z + 1 - CUBE_MARGIN;
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

    /**
     * If the client supports VBOs, copy the contents of vertexCache to a new
     * VBO, and register it. Any previous VBO will be removed.
     */
    protected void createVBO() {
        if (!GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
            vboId = 0;
            return;
        }

        // Convert down to 32-bit floating point to cut our VRAM usage in half.
        FloatBuffer vbo = BufferUtils.createFloatBuffer(vertexCache.size() * 3);
        for (Vector3 vertex : vertexCache) {
            vbo.put((float) vertex.getX());
            vbo.put((float) vertex.getY());
            vbo.put((float) vertex.getZ());
        }
        vbo.flip();

        // Register the VBO, copying the float buffer into VRAM.
        removeVBO();
        vboId = ARBVertexBufferObject.glGenBuffersARB();
        ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vboId);
        ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vbo, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
        ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);

        // We could clear vertexCache at this point as it's redundant, but keep
        // it around just in case the VBO gets removed prematurely.
    }

    protected void removeVBO() {
        if (vboId == 0) {
            return;
        }
        ARBVertexBufferObject.glDeleteBuffersARB(vboId);
        vboId = 0;
    }
}
