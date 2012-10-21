package com.bencvt.minecraft.client.buildregion.ui;

import java.util.ArrayList;
import java.util.HashMap;

import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

import com.bencvt.minecraft.client.buildregion.BuildMode;
import com.bencvt.minecraft.client.buildregion.Controller;
import com.bencvt.minecraft.client.buildregion.region.Axis;
import com.bencvt.minecraft.client.buildregion.region.RegionBase;
import com.bencvt.minecraft.client.buildregion.region.RegionFactory;
import com.bencvt.minecraft.client.buildregion.region.RegionType;

/**
 * The main BuildRegion GUI.
 * 
 * @author bencvt
 */
public class GuiBuildRegion extends GuiScreen {
    public static final int ROW_SPACING = 2;
    public static final int PAD = 2;
    public static final int BORDER_THICKNESS = 1;
    public static final int WINDOW_Y_ADJUST = -16;
    public static final int HEADER_ARGB            = Color.WHITE.getARGB();
    public static final int BACKGROUND_ARGB        = Color.BLACK.copy().setAlpha(3.0/16.0).getARGB();
    public static final ReadonlyColor BORDER_COLOR = Color.BLACK.copy().setAlpha(1.0/8.0);
    public static final int BORDER_ARGB            = BORDER_COLOR.getARGB();
    public static final ReadonlyColor SELECT_COLOR = Color.DODGER_BLUE;

    private static final RegionType LONGEST_SUBTABLE = RegionType.CYLINDER; // for calculating window height

    private final Controller controller;
    private final GuiEnumSelect<BuildMode>    inputBuildMode;
    private final GuiEnumSelect<RegionType>   inputRegionType;
    private final GuiHLine                    hLine;
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
    private final RegionFactory regionFactory;

    public GuiBuildRegion(Controller controller, FontRenderer fr) {
        this.controller = controller;

        // Create all controls.
        inputBuildMode = new GuiEnumSelect<BuildMode>("build mode:", fr, BuildMode.values(), null);
        for (BuildMode mode : BuildMode.values()) {
            inputBuildMode.setOptionColor(mode, mode.colorVisible);
        }
        inputRegionType = new GuiEnumSelect<RegionType>("region type:", fr, RegionType.values(), SELECT_COLOR);
        hLine = new GuiHLine(fr, BORDER_THICKNESS, BORDER_COLOR);
        inputPlaneAxis = new GuiEnumSelect<Axis>("axis:", fr, Axis.values(), SELECT_COLOR);
        inputPlaneCoord = new GuiInputDouble("x coordinate:", fr);
        inputCylinderOriginX = new GuiInputDouble("origin x:", fr);
        inputCylinderOriginY = new GuiInputDouble("origin y:", fr);
        inputCylinderOriginZ = new GuiInputDouble("origin z:", fr);
        inputCylinderAxis = new GuiEnumSelect<Axis>("axis:", fr, Axis.values(), SELECT_COLOR);
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
        controlsByRegionType.get(RegionType.NONE).add(hLine);
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
        regionFactory = new RegionFactory(controller.getPrototypeRegion());
        // TODO: remember the current active Region for the "Undo" button
        inputBuildMode.setSelectedValue(controller.getBuildMode().getValue(), false);
        if (controller.isRegionActive()) {
            inputRegionType.setSelectedValue(controller.getPrototypeRegion().getRegionType(), false);
        } else {
            inputRegionType.setSelectedValue(RegionType.NONE, false);
        }
        inputPlaneAxis.setSelectedValue(regionFactory.getPlane().getAxis(), false);
        inputPlaneCoord.setValue(regionFactory.getPlane().getCoord(regionFactory.getPlane().getAxis()));
        // TODO: other region types
        inputCylinderOriginX.setValue(32.5);
        inputCylinderOriginY.setValue(75.0);
        inputCylinderOriginZ.setValue(-322.0);
        inputCylinderAxis.setSelectedValue(Axis.Y, false);
        inputCylinderHeight.setValue(3.0);
        inputCylinderRadiusA.setValue(8.0);
        inputCylinderRadiusB.setValue(8.0);

        updateControlProperties();

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
        windowHeight += BORDER_THICKNESS*2 + PAD*2;

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
        windowWidth += BORDER_THICKNESS*2 + PAD*2;

        // Center everything.
        windowXPosition = (width - windowWidth) / 2;
        windowYPosition = (height - windowHeight) / 2 + WINDOW_Y_ADJUST;

        // [Re]position all controls.
        final int xPos = windowXPosition + BORDER_THICKNESS + PAD;
        int yPos = windowYPosition + BORDER_THICKNESS + PAD;
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

        // TODO: possibly allow the user to look around by holding right-click and moving around
        // TODO: pressing B or Esc is the same as pressing the "OK" button
        // TODO: buttons to rotate the region around its origin (for planes, fill in using player coords)
        // TODO: eliminate click sounds
        // TODO: row mouseover tooltips
        // TODO: add footer with buttons ("OK", "Usage", "Undo" (gray out initially))
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
    public void drawScreen(int xMouse, int yMouse, float partialTick) {
        final int xLeft   = windowXPosition;
        final int xRight  = windowXPosition + windowWidth;
        final int yTop    = windowYPosition;
        final int yBottom = windowYPosition + windowHeight;

        // Draw header above the window.
        mc.fontRenderer.drawString(controller.getModTitle(),
                xLeft + (windowWidth - mc.fontRenderer.getStringWidth(controller.getModTitle())) / 2,
                yTop - mc.fontRenderer.FONT_HEIGHT - PAD,
                HEADER_ARGB);

        // Draw background.
        drawRect(xLeft, yTop, xRight, yBottom, BACKGROUND_ARGB);

        // Draw border on top of background.
        if (BORDER_THICKNESS > 0) {
            // Draw top border, including corners.
            drawRect(
                    xLeft,
                    yTop,
                    xRight,
                    yTop + BORDER_THICKNESS,
                    BORDER_ARGB);
            // Draw bottom border, including corners.
            drawRect(
                    xLeft,
                    yBottom - BORDER_THICKNESS,
                    xRight,
                    yBottom,
                    BORDER_ARGB);
            // Draw left border, excluding corners.
            drawRect(
                    xLeft,
                    yTop + BORDER_THICKNESS,
                    xLeft + BORDER_THICKNESS,
                    yBottom - BORDER_THICKNESS,
                    BORDER_ARGB);
            // Draw right border, excluding corners.
            drawRect(
                    xRight - BORDER_THICKNESS,
                    yTop + BORDER_THICKNESS,
                    xRight,
                    yBottom - BORDER_THICKNESS,
                    BORDER_ARGB);
        }

        // Defer control rendering to parent class.
        super.drawScreen(xMouse, yMouse, partialTick);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) {
        if (guiButton == inputBuildMode) {
            controller.cmdMode(inputBuildMode.getSelectedValue());
        } else {
            RegionBase activeRegion = regionFactory.getRegionAs(inputRegionType.getSelectedValue());
            if (activeRegion == null) {
                controller.cmdClear();
            } else {
                controller.cmdSet(activeRegion);
            }
        }
        updateControlProperties();
    }
}
