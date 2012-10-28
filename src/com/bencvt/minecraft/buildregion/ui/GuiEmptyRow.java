package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.client.Minecraft;

public class GuiEmptyRow extends GuiLabeledControl {
    public GuiEmptyRow(GuiBaseScreen parent, int height) {
        super(parent, "");
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft minecraft, int xMouse, int yMouse) {
        // do nothing
    }

    @Override
    protected void drawControl(int xMouse, int yMouse) {
        // do nothing
    }
}
