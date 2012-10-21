package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.primitive.ReadonlyColor;
import net.minecraft.src.FontRenderer;

public class GuiHLine extends GuiLabeledControl {

    private final ReadonlyColor color;

    public GuiHLine(FontRenderer fontRenderer, int height, ReadonlyColor color) {
        super("", fontRenderer);
        this.height = height;
        this.color = color;
    }

    @Override
    protected void drawControl(int xMouse, int yMouse) {
        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, color.getARGB());
    }
}
