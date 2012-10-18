package com.bencvt.minecraft.client.buildregion.ui;

import libshapedraw.primitive.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

import com.bencvt.minecraft.client.buildregion.BuildMode;
import com.bencvt.minecraft.client.buildregion.Controller;

/**
 * The main BuildRegion GUI.
 * 
 * @author bencvt
 */
public class GuiBuildRegion extends GuiScreen {

    private static final int CID_BUILD_MODE = 1;
    private static final int CID_REGION_MODE = 2;

    // TODO: move to own file
    public enum RegionMode {
        PLANE, CUBOID, CYLINDER, SPHEROID, SCHEMATIC;
    }

    private final Controller controller;

    public GuiBuildRegion(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void initGui() {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        controlList.clear();

        GuiEnumSelect<BuildMode> buildModeSelect = new GuiEnumSelect<BuildMode>(
                BuildMode.class,
                fontRenderer,
                CID_BUILD_MODE,
                55, 15);
        for (BuildMode mode : BuildMode.values()) {
            buildModeSelect.setColors(mode, mode.lineColorVisible);
        }
        buildModeSelect.setSelectedValue(BuildMode.getActiveMode(), false);
        controlList.add(buildModeSelect);

        controlList.add(new GuiEnumSelect<RegionMode>(
                RegionMode.class,
                fontRenderer,
                CID_REGION_MODE,
                55, 35)
                .setColors(RegionMode.CUBOID, Color.AQUAMARINE)
                .setColors(RegionMode.SPHEROID, Color.BISQUE)
                .setAllowSetNull(true)
                .setSelectedValue(RegionMode.PLANE, false));
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) {
        if (guiButton.id == CID_BUILD_MODE) {
            GuiEnumSelect<BuildMode> select = (GuiEnumSelect<BuildMode>) guiButton;
            controller.cmdMode(select.getSelectedValue());
            return;
        }
        if (guiButton.id == CID_REGION_MODE) {
            GuiEnumSelect<RegionMode> select = (GuiEnumSelect<RegionMode>) guiButton;
            if (select.getSelectedValue() == null) {
                controller.cmdClear(true);
            }
            // TODO
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        // TODO: draw an outline behind all controls
        super.drawScreen(mouseX, mouseY, partialTick);
    }
}
