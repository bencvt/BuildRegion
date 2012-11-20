package com.bencvt.minecraft.buildregion.ui.world;

import libshapedraw.LibShapeDraw;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.ReadonlyVector3;

import com.bencvt.minecraft.buildregion.Controller;
import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.region.RegionCuboid;
import com.bencvt.minecraft.buildregion.region.RegionCylinder;
import com.bencvt.minecraft.buildregion.region.RegionPlane;
import com.bencvt.minecraft.buildregion.region.RegionSphere;
import com.bencvt.minecraft.buildregion.region.RegionType;

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

    public ShapeManager(Controller controller) {
        this.controller = controller;
        libShapeDraw = new LibShapeDraw().verifyInitialized();
    }

    public void reset() {
        libShapeDraw.getShapes().clear();
        if (mainShape != null) {
            mainShape.cleanup();
        }
        if (prevShape != null) {
            prevShape.cleanup();
        }
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
        libShapeDraw.addShape(mainShape);
        mainShape.animateFadeIn(animate);
    }

    private void removeShape(boolean animate) {
        if (mainShape == null) {
            return;
        }
        if (prevShape != null) {
            libShapeDraw.removeShape(prevShape);
            prevShape.cleanup();
        }
        prevShape = mainShape;
        mainShape = null;
        prevShape.animateFadeOut(animate);
    }

    private RenderBase createShape(RegionBase region) {
        final ReadonlyColor colorVisible = controller.getBuildMode().getColorVisible();
        final ReadonlyColor colorHidden = controller.getBuildMode().getColorHidden();
        final RegionType r = region.getRegionType();
        if (r == RegionType.PLANE) {
            return new RenderPlane(colorVisible, colorHidden, (RegionPlane) region);
        } else if (r == RegionType.CUBOID) {
            return new RenderCuboid(colorVisible, colorHidden, (RegionCuboid) region);
        } else if (r == RegionType.CYLINDER) {
            return new RenderCylinder(colorVisible, colorHidden, (RegionCylinder) region);
        } else if (r == RegionType.SPHERE) {
            return new RenderSphere(colorVisible, colorHidden, (RegionSphere) region);
        } else {
            throw new IllegalStateException();
        }
    }

    public void updateObserverPosition(ReadonlyVector3 playerCoords) {
        if (mainShape != null) {
            mainShape.updateObserverPosition(playerCoords);
        }
    }
}
