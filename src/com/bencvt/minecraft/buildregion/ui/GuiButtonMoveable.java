package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.src.GuiButton;

public abstract class GuiButtonMoveable extends GuiButton {
    private static int topId;

    public GuiButtonMoveable(String displayString) {
        super(topId, 0, 0, 0, 0, displayString);
        // caller is responsible for setting xPosition and yPosition
        topId++;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int position(int xPosition, int yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        return getHeight();
    }
}
