package com.bencvt.minecraft.client.buildregion.ui;

import com.bencvt.minecraft.client.buildregion.ui.GuiEnumSelect.Option;

import libshapedraw.primitive.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;

public abstract class GuiLabeledControl extends GuiButton {
    public static final int LABEL_SPACING = 4;
    public static final int LABEL_COLOR_ARGB = Color.LIGHT_GRAY.getARGB();
    public static final int CONTROL_NORMAL_COLOR_ARGB = Color.WHITE.getARGB();
    public static final int CONTROL_MOUSEOVER_COLOR_ARGB = Color.WHITE.getARGB();
    public static final int ROW_MOUSEOVER_BGCOLOR_ARGB = Color.LIGHT_GRAY.copy().setAlpha(0.125).getARGB();
    private static int topId;

    protected final FontRenderer fontRenderer;
    private int labelWidth;
    private int controlWidth;

    public GuiLabeledControl(String displayString, FontRenderer fontRenderer) {
        super(topId, 0, 0, 0, 0, displayString);
        // caller is responsible for setting xPosition and yPosition
        topId++;
        this.fontRenderer = fontRenderer;
        setLabelWidth(fontRenderer.getStringWidth(displayString));
        height = fontRenderer.FONT_HEIGHT;
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

    protected int getLabelYOffset() {
        return 0;
    }

    public int getLabelWidth() {
        return labelWidth;
    }
    public void setLabelWidth(int labelWidth) {
        this.labelWidth = labelWidth;
        width = labelWidth + LABEL_SPACING + controlWidth;
    }

    public int getControlWidth() {
        return controlWidth;
    }
    public void setControlWidth(int controlWidth) {
        this.controlWidth = controlWidth;
        width = labelWidth + LABEL_SPACING + controlWidth;
    }

    protected int getControlXOffset() {
        return xPosition + getLabelWidth() + LABEL_SPACING;
    }

    @Override
    public final void drawButton(Minecraft minecraft, int xMouse, int yMouse) {
        if (!drawButton) {
            return;
        }

        // Highlight background if mouseover.
        if (ROW_MOUSEOVER_BGCOLOR_ARGB != 0 &&
                xMouse >= xPosition && xMouse <= xPosition + width &&
                yMouse >= yPosition && yMouse <= yPosition + height) {
            drawRect(
                    xPosition, yPosition,
                    xPosition + width, yPosition + height,
                    ROW_MOUSEOVER_BGCOLOR_ARGB);
        }

        // Draw label text.
        minecraft.fontRenderer.drawString(
                displayString,
                xPosition + getLabelWidth() - fontRenderer.getStringWidth(displayString),
                yPosition + getLabelYOffset(),
                LABEL_COLOR_ARGB);

        // Defer control rendering to inheriting class.
        drawControl(xMouse, yMouse);
    }

    protected abstract void drawControl(int xMouse, int yMouse);
}
