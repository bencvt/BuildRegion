package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.primitive.Color;
import net.minecraft.client.Minecraft;

public abstract class GuiLabeledControl extends GuiBaseControl {
    public static final int LABEL_SPACING = 4;
    public static final int LABEL_ARGB             = Color.LIGHT_GRAY.getARGB();
    public static final int CONTROL_ENABLED_ARGB   = Color.WHITE.getARGB();
    public static final int CONTROL_DISABLED_ARGB  = Color.DARK_GRAY.getARGB();
    public static final int CONTROL_MOUSEOVER_ARGB = Color.WHITE.getARGB();
    public static final int ROW_MOUSEOVER_ARGB     = Color.LIGHT_GRAY.copy().setAlpha(1.0/8.0).getARGB();

    private int labelWidth;
    private int controlWidth;

    protected GuiLabeledControl(GuiBaseScreen parent, String text) {
        super(parent, text);
        setLabelWidth(parent.getFontRenderer().getStringWidth(displayString));
        height = parent.getFontRenderer().FONT_HEIGHT;
    }

    protected int getLabelYOffset() {
        return 0;
    }

    public final int getLabelWidth() {
        return labelWidth;
    }
    public final void setLabelWidth(int labelWidth) {
        this.labelWidth = labelWidth;
        width = labelWidth + LABEL_SPACING + controlWidth;
    }

    public final int getControlWidth() {
        return controlWidth;
    }
    public final void setControlWidth(int controlWidth) {
        this.controlWidth = controlWidth;
        width = labelWidth + LABEL_SPACING + controlWidth;
    }

    protected final int getControlXOffset() {
        return xPosition + labelWidth + LABEL_SPACING;
    }

    @Override
    public void drawButton(Minecraft minecraft, int xMouse, int yMouse) {
        if (!isVisible()) {
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
                getText(),
                xPosition + labelWidth - parent.getFontRenderer().getStringWidth(getText()),
                yPosition + getLabelYOffset(),
                LABEL_ARGB);

        // Defer control rendering to inheriting class.
        drawControl(xMouse, yMouse);
    }

    protected abstract void drawControl(int xMouse, int yMouse);
}
