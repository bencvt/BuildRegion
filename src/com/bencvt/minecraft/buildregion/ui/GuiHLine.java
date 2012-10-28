package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.primitive.ReadonlyColor;

public class GuiHLine extends GuiLabeledControl {
    private final ReadonlyColor color;

    public GuiHLine(GuiBaseScreen parent, int height, ReadonlyColor color) {
        super(parent, "");
        this.height = height;
        this.color = color;
    }

    @Override
    protected void drawControl(int xMouse, int yMouse) {
        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, color.getARGB());
    }
}
