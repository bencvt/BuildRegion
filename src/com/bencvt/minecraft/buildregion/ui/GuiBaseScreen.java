package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiScreen;

public abstract class GuiBaseScreen extends GuiScreen {
    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public void rapidUpdate(GuiBaseControl control) {
        // Do nothing. The child class can override.
    }

    public static void drawRectBorder(int xLeft, int yTop, int xRight, int yBottom, int borderARGB, int borderThickness) {
        // Draw top border, including corners.
        drawRect(
                xLeft,
                yTop,
                xRight,
                yTop + borderThickness,
                borderARGB);
        // Draw bottom border, including corners.
        drawRect(
                xLeft,
                yBottom - borderThickness,
                xRight,
                yBottom,
                borderARGB);
        // Draw left border, excluding corners.
        drawRect(
                xLeft,
                yTop + borderThickness,
                xLeft + borderThickness,
                yBottom - borderThickness,
                borderARGB);
        // Draw right border, excluding corners.
        drawRect(
                xRight - borderThickness,
                yTop + borderThickness,
                xRight,
                yBottom - borderThickness,
                borderARGB);
    }
}
