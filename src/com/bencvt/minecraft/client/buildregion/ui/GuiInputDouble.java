package com.bencvt.minecraft.client.buildregion.ui;

import com.bencvt.minecraft.client.buildregion.ui.GuiEnumSelect.Option;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;

public class GuiInputDouble extends GuiLabeledControl {
    public static final int PAD_TOP = 2;
    public static final int PAD_BOTTOM = 1;

    private int valueWidth;
    private double value;

    public GuiInputDouble(String displayString, FontRenderer fontRenderer) {
        super(displayString, fontRenderer);
        valueWidth = fontRenderer.getStringWidth("-99999.5");
        setControlWidth(valueWidth); // also sets width
        height = PAD_TOP + fontRenderer.FONT_HEIGHT + PAD_BOTTOM;
    }

    public double getValue() {
        return value;
    }
    public void setValue(double value) {
        this.value = value;
    }

    @Override
    protected int getLabelYOffset() {
        return PAD_TOP;
    }

    @Override
    protected void drawControl(int xMouse, int yMouse) {
        int xOffset = getControlXOffset();

        String valueString = Double.toString(getValue());
        fontRenderer.drawString(
                valueString,
                xOffset + valueWidth - fontRenderer.getStringWidth(valueString),
                yPosition + PAD_TOP,
                CONTROL_NORMAL_ARGB);
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int xMouse, int yMouse) {
        if (!super.mousePressed(minecraft, xMouse, yMouse)) {
            return false;
        }
        // TODO: clicking the value turns it into a text input box
        // TODO: +/- buttons and a slider
        // TODO: add the ability to lock the value to either full units or half units
        // TODO: some way to lock this GuiInputDouble to another (e.g. shared radiuses)
        return true;
    }
}
