package com.bencvt.minecraft.client.buildregion.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import libshapedraw.primitive.Color;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ScaledResolution;

import com.bencvt.minecraft.client.buildregion.BuildMode;
import com.bencvt.minecraft.client.buildregion.Controller;
import com.bencvt.minecraft.client.buildregion.region.Axis;
import com.bencvt.minecraft.client.buildregion.region.RegionType;

/**
 * The main BuildRegion GUI.
 * 
 * @author bencvt
 */
public class GuiBuildRegion extends GuiScreen {
    public static final int ROW_SPACING = 2;
    public static final int PAD = 2;
    public static final int BACKGROUND_ARGB = Color.BLACK.copy().setAlpha(0.25).getARGB();
    private static final RegionType LONGEST_SUBTABLE = RegionType.CYLINDER; // for calculating window height

    private final Controller controller;
    private final GuiEnumSelect<BuildMode>    inputBuildMode;
    private final GuiEnumSelect<RegionType>   inputRegionType;
    private final GuiEnumSelect<Axis>         inputPlaneAxis;
    private final GuiInputDouble              inputPlaneCoord;
    private final GuiInputDouble              inputCylinderOriginX;
    private final GuiInputDouble              inputCylinderOriginY;
    private final GuiInputDouble              inputCylinderOriginZ;
    private final GuiEnumSelect<Axis>         inputCylinderAxis;
    private final GuiInputDouble              inputCylinderHeight;
    private final GuiInputDouble              inputCylinderRadiusA;
    private final GuiInputDouble              inputCylinderRadiusB;
    private final HashMap<RegionType, ArrayList<GuiLabeledControl>> controlsByRegionType;
    // Position, height, and width are calculated in initGui().
    private int windowXPosition;
    private int windowYPosition;
    private int windowHeight;
    private int windowWidth;

    public GuiBuildRegion(Controller controller, FontRenderer fr) {
        this.controller = controller;

        // Create all controls.
        inputBuildMode = new GuiEnumSelect<BuildMode>("build mode:", fr, BuildMode.values(), null);
        for (BuildMode mode : BuildMode.values()) {
            inputBuildMode.setOptionColor(mode, mode.lineColorVisible);
        }
        inputRegionType = new GuiEnumSelect<RegionType>("region type:", fr, RegionType.values(), BuildMode.activeLineColorVisible);
        inputPlaneAxis = new GuiEnumSelect<Axis>("axis:", fr, Axis.values(), BuildMode.activeLineColorVisible);
        inputPlaneCoord = new GuiInputDouble("x coordinate:", fr);
        inputCylinderOriginX = new GuiInputDouble("origin x:", fr);
        inputCylinderOriginY = new GuiInputDouble("origin y:", fr);
        inputCylinderOriginZ = new GuiInputDouble("origin z:", fr);
        inputCylinderAxis = new GuiEnumSelect<Axis>("axis:", fr, Axis.values(), BuildMode.activeLineColorVisible);
        inputCylinderHeight = new GuiInputDouble("height:", fr);
        inputCylinderRadiusA = new GuiInputDouble("x radius:", fr);
        inputCylinderRadiusB = new GuiInputDouble("y radius:", fr);

        // Add each control to the appropriate list.
        controlsByRegionType = new HashMap<RegionType, ArrayList<GuiLabeledControl>>();
        for (RegionType regionType : RegionType.values()) {
            controlsByRegionType.put(regionType, new ArrayList<GuiLabeledControl>());
        }
        controlsByRegionType.get(RegionType.NONE).add(inputBuildMode);
        controlsByRegionType.get(RegionType.NONE).add(inputRegionType);
        controlsByRegionType.get(RegionType.PLANE).add(inputPlaneAxis);
        controlsByRegionType.get(RegionType.PLANE).add(inputPlaneCoord);
        controlsByRegionType.get(RegionType.CYLINDER).add(inputCylinderOriginX);
        controlsByRegionType.get(RegionType.CYLINDER).add(inputCylinderOriginY);
        controlsByRegionType.get(RegionType.CYLINDER).add(inputCylinderOriginZ);
        controlsByRegionType.get(RegionType.CYLINDER).add(inputCylinderAxis);
        controlsByRegionType.get(RegionType.CYLINDER).add(inputCylinderHeight);
        controlsByRegionType.get(RegionType.CYLINDER).add(inputCylinderRadiusA);
        controlsByRegionType.get(RegionType.CYLINDER).add(inputCylinderRadiusB);

        // Populate the controls' contents from the controller.
        // TODO: use actual values
        // TODO: maintain individual Region instances for each type
        // TODO: coerce the active Region into instances for the other types with reasonable defaults
        // TODO: make controller accept a Region as a live preview
        // TODO: remember the current active Region for the "Cancel" button
        // TODO: possibly allow the user to look around by holding right-click and moving around
        inputBuildMode.setSelectedValue(BuildMode.getActiveMode(), false);
        inputRegionType.setSelectedValue(RegionType.PLANE, false);
        inputPlaneAxis.setSelectedValue(Axis.X, false);
        inputPlaneCoord.setValue(-4213.5);
        inputCylinderOriginX.setValue(32.5);
        inputCylinderOriginY.setValue(75.0);
        inputCylinderOriginZ.setValue(-322.0);
        inputCylinderAxis.setSelectedValue(Axis.Y, false);
        inputCylinderHeight.setValue(3.0);
        inputCylinderRadiusA.setValue(8.0);
        inputCylinderRadiusB.setValue(8.0);

        // Defer positioning and width adjustments until initGui().
    }

    @Override
    public void initGui() {
        // Calculate maximum widths so the table's columns align.
        // Calculate the window height, assuming the subtable with the most rows is being displayed.
        int maxLabelWidth = 0;
        int maxControlWidth = 0;
        windowHeight = 0;
        for (RegionType regionType : RegionType.values()) {
            for (GuiLabeledControl control : controlsByRegionType.get(regionType)) {
                maxLabelWidth = Math.max(maxLabelWidth, control.getLabelWidth());
                maxControlWidth = Math.max(maxControlWidth, control.getControlWidth());

                if (regionType == RegionType.NONE || regionType == LONGEST_SUBTABLE) {
                    if (windowHeight > 0) {
                        windowHeight += ROW_SPACING;
                    }
                    windowHeight += control.getHeight();
                }
            }
        }
        windowHeight += PAD + PAD;

        // Adjust controls' widths to match the above.
        // Calculate the window width.
        // Add all controls to the list of controls to render.
        windowWidth = 0;
        for (RegionType regionType : RegionType.values()) {
            for (GuiLabeledControl control : controlsByRegionType.get(regionType)) {
                control.setLabelWidth(maxLabelWidth);
                control.setControlWidth(maxControlWidth);

                windowWidth = Math.max(windowWidth, control.getWidth());

                controlList.add(control);
            }
        }
        windowWidth += PAD + PAD;

        // Center everything.
        windowXPosition = (width - windowWidth) / 2;
        windowYPosition = (height - windowHeight) / 2;

        // [Re]position all controls.
        final int xPos = windowXPosition + PAD;
        int yPos = windowYPosition + PAD;
        int yPosBack = yPos;
        for (RegionType regionType : RegionType.values()) {
            for (GuiLabeledControl control : controlsByRegionType.get(regionType)) {
                yPos += control.position(xPos, yPos) + ROW_SPACING;
            }
            if (regionType == RegionType.NONE) {
                yPosBack = yPos;
            } else {
                yPos = yPosBack;
            }
        }
        // TODO: add header with "BuildRegion vX.Y" text, no background
        // TODO: add horizontal line between permanent rows and region-specific row
        // TODO: add footer with buttons ("OK", "Usage", "Cancel")
        // TODO: some way to lock two or three GuiInputDoubles together for shared radiuses

        updateControlProperties();
    }

    public void updateControlProperties() {
        RegionType activeRegionType = inputRegionType.getSelectedValue();
        for (RegionType regionType : RegionType.values()) {
            for (GuiLabeledControl control : controlsByRegionType.get(regionType)) {
                control.drawButton = (regionType == RegionType.NONE || regionType == activeRegionType);
            }
        }

        inputPlaneCoord.displayString = inputPlaneAxis.getSelectedValue().toString().toLowerCase() + " coordinate:";

        Axis cylAxis = inputCylinderAxis.getSelectedValue();
        if (cylAxis == Axis.X) {
            inputCylinderRadiusA.displayString = "y radius";
            inputCylinderRadiusB.displayString = "z radius";
        } else if (cylAxis == Axis.Y) {
            inputCylinderRadiusA.displayString = "x radius";
            inputCylinderRadiusB.displayString = "z radius";
        } else if (cylAxis == Axis.Z) {
            inputCylinderRadiusA.displayString = "x radius";
            inputCylinderRadiusB.displayString = "y radius";
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        drawRect(
                windowXPosition,
                windowYPosition,
                windowXPosition + windowWidth,
                windowYPosition + windowHeight,
                BACKGROUND_ARGB);
        super.drawScreen(mouseX, mouseY, partialTick);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) {
        if (guiButton == inputBuildMode) {
            controller.cmdMode(inputBuildMode.getSelectedValue());
            return;
        }
        if (guiButton == inputRegionType) {
            if (inputRegionType.getSelectedValue() == null) {
                controller.cmdClear(true);
            }
            // TODO
        }
        updateControlProperties();
    }
}
