package com.bencvt.minecraft.client.buildregion.ui;

import libshapedraw.primitive.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;

public abstract class GuiLabeledControl extends GuiButtonMoveable {
    public static final int LABEL_SPACING = 4;
    public static final int LABEL_ARGB             = Color.LIGHT_GRAY.getARGB();
    public static final int CONTROL_NORMAL_ARGB    = Color.WHITE.getARGB();
    public static final int CONTROL_MOUSEOVER_ARGB = Color.WHITE.getARGB();
    public static final int ROW_MOUSEOVER_ARGB     = Color.LIGHT_GRAY.copy().setAlpha(1.0/8.0).getARGB();

    protected final FontRenderer fontRenderer;
    private int labelWidth;
    private int controlWidth;

    public GuiLabeledControl(String displayString, FontRenderer fontRenderer) {
        super(displayString);
        this.fontRenderer = fontRenderer;
        setLabelWidth(fontRenderer.getStringWidth(displayString));
        height = fontRenderer.FONT_HEIGHT;
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
        if (ROW_MOUSEOVER_ARGB != 0 &&
                xMouse >= xPosition && xMouse <= xPosition + width &&
                yMouse >= yPosition && yMouse <= yPosition + height) {
            drawRect(
                    xPosition, yPosition,
                    xPosition + width, yPosition + height,
                    ROW_MOUSEOVER_ARGB);
        }

        // Draw label text.
        minecraft.fontRenderer.drawString(
                displayString,
                xPosition + getLabelWidth() - fontRenderer.getStringWidth(displayString),
                yPosition + getLabelYOffset(),
                LABEL_ARGB);

        // Defer control rendering to inheriting class.
        drawControl(xMouse, yMouse);
    }

    protected abstract void drawControl(int xMouse, int yMouse);
}
