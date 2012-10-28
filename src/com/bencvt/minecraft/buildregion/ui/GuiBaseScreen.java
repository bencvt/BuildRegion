package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiScreen;

public abstract class GuiBaseScreen extends GuiScreen {
    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    /**
     * When a control is clicked, its parent GuiScreen's actionPerformed method
     * is automatically invoked. However this does not cover other ways that a
     * control can be updated, such as dragging the mouse.
     * 
     * @param control the control being updated
     * @param rapid if true the control is being rapidly updated and the screen
     *              should expect subsequent updates within a few milliseconds
     */
    public void controlUpdate(GuiBaseControl control, boolean rapid) {
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
