package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.primitive.Color;

import com.bencvt.minecraft.buildregion.region.Units;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;

public class GuiInputDouble extends GuiLabeledControl {
    public static final int PAD_TOP = 2;
    public static final int PAD_BOTTOM = 1;
    public static final int BUTTON_SPACING = 2;

    public static final int XBEGIN_TEXT   = 0;
    public static final int XEND_TEXT     = XBEGIN_TEXT + 44; // == fontRenderer.getStringWidth("-99999.5")
    public static final int XBEGIN_MINUS  = XEND_TEXT + BUTTON_SPACING;
    public static final int XEND_MINUS    = XBEGIN_MINUS + 11;
    public static final int XBEGIN_PLUS   = XEND_MINUS + BUTTON_SPACING;
    public static final int XEND_PLUS     = XBEGIN_PLUS + 11;
    public static final int XBEGIN_SLIDER = XEND_PLUS + BUTTON_SPACING;
    public static final int MIN_SLIDER_WIDTH = 100;

    public static final int BUTTON_DISABLED_ARGB = Color.SILVER.copy().scaleAlpha(0.5).getARGB();
    public static final int BUTTON_ENABLED_ARGB = Color.BLACK.getARGB();
    public static final int BUTTON_MOUSEOVER_ARGB = Color.DODGER_BLUE.getARGB();

    private double value;
    private final Units units;
    private final boolean positive;

    public GuiInputDouble(String displayString, FontRenderer fontRenderer, Units units, boolean positive) {
        super(displayString, fontRenderer);
        this.units = units;
        this.positive = positive;
        setControlWidth(XBEGIN_SLIDER + MIN_SLIDER_WIDTH); // also sets width
        height = PAD_TOP + fontRenderer.FONT_HEIGHT + PAD_BOTTOM;
    }

    public double getValue() {
        return value;
    }
    public void setValue(double value) {
        if (positive) {
            this.value = units.clampAtom(value);
        } else {
            this.value = units.clamp(value);
        }
        // TODO: some way to lock this GuiInputDouble to another (e.g. shared radiuses)
    }

    private boolean isMinusButtonEnabled() {
        return !positive || value > units.atom;
    }

    private boolean isPlusButtonEnabled() {
        return true;
    }

    @Override
    protected int getLabelYOffset() {
        return PAD_TOP;
    }

    @Override
    protected void drawControl(int xMouse, int yMouse) {
        final int xControl = getControlXOffset();
        int buttonARGB;

        // Value as text.
        String valueString = units.d2s(getValue());
        fontRenderer.drawString(
                valueString,
                xControl + XEND_TEXT - XBEGIN_TEXT - fontRenderer.getStringWidth(valueString),
                yPosition + PAD_TOP,
                CONTROL_NORMAL_ARGB);

        // Minus button.
        renderSubButton(xMouse, yMouse, "-", isMinusButtonEnabled(),
                xControl + XBEGIN_MINUS, xControl + XEND_MINUS);

        // Plus button.
        renderSubButton(xMouse, yMouse, "+", isPlusButtonEnabled(),
                xControl + XBEGIN_PLUS, xControl + XEND_PLUS);

        // Slider.
        drawRect(
                xControl + XBEGIN_SLIDER,
                yPosition,
                xPosition + width,
                yPosition + height,
                Color.GOLD.getARGB());
    }

    private void renderSubButton(int xMouse, int yMouse, String text, boolean enabled, int xBegin, int xEnd) {
        final int buttonARGB;
        if (enabled) {
            if (xMouse >= xBegin && xMouse <= xEnd &&
                    yMouse >= yPosition && yMouse <= yPosition + height) {
                buttonARGB = BUTTON_MOUSEOVER_ARGB;
            } else {
                buttonARGB = BUTTON_ENABLED_ARGB;
            }
        } else {
            buttonARGB = BUTTON_DISABLED_ARGB;
        }
        drawRect(xBegin, yPosition, xEnd, yPosition + height - 1, buttonARGB);
        fontRenderer.drawString(
                text,
                xBegin + (xEnd - xBegin - fontRenderer.getStringWidth(text))/2 + 1,
                yPosition + PAD_TOP,
                CONTROL_NORMAL_ARGB);
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int xMouse, int yMouse) {
        if (!super.mousePressed(minecraft, xMouse, yMouse)) {
            return false;
        }
        final int x = xMouse - getControlXOffset();
        if (x >= XBEGIN_TEXT && x <= XEND_TEXT) {
            System.out.println("you clicked text");//TODO: turn into text input box
            return true;
        } else if (isMinusButtonEnabled() && x >= XBEGIN_MINUS && x <= XEND_MINUS) {
            setValue(getValue() - units.atom);
            return true;
        } else if (isPlusButtonEnabled() && x >= XBEGIN_PLUS && x <= XEND_PLUS) {
            setValue(getValue() + units.atom);
            return true;
        } else if (x >= XBEGIN_SLIDER) {
            System.out.println("you clicked slider");//TODO
            return true;
        } else {
            return false;
        }
    }
}
