package com.bencvt.minecraft.client.buildregion.ui;

import java.util.LinkedHashMap;

import libshapedraw.animation.trident.Timeline;
import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;

/**
 * A multi-select Minecraft GUI control.
 * @param <T> an enum type
 * 
 * @author bencvt
 */
public class GuiEnumSelect<T extends Enum> extends GuiButton {
    public static final int SPACING = 2;
    public static final int PAD_LEFT = 2;
    public static final int PAD_RIGHT = 2;
    public static final int PAD_TOP = 2;
    public static final int PAD_BOTTOM = 1;
    public static final ReadonlyColor DEFAULT_COLOR_ON = Color.DODGER_BLUE;
    public static final ReadonlyColor DEFAULT_COLOR_OFF = Color.DODGER_BLUE.copy().setAlpha(0.0);

    private class Option {
        private T value;
        private int index;
        private String text;
        private ReadonlyColor colorOn;
        private ReadonlyColor colorOff;
        private Color colorCurrent;
        private Timeline colorTimeline;
        private int xBegin;
        private int xEnd;
    }
    private final LinkedHashMap<T, Option> options;
    private boolean allowSetNull;
    private T selectedValue;

    public GuiEnumSelect(Class<T> enumType, FontRenderer fontRenderer, int id, int x, int y) {
        super(id, x, y, 0, 0, null);
        height = PAD_TOP + fontRenderer.FONT_HEIGHT + PAD_BOTTOM;
        width = 0;
        options = new LinkedHashMap<T, Option>();
        int index = 0;
        for (T value : enumType.getEnumConstants()) {
            Option option = new Option();
            option.value = value;
            option.index = index;
            option.text = value.toString().toLowerCase();
            option.colorOn = DEFAULT_COLOR_ON;
            option.colorOff = DEFAULT_COLOR_OFF;
            option.colorCurrent = option.colorOff.copy();
            option.colorTimeline = null;
            if (index > 0) {
                width += SPACING;
            }
            option.xBegin = x + width;
            width += PAD_LEFT + fontRenderer.getStringWidth(option.text) + PAD_RIGHT;
            option.xEnd = x + width;

            options.put(value, option);
            index++;
        }
        if (index == 0) {
            throw new IllegalArgumentException("enum type must have at least one value");
        }
        selectedValue = null;
    }

    private Option getOptionForValue(T value) {
        Option option = options.get(value);
        if (value != null && option == null) {
            throw new IllegalArgumentException("invalid value: " + String.valueOf(value));
        }
        return option;
    }

    public boolean isAllowSetNull() {
        return allowSetNull;
    }
    public GuiEnumSelect<T> setAllowSetNull(boolean allowSetNull) {
        this.allowSetNull = allowSetNull;
        return this;
    }

    public GuiEnumSelect<T> setColors(T value, ReadonlyColor colorOn) {
        return setColors(value, colorOn, colorOn.copy().setAlpha(0.0));
    }
    public GuiEnumSelect<T> setColors(T value, ReadonlyColor colorOn, ReadonlyColor colorOff) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        Option option = getOptionForValue(value);
        option.colorOn = colorOn;
        option.colorOff = colorOff;
        return this;
    }

    public T getSelectedValue() {
        return selectedValue;
    }
    public GuiEnumSelect<T> setSelectedValue(T value, boolean animate) {
        Option prev = getOptionForValue(selectedValue);
        Option cur = getOptionForValue(value);
        selectedValue = value;

        if (prev == cur) {
            return this;
        }

        if (prev != null) {
            // Fade out previous selection
            if (prev.colorTimeline != null && !prev.colorTimeline.isDone()) {
                prev.colorTimeline.abort();
                prev.colorTimeline = null;
            }
            if (animate) {
                prev.colorTimeline = new Timeline(prev.colorCurrent);
                prev.colorTimeline.addPropertyToInterpolate("red",   prev.colorCurrent.getRed(),   prev.colorOff.getRed());
                prev.colorTimeline.addPropertyToInterpolate("green", prev.colorCurrent.getGreen(), prev.colorOff.getGreen());
                prev.colorTimeline.addPropertyToInterpolate("blue",  prev.colorCurrent.getBlue(),  prev.colorOff.getBlue());
                prev.colorTimeline.addPropertyToInterpolate("alpha", prev.colorCurrent.getAlpha(), prev.colorOff.getAlpha());
                prev.colorTimeline.setDuration(500);
                prev.colorTimeline.play();
            } else {
                prev.colorCurrent.set(prev.colorOff);
            }
        }

        if (cur != null) {
            // Fade in current selection
            if (cur.colorTimeline != null && !cur.colorTimeline.isDone()) {
                cur.colorTimeline.abort();
                cur.colorTimeline = null;
            }
            if (animate) {
                cur.colorTimeline = new Timeline(cur.colorCurrent);
                cur.colorTimeline.addPropertyToInterpolate("red",   cur.colorCurrent.getRed(),   cur.colorOn.getRed());
                cur.colorTimeline.addPropertyToInterpolate("green", cur.colorCurrent.getGreen(), cur.colorOn.getGreen());
                cur.colorTimeline.addPropertyToInterpolate("blue",  cur.colorCurrent.getBlue(),  cur.colorOn.getBlue());
                cur.colorTimeline.addPropertyToInterpolate("alpha", cur.colorCurrent.getAlpha(), cur.colorOn.getAlpha());
                cur.colorTimeline.setDuration(500);
                cur.colorTimeline.play();
            } else {
                cur.colorCurrent.set(cur.colorOn);
            }
        }

        return this;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        if (!drawButton) {
            return;
        }
        for (Option option : options.values()) {
            drawRect(
                    option.xBegin,
                    yPosition,
                    option.xEnd,
                    yPosition + height,
                    option.colorCurrent.getARGB());
            minecraft.fontRenderer.drawString(
                    option.text,
                    option.xBegin + PAD_LEFT,
                    yPosition + PAD_TOP,
                    0xffffffff);
        }
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        if (!super.mousePressed(minecraft, mouseX, mouseY)) {
            return false;
        }
        for (Option option : options.values()) {
            if (mouseX >= option.xBegin && mouseX <= option.xEnd) {
                if (allowSetNull && selectedValue == option.value) {
                    setSelectedValue(null, true);
                } else {
                    setSelectedValue(option.value, true);
                }
                return true;
            }
        }
        return false;
    }
}
