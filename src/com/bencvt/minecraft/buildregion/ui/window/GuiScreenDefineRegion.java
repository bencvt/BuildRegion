package com.bencvt.minecraft.buildregion.ui.window;

import java.util.ArrayList;
import java.util.HashMap;

import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import libshapedraw.primitive.Vector3;
import net.minecraft.src.GuiButton;

import com.bencvt.minecraft.buildregion.BuildMode;
import com.bencvt.minecraft.buildregion.Controller;
import com.bencvt.minecraft.buildregion.region.Axis;
import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.region.RegionCuboid;
import com.bencvt.minecraft.buildregion.region.RegionCylinder;
import com.bencvt.minecraft.buildregion.region.RegionFactory;
import com.bencvt.minecraft.buildregion.region.RegionPlane;
import com.bencvt.minecraft.buildregion.region.RegionSphere;
import com.bencvt.minecraft.buildregion.region.RegionType;
import com.bencvt.minecraft.buildregion.region.Units;
import com.bencvt.minecraft.buildregion.ui.ChatHider;

/**
 * The main BuildRegion GUI.
 * 
 * @author bencvt
 */
public class GuiScreenDefineRegion extends GuiScreenBase {
    public static final int ROW_SPACING = 2;
    public static final int PAD = 2;
    public static final int BORDER_THICKNESS = 1;
    public static final int BACKGROUND_ARGB        = Color.BLACK.copy().setAlpha(3.0/8.0).getARGB();
    public static final ReadonlyColor BORDER_COLOR = Color.BLACK.copy().setAlpha(1.0/8.0);
    public static final int BORDER_ARGB            = BORDER_COLOR.getARGB();
    public static final ReadonlyColor SELECT_COLOR = Color.DODGER_BLUE;

    private final Controller controller;
    private final BuildMode originalBuildMode;
    private final RegionType originalRegionType;
    private final GuiEmptyRow                 rowSpacer;
    private final GuiSelectEnum<BuildMode>    inputBuildMode;
    private final GuiSelectEnum<RegionType>   inputRegionType;
    private final GuiHLine                    hLine;
    private final GuiSelectEnum<Axis>         inputPlaneAxis;
    private final GuiInputNumber              inputPlaneCoord;
    private final GuiInputNumber              inputCuboidLowerCornerX;
    private final GuiInputNumber              inputCuboidLowerCornerY;
    private final GuiInputNumber              inputCuboidLowerCornerZ;
    private final GuiInputNumberGroup         groupCuboidSizes;
    private final GuiInputNumber              inputCuboidSizeX;
    private final GuiInputNumber              inputCuboidSizeY;
    private final GuiInputNumber              inputCuboidSizeZ;
    private final GuiInputNumber              inputCylinderOriginX;
    private final GuiInputNumber              inputCylinderOriginY;
    private final GuiInputNumber              inputCylinderOriginZ;
    private final GuiSelectEnum<Axis>         inputCylinderAxis;
    private final GuiInputNumber              inputCylinderHeight;
    private final GuiInputNumberGroup         groupCylinderRadii;
    private final GuiInputNumber              inputCylinderRadiusA;
    private final GuiInputNumber              inputCylinderRadiusB;
    private final GuiInputNumber              inputSphereOriginX;
    private final GuiInputNumber              inputSphereOriginY;
    private final GuiInputNumber              inputSphereOriginZ;
    private final GuiInputNumberGroup         groupSphereRadii;
    private final GuiInputNumber              inputSphereRadiusX;
    private final GuiInputNumber              inputSphereRadiusY;
    private final GuiInputNumber              inputSphereRadiusZ;
    private final HashMap<RegionType, ArrayList<GuiLabeledControl>> rows;
    private final GuiStandardButton buttonHelp;
    private final GuiStandardButton buttonOptions;
    private final GuiStandardButton buttonReset;
    private final GuiStandardButton buttonDone;
    // Position, height, and width are calculated in initGui().
    private int windowXPosition;
    private int windowYPosition;
    private int windowHeight;
    private int windowWidth;
    private final RegionFactory regionFactory;

    public GuiScreenDefineRegion(Controller controller) {
        super(null); // this is a root screen
        this.controller = controller;
        originalBuildMode = controller.getBuildMode().getValue();
        if (controller.isRegionActive()) {
            originalRegionType = controller.getPrototypeRegion().getRegionType();
        } else {
            originalRegionType = RegionType.NONE;
        }

        // Create all row controls.
        rowSpacer = new GuiEmptyRow(this, fontRenderer.FONT_HEIGHT + 3);
        inputBuildMode = new GuiSelectEnum<BuildMode>(this, i18n("label.buildmode"), BuildMode.values(), null);
        for (BuildMode mode : BuildMode.values()) {
            inputBuildMode.setOptionColor(mode, mode.colorVisible);
        }
        inputRegionType = new GuiSelectEnum<RegionType>(this, i18n("label.regiontype"), RegionType.values(), SELECT_COLOR);
        hLine = new GuiHLine(this, BORDER_THICKNESS, BORDER_COLOR);
        inputPlaneAxis = new GuiSelectEnum<Axis>(this, i18n("label.axis"), Axis.values(), SELECT_COLOR);
        inputPlaneCoord = new GuiInputNumber(this, i18n("label.coord", "?"), Units.WHOLE, false, null);
        inputCuboidLowerCornerX = new GuiInputNumber(this, i18n("label.corner", Axis.X), Units.WHOLE, false, null);
        inputCuboidLowerCornerY = new GuiInputNumber(this, i18n("label.corner", Axis.Y), Units.WHOLE, false, null);
        inputCuboidLowerCornerZ = new GuiInputNumber(this, i18n("label.corner", Axis.Z), Units.WHOLE, false, null);
        groupCuboidSizes = new GuiInputNumberGroup();
        inputCuboidSizeX = new GuiInputNumber(this, i18n("label.size.x"), Units.WHOLE, true, groupCuboidSizes);
        inputCuboidSizeY = new GuiInputNumber(this, i18n("label.size.y"), Units.WHOLE, true, groupCuboidSizes);
        inputCuboidSizeZ = new GuiInputNumber(this, i18n("label.size.z"), Units.WHOLE, true, groupCuboidSizes);
        // TODO: special value restriction... origin matching cylinder axis must be whole units; other two can be half units
        inputCylinderOriginX = new GuiInputNumber(this, i18n("label.origin", Axis.X), Units.WHOLE, false, null);
        inputCylinderOriginY = new GuiInputNumber(this, i18n("label.origin", Axis.Y), Units.WHOLE, false, null);
        inputCylinderOriginZ = new GuiInputNumber(this, i18n("label.origin", Axis.Z), Units.WHOLE, false, null);
        inputCylinderAxis = new GuiSelectEnum<Axis>(this, i18n("label.axis"), Axis.values(), SELECT_COLOR);
        inputCylinderHeight = new GuiInputNumber(this, i18n("label.height"), Units.WHOLE, true, null);
        groupCylinderRadii = new GuiInputNumberGroup();
        inputCylinderRadiusA = new GuiInputNumber(this, i18n("label.radius", "?"), Units.HALF, true, groupCylinderRadii);
        inputCylinderRadiusB = new GuiInputNumber(this, i18n("label.radius", "?"), Units.HALF, true, groupCylinderRadii);
        inputSphereOriginX = new GuiInputNumber(this, i18n("label.origin", Axis.X), Units.HALF, false, null);
        inputSphereOriginY = new GuiInputNumber(this, i18n("label.origin", Axis.Y), Units.HALF, false, null);
        inputSphereOriginZ = new GuiInputNumber(this, i18n("label.origin", Axis.Z), Units.HALF, false, null);
        groupSphereRadii = new GuiInputNumberGroup();
        inputSphereRadiusX = new GuiInputNumber(this, i18n("label.radius", Axis.X), Units.HALF, true, groupSphereRadii);
        inputSphereRadiusY = new GuiInputNumber(this, i18n("label.radius", Axis.Y), Units.HALF, true, groupSphereRadii);
        inputSphereRadiusZ = new GuiInputNumber(this, i18n("label.radius", Axis.Z), Units.HALF, true, groupSphereRadii);

        // Add each row control to the appropriate list.
        rows = new HashMap<RegionType, ArrayList<GuiLabeledControl>>();
        for (RegionType r : RegionType.values()) {
            rows.put(r, new ArrayList<GuiLabeledControl>());
        }
        rows.get(RegionType.NONE).add(inputBuildMode);
        rows.get(RegionType.NONE).add(inputRegionType);
        rows.get(RegionType.NONE).add(hLine);
        rows.get(RegionType.PLANE).add(inputPlaneAxis);
        rows.get(RegionType.PLANE).add(inputPlaneCoord);
        rows.get(RegionType.CUBOID).add(inputCuboidLowerCornerX);
        rows.get(RegionType.CUBOID).add(inputCuboidLowerCornerY);
        rows.get(RegionType.CUBOID).add(inputCuboidLowerCornerZ);
        rows.get(RegionType.CUBOID).add(rowSpacer);
        rows.get(RegionType.CUBOID).add(inputCuboidSizeX);
        rows.get(RegionType.CUBOID).add(inputCuboidSizeY);
        rows.get(RegionType.CUBOID).add(inputCuboidSizeZ);
        rows.get(RegionType.CYLINDER).add(inputCylinderOriginX);
        rows.get(RegionType.CYLINDER).add(inputCylinderOriginY);
        rows.get(RegionType.CYLINDER).add(inputCylinderOriginZ);
        rows.get(RegionType.CYLINDER).add(inputCylinderAxis);
        rows.get(RegionType.CYLINDER).add(inputCylinderHeight);
        rows.get(RegionType.CYLINDER).add(inputCylinderRadiusA);
        rows.get(RegionType.CYLINDER).add(inputCylinderRadiusB);
        rows.get(RegionType.SPHERE).add(inputSphereOriginX);
        rows.get(RegionType.SPHERE).add(inputSphereOriginY);
        rows.get(RegionType.SPHERE).add(inputSphereOriginZ);
        rows.get(RegionType.SPHERE).add(rowSpacer);
        rows.get(RegionType.SPHERE).add(inputSphereRadiusX);
        rows.get(RegionType.SPHERE).add(inputSphereRadiusY);
        rows.get(RegionType.SPHERE).add(inputSphereRadiusZ);

        // Populate the row controls' contents from the controller.
        regionFactory = new RegionFactory(controller.getPrototypeRegion(), controller.getBlockInFrontOfPlayer());
        inputBuildMode.setSelectedValue(originalBuildMode, false);
        inputRegionType.setSelectedValue(originalRegionType, false);
        importRegion();
        updateControlProperties();

        // Create other (non-row) controls.
        buttonHelp = new GuiStandardButton(this, i18n("button.help"));
        buttonOptions = new GuiStandardButton(this, i18n("button.options"));
        buttonReset = new GuiStandardButton(this, i18n("button.reset"));
        buttonDone = new GuiStandardButton(this, i18n("button.done"));

        // Defer positioning and width adjustments until initGui().
    }

    @Override
    public void initGui() {
        // Calculate maximum widths so the table's columns align.
        // Calculate the window height, assuming the subtable with the most rows is being displayed.
        int maxLabelWidth = 0;
        int maxControlWidth = 0;
        windowHeight = 0;
        for (RegionType r : RegionType.values()) {
            for (GuiLabeledControl control : rows.get(r)) {
                maxLabelWidth = Math.max(maxLabelWidth, control.getLabelWidth());
                maxControlWidth = Math.max(maxControlWidth, control.getControlWidth());

                // The cylinder subtable has the most rows.
                if (r == RegionType.NONE || r == RegionType.CYLINDER) {
                    if (windowHeight > 0) {
                        windowHeight += ROW_SPACING;
                    }
                    windowHeight += control.getHeight();
                }
            }
        }
        windowHeight += BORDER_THICKNESS*2 + PAD*2;

        // Adjust row controls' widths to match the above.
        // Calculate the window width.
        // Add all row controls to the main list of controls to render.
        windowWidth = 0;
        for (RegionType r : RegionType.values()) {
            for (GuiLabeledControl control : rows.get(r)) {
                control.setLabelWidth(maxLabelWidth);
                control.setControlWidth(maxControlWidth);

                windowWidth = Math.max(windowWidth, control.getWidth());

                controlList.add(control);
            }
        }
        windowWidth += BORDER_THICKNESS*2 + PAD*2;

        // Center horizontally and attach to bottom of the screen.
        windowXPosition = (width - windowWidth) / 2;
        windowYPosition = height - windowHeight - BOTTOM_OVERLAY_HEIGHT;

        // [Re]position all row controls.
        int xPos = windowXPosition + BORDER_THICKNESS + PAD;
        int yPos = windowYPosition + BORDER_THICKNESS + PAD;
        int yPosBack = yPos;
        for (RegionType r : RegionType.values()) {
            for (GuiLabeledControl control : rows.get(r)) {
                yPos += control.setPositionXY(xPos, yPos).getHeight() + ROW_SPACING;
            }
            if (r == RegionType.NONE) {
                yPosBack = yPos;
            } else {
                yPos = yPosBack;
            }
        }

        // [Re]position other (non-row) controls, below the window.
        // Ensure the button widths are uniform.
        // Add to the main list of controls to render.
        final int buttonWidth = Math.max(
                Math.max(buttonHelp.getWidth(), buttonOptions.getWidth()),
                Math.max(buttonReset.getWidth(), buttonDone.getWidth()));
        xPos = windowXPosition;
        yPos = windowYPosition + windowHeight; // no vertical padding
        controlList.add(buttonHelp.setWidth(buttonWidth).setPositionXY(xPos, yPos));
        xPos += buttonHelp.getWidth() + 4;
        // TODO: enable options button once GUI is built
        //controlList.add(buttonOptions.setWidth(buttonWidth).setPositionXY(xPos, yPos));
        xPos = windowXPosition + windowWidth - buttonDone.setWidth(buttonWidth).getWidth();
        controlList.add(buttonDone.setPositionXY(xPos, yPos));
        xPos -= buttonReset.setWidth(buttonWidth).getWidth() + 4;
        controlList.add(buttonReset.setPositionXY(xPos, yPos));

        // Hide the chat window which would otherwise clutter up the screen.
        ChatHider.hide();

        // TODO: allow the keybinds to move regions around to still work
        // TODO: pressing B or Esc is the same as pressing the "OK" button
        // TODO: buttons to rotate the region around its origin (for planes, fill in using player coords)
        // TODO: row mouseover tooltips
    }

    /**
     * Populate control values from the region factory.
     */
    private void importRegion() {
        final RegionType active = inputRegionType.getSelectedValue();
        final RegionBase region = regionFactory.convert(active);
        if (active == RegionType.PLANE) {
            RegionPlane plane = (RegionPlane) region;
            inputPlaneAxis.setSelectedValue(plane.getAxis(), false);
            inputPlaneCoord.setValue(plane.getCoord());
        } else if (active == RegionType.CUBOID) {
            RegionCuboid cuboid = (RegionCuboid) region;
            inputCuboidLowerCornerX.setValue(cuboid.getLowerCornerReadonly().getX());
            inputCuboidLowerCornerY.setValue(cuboid.getLowerCornerReadonly().getY());
            inputCuboidLowerCornerZ.setValue(cuboid.getLowerCornerReadonly().getZ());
            inputCuboidSizeX.setValue(cuboid.getSizeX());
            inputCuboidSizeY.setValue(cuboid.getSizeY());
            inputCuboidSizeZ.setValue(cuboid.getSizeZ());
            groupCuboidSizes.unlock();
        } else if (active == RegionType.CYLINDER) {
            RegionCylinder cylinder = (RegionCylinder) region;
            inputCylinderOriginX.setValue(cylinder.getOriginReadonly().getX());
            inputCylinderOriginY.setValue(cylinder.getOriginReadonly().getY());
            inputCylinderOriginZ.setValue(cylinder.getOriginReadonly().getZ());
            inputCylinderAxis.setSelectedValue(cylinder.getAxis(), false);
            inputCylinderHeight.setValue(cylinder.getHeight());
            inputCylinderRadiusA.setValue(cylinder.getRadiusA());
            inputCylinderRadiusB.setValue(cylinder.getRadiusB());
            groupCylinderRadii.lockIfAllEqual();
        } else if (active == RegionType.SPHERE) {
            RegionSphere sphere = (RegionSphere) region;
            inputSphereOriginX.setValue(sphere.getOriginReadonly().getX());
            inputSphereOriginY.setValue(sphere.getOriginReadonly().getY());
            inputSphereOriginZ.setValue(sphere.getOriginReadonly().getZ());
            inputSphereRadiusX.setValue(sphere.getRadiusX());
            inputSphereRadiusY.setValue(sphere.getRadiusY());
            inputSphereRadiusZ.setValue(sphere.getRadiusZ());
            groupSphereRadii.lockIfAllEqual();
        }
        // TODO: move radius/size locking logic to the Region classes instead and only auto-lock when converting in factory
    }

    /**
     * Update the region factory using control values.
     */
    private void exportRegion() {
        final RegionType active = inputRegionType.getSelectedValue();
        final RegionBase region = regionFactory.getRegion();
        if (active == RegionType.PLANE) {
            ((RegionPlane) region).set(
                    inputPlaneAxis.getSelectedValue(),
                    inputPlaneCoord.getValue());
        } else if (active == RegionType.CUBOID) {
            Vector3 lower = new Vector3(
                    inputCuboidLowerCornerX.getValue(),
                    inputCuboidLowerCornerY.getValue(),
                    inputCuboidLowerCornerZ.getValue());
            ((RegionCuboid) region).set(
                    lower,
                    inputCuboidSizeX.getValue(),
                    inputCuboidSizeY.getValue(),
                    inputCuboidSizeZ.getValue());
        } else if (active == RegionType.CYLINDER) {
            Vector3 origin = new Vector3(
                    inputCylinderOriginX.getValue(),
                    inputCylinderOriginY.getValue(),
                    inputCylinderOriginZ.getValue());
            ((RegionCylinder) region).set(
                    origin,
                    inputCylinderAxis.getSelectedValue(),
                    inputCylinderHeight.getValue(),
                    inputCylinderRadiusA.getValue(),
                    inputCylinderRadiusB.getValue());
        } else if (active == RegionType.SPHERE) {
            Vector3 origin = new Vector3(
                    inputSphereOriginX.getValue(),
                    inputSphereOriginY.getValue(),
                    inputSphereOriginZ.getValue());
            ((RegionSphere) region).set(
                    origin,
                    inputSphereRadiusX.getValue(),
                    inputSphereRadiusY.getValue(),
                    inputSphereRadiusZ.getValue());
        }
    }

    public void updateControlProperties() {
        // Hide controls that don't match the current region type being modified.
        final RegionType active = inputRegionType.getSelectedValue();
        for (RegionType r : RegionType.values()) {
            for (GuiLabeledControl control : rows.get(r)) {
                control.setVisible(r == RegionType.NONE || r == active);
            }
        }

        // Adjust dynamic label texts.
        if (active == RegionType.PLANE) {
            inputPlaneCoord.setText(i18n("label.coord", regionFactory.getRegion().getAxis()));
        } else if (active == RegionType.CYLINDER) {
            RegionCylinder cylinder = (RegionCylinder) regionFactory.getRegion();
            inputCylinderRadiusA.setText(i18n("label.radius", cylinder.getRadiusAxisA()));
            inputCylinderRadiusB.setText(i18n("label.radius", cylinder.getRadiusAxisB()));
        }
    }

    @Override
    public void drawScreen(int xMouse, int yMouse, float partialTick) {
        // Draw HUD-covering overlay at bottom.
        drawBottomOverlay();

        // Draw window background and border on top of it.
        final int xLeft   = windowXPosition;
        final int xRight  = windowXPosition + windowWidth;
        final int yTop    = windowYPosition;
        final int yBottom = windowYPosition + windowHeight;
        drawRect(xLeft, yTop, xRight, yBottom, BACKGROUND_ARGB);
        if (BORDER_THICKNESS > 0) {
            drawRectBorder(xLeft, yTop, xRight, yBottom, BORDER_ARGB, BORDER_THICKNESS);
        }

        // Defer control rendering to parent class.
        super.drawScreen(xMouse, yMouse, partialTick);

        // Except for control groups, we do that.
        if (inputRegionType.getSelectedValue() == RegionType.CUBOID) {
            groupCuboidSizes.draw();
        } else if (inputRegionType.getSelectedValue() == RegionType.CYLINDER) {
            groupCylinderRadii.draw();
        } else if (inputRegionType.getSelectedValue() == RegionType.SPHERE) {
            groupSphereRadii.draw();
        }
    }

    @Override
    protected void onControlClick(GuiButton guiButton) {
        if (guiButton == buttonHelp) {
            open(new GuiScreenHelp(this, controller));
        } else if (guiButton == buttonOptions) {
            open(new GuiScreenOptions(this));
        } else if (guiButton == buttonReset) {
            // Reset build mode and region to whatever they were when the user
            // opened the gui.
            controller.cmdMode(inputBuildMode
                    .setSelectedValue(originalBuildMode, true)
                    .getSelectedValue());
            regionFactory.reset();
            onControlClick(inputRegionType.setSelectedValue(originalRegionType, true));
        } else if (guiButton == buttonDone) {
            // Restore the chat window. We do this here instead of overriding
            // onGuiClosed because we want chat hidden for child windows too.
            ChatHider.show();
            close();
        } else if (guiButton == inputBuildMode) {
            controller.cmdMode(inputBuildMode.getSelectedValue());
        } else if (guiButton == inputRegionType) {
            importRegion();
            updateControlProperties();
            controller.cmdSet(regionFactory.getRegion(), true);
        } else {
            exportRegion();
            updateControlProperties();
            controller.cmdSet(regionFactory.getRegion(), true);
        }
    }

    @Override
    public void onControlUpdate(GuiControlBase control, boolean rapid) {
        exportRegion();
        controller.cmdSet(regionFactory.getRegion(), !rapid);
    }
}
