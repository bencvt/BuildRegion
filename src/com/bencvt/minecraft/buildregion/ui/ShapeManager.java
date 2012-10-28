package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.LibShapeDraw;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

import com.bencvt.minecraft.buildregion.Controller;
import com.bencvt.minecraft.buildregion.region.Axis;
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
    private boolean guiScreenActive;

    public ShapeManager(Controller controller, LibShapeDraw libShapeDraw) {
        this.controller = controller;
        this.libShapeDraw = libShapeDraw;
    }

    public void reset() {
        libShapeDraw.getShapes().clear();
        mainShape = null;
        prevShape = null;
    }

    public void updateRegion(RegionBase region, boolean animate) {
        if (region == null) {
            removeShape(animate);
            return;
        }
        if (mainShape != null && mainShape.updateIfPossible(region, animate)) {
            return;
        }
        removeShape(animate);
        mainShape = createShape(region);
        mainShape.setRenderMarkersNow(guiScreenActive);
        libShapeDraw.addShape(mainShape);
        mainShape.animateFadeIn(animate);
    }

    private void removeShape(boolean animate) {
        if (mainShape == null) {
            return;
        }
        if (prevShape != null) {
            libShapeDraw.getShapes().remove(prevShape);
        }
        prevShape = mainShape;
        mainShape = null;
        prevShape.animateFadeOut(animate);
    }

    private RenderBase createShape(RegionBase region) {
        switch (region.getRegionType()) {
        case NONE:
            break;
        case PLANE:
            return new RenderPlane(
                    controller.getBuildMode().getColorVisible(),
                    controller.getBuildMode().getColorHidden(),
                    (RegionPlane) region);
        case CUBOID:
            return new RenderCuboid(
                    controller.getBuildMode().getColorVisible(),
                    controller.getBuildMode().getColorHidden(),
                    (RegionCuboid) region);
        case CYLINDER:
            return new RenderCylinder(
                    controller.getBuildMode().getColorVisible(),
                    controller.getBuildMode().getColorHidden(),
                    (RegionCylinder) region);
        case SPHERE:
            return new RenderSphere(
                    controller.getBuildMode().getColorVisible(),
                    controller.getBuildMode().getColorHidden(),
                    (RegionSphere) region);
        }
        throw new IllegalStateException();
    }

    public void updateObserverPosition(ReadonlyVector3 playerCoords) {
        if (mainShape != null) {
            mainShape.updateObserverPosition(playerCoords);
        }
    }

    public void setGuiScreenActive(boolean guiScreenActive) {
        this.guiScreenActive = guiScreenActive;
        if (mainShape != null) {
            mainShape.setRenderMarkersNow(guiScreenActive);
        }
        if (prevShape != null) {
            prevShape.setRenderMarkersNow(guiScreenActive);
        }
    }
}
