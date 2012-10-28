package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;

public class GuiEmptyRow extends GuiLabeledControl {
    public GuiEmptyRow(FontRenderer fontRenderer, int height) {
        super("", fontRenderer);
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
