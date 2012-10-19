package com.bencvt.minecraft.client.buildregion.ui;

import java.util.ArrayList;
import java.util.HashMap;

import libshapedraw.primitive.Color;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

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

    private final Controller controller;
    private final HashMap<RegionType, ArrayList<GuiLabeledControl>> controlsByRegionType;
    private final GuiEnumSelect<BuildMode>    selectBuildMode;
    private final GuiEnumSelect<RegionType>   selectRegionType;
    private final GuiEnumSelect<Axis>         selectPlaneAxis;
    private final GuiInputDouble              inputPlaneCoord;
    private int windowXPosition = 0;
    private int windowYPosition = 0;
    private int windowHeight; // calculated in initGui
    private int windowWidth;  // calculated in initGui

    public GuiBuildRegion(Controller controller) {
        this.controller = controller;

        controlsByRegionType = new HashMap<RegionType, ArrayList<GuiLabeledControl>>();
        for (RegionType regionType : RegionType.values()) {
            controlsByRegionType.put(regionType, new ArrayList<GuiLabeledControl>());
        }

        selectBuildMode = new GuiEnumSelect<BuildMode>(BuildMode.class, "build mode:", null);
        for (BuildMode mode : BuildMode.values()) {
            selectBuildMode.setOptionColor(mode, mode.lineColorVisible);
        }
        selectRegionType = new GuiEnumSelect<RegionType>(RegionType.class, "region type:", BuildMode.activeLineColorVisible);
        selectPlaneAxis = new GuiEnumSelect<Axis>(Axis.class, "axis:", BuildMode.activeLineColorVisible);
        inputPlaneCoord = new GuiInputDouble("x coordinate:");

        controlsByRegionType.get(RegionType.NONE).add(selectBuildMode);
        controlsByRegionType.get(RegionType.NONE).add(selectRegionType);
        controlsByRegionType.get(RegionType.PLANE).add(selectPlaneAxis);
        controlsByRegionType.get(RegionType.PLANE).add(inputPlaneCoord);
    }

    public void updateControlProperties() {
        RegionType activeRegionType = selectRegionType.getSelectedValue();
        for (RegionType regionType : controlsByRegionType.keySet()) {
            for (GuiLabeledControl control : controlsByRegionType.get(regionType)) {
                control.drawButton = (regionType == RegionType.NONE || regionType == activeRegionType);
            }
        }
        inputPlaneCoord.displayString = selectPlaneAxis.getSelectedValue().toString().toLowerCase() + " coordinate:";
    }

    @Override
    public void initGui() {
        // Calculate the maximum label width so the table's columns align.
        int labelWidth = 0;
        for (RegionType regionType : controlsByRegionType.keySet()) {
            for (GuiLabeledControl control : controlsByRegionType.get(regionType)) {
                labelWidth = Math.max(labelWidth, fontRenderer.getStringWidth(control.displayString));
            }
        }

        // [Re]initialize the layout of all controls.
        setLayoutAll(labelWidth, windowXPosition + PAD, windowYPosition + PAD);

        // Populate the controls' contents from the controller.
        selectBuildMode.setSelectedValue(BuildMode.getActiveMode(), false);
        selectRegionType.setSelectedValue(RegionType.PLANE, false); // XXX
        selectPlaneAxis.setSelectedValue(Axis.X, false); // XXX
        inputPlaneCoord.setValue(-4213.5); // XXX

        // Calculate the window width and height.
        // Add all controls to the list of controls to render.
        windowWidth = 0;
        windowHeight = 0;
        for (RegionType regionType : controlsByRegionType.keySet()) {
            for (GuiLabeledControl control : controlsByRegionType.get(regionType)) {
                windowWidth = Math.max(windowWidth, PAD + control.getWidth() + PAD);
                windowHeight = Math.max(windowHeight, control.yPosition + control.getHeight() + PAD);
                controlList.add(control);
            }
        }

        updateControlProperties();
    }

    private void setLayoutAll(int labelWidth, int xPos, int yPos) {
        selectBuildMode.setLayout(fontRenderer, labelWidth, xPos, yPos);
        yPos += selectBuildMode.getHeight() + ROW_SPACING;
        selectRegionType.setLayout(fontRenderer, labelWidth, xPos, yPos);
        yPos += selectRegionType.getHeight() + ROW_SPACING;
        setLayoutPlane(labelWidth, xPos, yPos);
    }
    private void setLayoutPlane(int labelWidth, int xPos, int yPos) {
        selectPlaneAxis.setLayout(fontRenderer, labelWidth, xPos, yPos);
        yPos += selectPlaneAxis.getHeight() + ROW_SPACING;
        inputPlaneCoord.setLayout(fontRenderer, labelWidth, xPos, yPos);
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
        if (guiButton == selectBuildMode) {
            controller.cmdMode(selectBuildMode.getSelectedValue());
            return;
        }
        if (guiButton == selectRegionType) {
            if (selectRegionType.getSelectedValue() == null) {
                controller.cmdClear(true);
            }
            // TODO
        }
        updateControlProperties();
    }
}
