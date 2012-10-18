package com.bencvt.minecraft.client.buildregion.ui;

import libshapedraw.LibShapeDraw;
import libshapedraw.primitive.ReadonlyVector3;

import com.bencvt.minecraft.client.buildregion.region.Axis;
import com.bencvt.minecraft.client.buildregion.region.RegionBase;

/**
 * Manage LibShapeDraw Shape objects and animations... i.e., the in-world GUI.
 * 
 * @author bencvt
 */
public class ShapeManager {
    private final LibShapeDraw libShapeDraw;
    private RenderBase mainShape;
    private RenderBase prevShape;

    public ShapeManager(LibShapeDraw libShapeDraw) {
        this.libShapeDraw = libShapeDraw;
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
        mainShape = buildRegion.createShape();
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
