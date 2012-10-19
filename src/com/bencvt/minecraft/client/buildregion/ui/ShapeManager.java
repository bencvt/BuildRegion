package com.bencvt.minecraft.client.buildregion.ui;

import libshapedraw.LibShapeDraw;
import libshapedraw.primitive.ReadonlyVector3;

import com.bencvt.minecraft.client.buildregion.Controller;
import com.bencvt.minecraft.client.buildregion.region.Axis;
import com.bencvt.minecraft.client.buildregion.region.RegionBase;

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

    public void animateFadeOut() {
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

    public void animateFadeIn(RegionBase buildRegion) {
        if (mainShape != null) {
            libShapeDraw.getShapes().remove(mainShape);
        }
        mainShape = buildRegion.createShape(controller.getBuildMode());
        libShapeDraw.addShape(mainShape);
        mainShape.fadeIn();
    }

    public void animateShift(Axis axis, double newCoord) {
        if (mainShape == null) {
            return;
        }
        mainShape.shift(axis, newCoord);
    }

    public void updateObserverPosition(ReadonlyVector3 playerCoords) {
        if (mainShape != null) {
            mainShape.updateObserverPosition(playerCoords);
        }
    }
}
