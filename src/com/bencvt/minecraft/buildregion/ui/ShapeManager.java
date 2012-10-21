package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.LibShapeDraw;
import libshapedraw.primitive.ReadonlyVector3;

import com.bencvt.minecraft.buildregion.Controller;
import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.region.RegionCuboid;
import com.bencvt.minecraft.buildregion.region.RegionCylinder;
import com.bencvt.minecraft.buildregion.region.RegionPlane;
import com.bencvt.minecraft.buildregion.region.RegionSphere;

/**
 * Manage LibShapeDraw Shape objects and animations... i.e., the in-world GUI.
 * 
 * @author bencvt
 */
public class ShapeManager {
    private final Controller controller;
    private final LibShapeDraw libShapeDraw;
    private RenderBase mainShape;
    private RenderBase prevShape;

    public ShapeManager(Controller controller, LibShapeDraw libShapeDraw) {
        this.controller = controller;
        this.libShapeDraw = libShapeDraw;
    }

    public void reset() {
        if (mainShape == null) {
            libShapeDraw.getShapes().remove(mainShape);
        }
        mainShape = null;
        if (prevShape != null) {
            libShapeDraw.getShapes().remove(prevShape);
        }
        prevShape = null;
    }

    public void updateRegion(RegionBase region) {
        if (region == null) {
            removeShape();
            return;
        }
        if (mainShape != null && mainShape.updateIfPossible(region)) {
            return;
        }
        removeShape();
        mainShape = createShape(region);
        libShapeDraw.addShape(mainShape);
        mainShape.fadeIn();
    }

    private void removeShape() {
        if (mainShape == null) {
            return;
        }
        if (prevShape != null) {
            libShapeDraw.getShapes().remove(prevShape);
        }
        prevShape = mainShape;
        mainShape = null;
        prevShape.fadeOut();
    }

    private RenderBase createShape(RegionBase region) {
        switch (region.getRegionType()) {
        case NONE:
            break;
        case PLANE:
            RegionPlane plane = (RegionPlane) region;
            return new RenderPlane(
                    controller.getBuildMode().getColorVisible(),
                    controller.getBuildMode().getColorHidden(),
                    plane.getAxis(),
                    plane.getCoord());
        case CUBOID:
            RegionCuboid cuboid = (RegionCuboid) region;
            // TODO
            return null;
        case CYLINDER:
            RegionCylinder cylinder = (RegionCylinder) region;
            // TODO
            return null;
        case SPHERE:
            RegionSphere sphere = (RegionSphere) region;
            // TODO
            return null;
        }
        throw new IllegalStateException();
    }

    public void updateObserverPosition(ReadonlyVector3 playerCoords) {
        if (mainShape != null) {
            mainShape.updateObserverPosition(playerCoords);
        }
    }
}
