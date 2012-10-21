package com.bencvt.minecraft.buildregion.ui;

import java.util.ArrayList;
import java.util.HashMap;

import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

import com.bencvt.minecraft.buildregion.BuildMode;
import com.bencvt.minecraft.buildregion.Controller;
import com.bencvt.minecraft.buildregion.region.Axis;
import com.bencvt.minecraft.buildregion.region.RegionFactory;
import com.bencvt.minecraft.buildregion.region.RegionType;

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
    private final GuiEmptyRow                 emptyRow;
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
    private final GuiInputDouble              inputSphereOriginX;
    private final GuiInputDouble              inputSphereOriginY;
    private final GuiInputDouble              inputSphereOriginZ;
    private final GuiInputDouble              inputSphereRadiusX;
    private final GuiInputDouble              inputSphereRadiusY;
    private final GuiInputDouble              inputSphereRadiusZ;
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
        emptyRow = new GuiEmptyRow(fr);
        inputBuildMode = new GuiEnumSelect<BuildMode>("build mode:", fr, BuildMode.values(), null);
        for (BuildMode mode : BuildMode.values()) {
            inputBuildMode.setOptionColor(mode, mode.colorVisible);
        }
        inputRegionType = new GuiEnumSelect<RegionType>("region type:", fr, RegionType.values(), SELECT_COLOR);
        hLine = new GuiHLine(fr, BORDER_THICKNESS, BORDER_COLOR);
        inputPlaneAxis = new GuiEnumSelect<Axis>("axis:", fr, Axis.values(), SELECT_COLOR);
        inputPlaneCoord = new GuiInputDouble("x coordinate:", fr);
        // TODO: cuboid inputs
        inputCylinderOriginX = new GuiInputDouble("origin x:", fr);
        inputCylinderOriginY = new GuiInputDouble("origin y:", fr);
        inputCylinderOriginZ = new GuiInputDouble("origin z:", fr);
        inputCylinderAxis = new GuiEnumSelect<Axis>("axis:", fr, Axis.values(), SELECT_COLOR);
        inputCylinderHeight = new GuiInputDouble("height:", fr);
        inputCylinderRadiusA = new GuiInputDouble("x radius:", fr);
        inputCylinderRadiusB = new GuiInputDouble("y radius:", fr);
        inputSphereOriginX = new GuiInputDouble("origin x:", fr);
        inputSphereOriginY = new GuiInputDouble("origin y:", fr);
        inputSphereOriginZ = new GuiInputDouble("origin z:", fr);
        inputSphereRadiusX = new GuiInputDouble("x radius:", fr);
        inputSphereRadiusY = new GuiInputDouble("y radius:", fr);
        inputSphereRadiusZ = new GuiInputDouble("z radius:", fr);

        // Add each control to the appropriate list.
        controlsByRegionType = new HashMap<RegionType, ArrayList<GuiLabeledControl>>();
        for (RegionType regionType : RegionType.values()) {
            controlsByRegionType.put(regionType, new ArrayList<GuiLabeledControl>());
        }
        controlsByRegionType.get(RegionType.NONE).add(inputBuildMode);
        controlsByRegionType.get(RegionType.NONE).add(inputRegionType);
        controlsByRegionType.get(RegionType.NONE).add(hLine);
        controlsByRegionType.get(RegionType.PLANE).add(emptyRow);
        controlsByRegionType.get(RegionType.PLANE).add(emptyRow);
        controlsByRegionType.get(RegionType.PLANE).add(inputPlaneCoord);
        controlsByRegionType.get(RegionType.PLANE).add(inputPlaneAxis);
        // TODO: cuboid inputs
        controlsByRegionType.get(RegionType.CYLINDER).add(inputCylinderOriginX);
        controlsByRegionType.get(RegionType.CYLINDER).add(inputCylinderOriginY);
        controlsByRegionType.get(RegionType.CYLINDER).add(inputCylinderOriginZ);
        controlsByRegionType.get(RegionType.CYLINDER).add(inputCylinderAxis);
        controlsByRegionType.get(RegionType.CYLINDER).add(inputCylinderHeight);
        controlsByRegionType.get(RegionType.CYLINDER).add(inputCylinderRadiusA);
        controlsByRegionType.get(RegionType.CYLINDER).add(inputCylinderRadiusB);
        controlsByRegionType.get(RegionType.SPHERE).add(inputSphereOriginX);
        controlsByRegionType.get(RegionType.SPHERE).add(inputSphereOriginY);
        controlsByRegionType.get(RegionType.SPHERE).add(inputSphereOriginZ);
        controlsByRegionType.get(RegionType.SPHERE).add(emptyRow);
        controlsByRegionType.get(RegionType.SPHERE).add(inputSphereRadiusX);
        controlsByRegionType.get(RegionType.SPHERE).add(inputSphereRadiusY);
        controlsByRegionType.get(RegionType.SPHERE).add(inputSphereRadiusZ);

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
        inputPlaneCoord.setValue(regionFactory.getPlane().getCoord());
        // TODO: cuboid inputs
        inputCylinderOriginX.setValue(regionFactory.getCylinder().getOriginReadonly().getX());
        inputCylinderOriginY.setValue(regionFactory.getCylinder().getOriginReadonly().getY());
        inputCylinderOriginZ.setValue(regionFactory.getCylinder().getOriginReadonly().getZ());
        inputCylinderAxis.setSelectedValue(regionFactory.getCylinder().getAxis(), false);
        inputCylinderHeight.setValue(3.0);
        inputCylinderRadiusA.setValue(8.0);
        inputCylinderRadiusB.setValue(8.0);
        inputSphereOriginX.setValue(regionFactory.getSphere().getOriginReadonly().getX());
        inputSphereOriginY.setValue(regionFactory.getSphere().getOriginReadonly().getY());
        inputSphereOriginZ.setValue(regionFactory.getSphere().getOriginReadonly().getZ());
        inputSphereRadiusX.setValue(regionFactory.getSphere().getRadiusX());
        inputSphereRadiusY.setValue(regionFactory.getSphere().getRadiusY());
        inputSphereRadiusZ.setValue(regionFactory.getSphere().getRadiusZ());

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

        // Notify controller that the GUI is open.
        controller.toggleGui(true);

        // TODO: possibly allow the user to look around by holding right-click and moving around
        // TODO: pressing B or Esc is the same as pressing the "OK" button
        // TODO: buttons to rotate the region around its origin (for planes, fill in using player coords)
        // TODO: eliminate click sounds
        // TODO: row mouseover tooltips
        // TODO: add footer with buttons ("OK", "Usage", "Undo" (gray out initially))
    }

    @Override
    public void onGuiClosed() {
        controller.toggleGui(false);
    }

    public void updateControlProperties() {
        // Hide controls that don't match the current region type being modified.
        RegionType activeRegionType = inputRegionType.getSelectedValue();
        for (RegionType regionType : RegionType.values()) {
            for (GuiLabeledControl control : controlsByRegionType.get(regionType)) {
                control.drawButton = (regionType == RegionType.NONE || regionType == activeRegionType);
            }
        }

        // Adjust dynamic label texts.
        inputPlaneCoord.displayString = regionFactory.getPlane().getAxis().toString().toLowerCase() + " coordinate:";
        inputCylinderRadiusA.displayString = regionFactory.getCylinder().getRadiusAxisA().toString().toLowerCase() + " radius:";
        inputCylinderRadiusB.displayString = regionFactory.getCylinder().getRadiusAxisB().toString().toLowerCase() + " radius:";
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
            controller.cmdSet(regionFactory.getRegionAs(inputRegionType.getSelectedValue()));
        }
        updateControlProperties();
    }
}
