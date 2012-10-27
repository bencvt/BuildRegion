package com.bencvt.minecraft.buildregion.ui;

import com.bencvt.minecraft.buildregion.region.Units;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;

public class GuiInputDouble extends GuiLabeledControl {
    public static final int PAD_TOP = 2;
    public static final int PAD_BOTTOM = 1;

    private final Units units;
    private final boolean positive;
    private final int valueRenderWidth;
    private double value;

    public GuiInputDouble(String displayString, FontRenderer fontRenderer, Units units, boolean positive) {
        super(displayString, fontRenderer);
        this.units = units;
        this.positive = positive;
        valueRenderWidth = fontRenderer.getStringWidth("-99999.5");
        setControlWidth(valueRenderWidth); // also sets width
        height = PAD_TOP + fontRenderer.FONT_HEIGHT + PAD_BOTTOM;
    }

    public double getValue() {
        return value;
    }
    public void setValue(double value) {
        this.value = units.clamp(value);
    }

    @Override
    protected int getLabelYOffset() {
        return PAD_TOP;
    }

    @Override
    protected void drawControl(int xMouse, int yMouse) {
        int xOffset = getControlXOffset();

        String valueString = units.d2s(getValue());
        fontRenderer.drawString(
                valueString,
                xOffset + valueRenderWidth - fontRenderer.getStringWidth(valueString),
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
