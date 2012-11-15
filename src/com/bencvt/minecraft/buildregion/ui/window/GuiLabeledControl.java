package com.bencvt.minecraft.buildregion.ui.window;

import libshapedraw.primitive.Color;
import net.minecraft.client.Minecraft;

import org.lwjgl.input.Mouse;

public abstract class GuiLabeledControl extends GuiControlBase {
    public static final int LABEL_SPACING = 4;
    public static final int LABEL_ARGB             = Color.LIGHT_GRAY.getARGB();
    public static final int CONTROL_ENABLED_ARGB   = Color.WHITE.getARGB();
    public static final int CONTROL_DISABLED_ARGB  = Color.DARK_GRAY.getARGB();
    public static final int CONTROL_MOUSEOVER_ARGB = Color.WHITE.getARGB();
    public static final int ROW_MOUSEOVER_ARGB     = Color.LIGHT_GRAY.copy().setAlpha(1.0/8.0).getARGB();

    private int labelWidth;
    private int controlWidth;
    private boolean wasMouseOver;

    public GuiLabeledControl(GuiScreenBase parent, String text) {
        super(parent, text);
        setLabelWidth(parent.getFontRenderer().getStringWidth(getText()));
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
    public final void drawButton(Minecraft minecraft, int xMouse, int yMouse) {
        if (!isVisible()) {
            return;
        }

        if (isMouseOver(xMouse, yMouse)) {
            // Dispatch mouse wheel event.
            int wheel = Mouse.getDWheel();
            if (wasMouseOver) {
                boolean back = wheel < 0;
                if (back || wheel > 0) {
                    mouseWheelScrolled(back);
                }
            }
            // Else do nothing; we consumed the event if there was one.

            // Highlight background on mouseover.
            if (ROW_MOUSEOVER_ARGB != 0) {
                drawRect(
                        xPosition, yPosition,
                        xPosition + width, yPosition + height,
                        ROW_MOUSEOVER_ARGB);
            }
            wasMouseOver = true;
        } else {
            wasMouseOver = false;
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

    protected void mouseWheelScrolled(boolean back) {
        // do nothing by default
    }

    protected abstract void drawControl(int xMouse, int yMouse);
}
