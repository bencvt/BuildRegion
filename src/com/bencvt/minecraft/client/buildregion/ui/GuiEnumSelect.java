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
public class GuiEnumSelect<T extends Enum> extends GuiLabeledControl {
    public static final int LABEL_SPACING = 4;
    public static final int OPTION_SPACING = 2;
    public static final int PAD_LEFT = 2;
    public static final int PAD_RIGHT = 2;
    public static final int PAD_TOP = 2;
    public static final int PAD_BOTTOM = 1;
    public static final int ANIM_DURATION = 500;
    public static final double ALPHA_OFF = 0.0;
    public static final double ALPHA_ON = 1.0;

    public class Option {
        protected T value;
        protected String text;
        protected String textMouseOver;
        protected ReadonlyColor color;

        private double alpha;
        // getter/setter required for property interpolation
        public double getAlpha() { return alpha; }
        public void setAlpha(double alpha) { this.alpha = alpha; }
        protected Timeline alphaTimeline;

        protected int xBegin;
        protected int xEnd;
    }
    private final LinkedHashMap<T, Option> options;
    private int labelWidth;
    private boolean allowSetNull;
    private T selectedValue;

    public GuiEnumSelect(Class<T> enumType, String labelText, ReadonlyColor color) {
        super(labelText);
        options = new LinkedHashMap<T, Option>();
        for (T value : enumType.getEnumConstants()) {
            Option option = new Option();
            option.value = value;
            option.text = value.toString().toLowerCase();
            option.textMouseOver = "\u00a7n" + option.text;
            option.color = color;
            option.setAlpha(ALPHA_OFF);
            option.alphaTimeline = null;
            // option.xBegin and option.xEnd deferred to setLayout
            options.put(value, option);
        }
        selectedValue = null;
    }

    @Override
    public void setLayout(FontRenderer fontRenderer, int labelWidth, int x, int y) {
        this.labelWidth = labelWidth;
        xPosition = x;
        yPosition = y;
        height = PAD_TOP + fontRenderer.FONT_HEIGHT + PAD_BOTTOM;
        width = labelWidth + LABEL_SPACING;
        boolean first = true;
        for (Option option : options.values()) {
            if (!first) {
                width += OPTION_SPACING;
            }
            first = false;
            option.xBegin = width;
            width += PAD_LEFT + fontRenderer.getStringWidth(option.text) + PAD_RIGHT;
            option.xEnd = width;
        }
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

    public GuiEnumSelect<T> setOptionColor(T value, ReadonlyColor color) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        getOptionForValue(value).color = color;
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
            if (prev.alphaTimeline != null && !prev.alphaTimeline.isDone()) {
                prev.alphaTimeline.abort();
                prev.alphaTimeline = null;
            }
            if (animate) {
                prev.alphaTimeline = new Timeline(prev);
                prev.alphaTimeline.addPropertyToInterpolate("alpha", prev.getAlpha(), ALPHA_OFF);
                prev.alphaTimeline.setDuration(ANIM_DURATION);
                prev.alphaTimeline.play();
            } else {
                prev.setAlpha(ALPHA_OFF);
            }
        }

        if (cur != null) {
            // Fade in current selection
            if (cur.alphaTimeline != null && !cur.alphaTimeline.isDone()) {
                cur.alphaTimeline.abort();
                cur.alphaTimeline = null;
            }
            if (animate) {
                cur.alphaTimeline = new Timeline(cur);
                cur.alphaTimeline.addPropertyToInterpolate("alpha", cur.getAlpha(), ALPHA_ON);
                cur.alphaTimeline.setDuration(ANIM_DURATION);
                cur.alphaTimeline.play();
            } else {
                cur.setAlpha(ALPHA_ON);
            }
        }

        return this;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        if (!drawButton) {
            return;
        }
        minecraft.fontRenderer.drawString(
                displayString,
                xPosition + labelWidth - minecraft.fontRenderer.getStringWidth(displayString),
                yPosition + PAD_TOP,
                LABEL_COLOR_ARGB);
        for (Option option : options.values()) {
            boolean mouseOver = (
                    mouseX >= option.xBegin && mouseX <= option.xEnd &&
                    mouseY >= yPosition && mouseY <= yPosition + height);
            tempColor.set(option.color).scaleAlpha(option.getAlpha());
            drawRect(
                    option.xBegin,
                    yPosition,
                    option.xEnd,
                    yPosition + height,
                    tempColor.getARGB());
            minecraft.fontRenderer.drawString(
                    mouseOver ? option.textMouseOver : option.text,
                    option.xBegin + PAD_LEFT,
                    yPosition + PAD_TOP,
                    0xffffffff);
        }
    }
    /** so we don't create a bunch of temporary objects when rendering */
    private static final Color tempColor = Color.BLACK.copy();

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
        // clicked in the label or the empty space between options
        return false;
    }
}
