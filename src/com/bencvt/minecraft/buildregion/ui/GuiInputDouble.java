package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.primitive.Color;
import net.minecraft.client.Minecraft;

import com.bencvt.minecraft.buildregion.region.Units;

public class GuiInputDouble extends GuiLabeledControl {
    public static final int PAD_TOP = 2;
    public static final int PAD_BOTTOM = 1;

    public static final int XBEGIN_TEXT   = 0;
    public static final int XEND_TEXT     = XBEGIN_TEXT + 44; // == fontRenderer.getStringWidth("-99999.5")
    public static final int XBEGIN_MINUS  = XEND_TEXT + 2;
    public static final int XEND_MINUS    = XBEGIN_MINUS + 11;
    public static final int XBEGIN_PLUS   = XEND_MINUS + 2;
    public static final int XEND_PLUS     = XBEGIN_PLUS + 11;
    public static final int XBEGIN_SLIDER = XEND_PLUS + 2;
    public static final int MIN_SLIDER_WIDTH = 100;
    public static final int R_XBEGIN_GROUP   = GuiInputDoubleGroup.WIDTH + 3;
    public static final int R_XEND_GROUP     = R_XBEGIN_GROUP + 11;
    public static final int R_XBEGIN_SLIDER  = R_XEND_GROUP + 2;

    public static final int BUTTON_DISABLED_ARGB = Color.BLACK.copy().scaleAlpha(0.25).getARGB();
    public static final int BUTTON_ENABLED_ARGB = Color.LIGHT_GRAY.copy().scaleAlpha(3.0/8.0).getARGB();
    public static final int BUTTON_MOUSEOVER_ARGB = Color.LIGHT_GRAY.copy().scaleAlpha(5.0/8.0).getARGB();
    public static final int BUTTON_LOCKED_ARGB = Color.DODGER_BLUE.getARGB();
    public static final int BUTTON_MOUSEOVERLOCKED_ARGB = Color.DODGER_BLUE.copy().blend(Color.LIGHT_GRAY, 0.25).getARGB();

    public static final int SLIDER_BGLINE0_ARGB = Color.GRAY.copy().scaleAlpha(0.5).getARGB();
    public static final int SLIDER_BGLINE1_ARGB = Color.DARK_GRAY.copy().scaleAlpha(0.5).getARGB();
    public static final int SLIDER_HALF_WIDTH = 2;
    public static final int SLIDER_NORMAL_ARGB = Color.DARK_GRAY.getARGB();
    public static final int SLIDER_DRAGGING_ARGB = BUTTON_LOCKED_ARGB;
    public static final int SLIDER_BORDER_NORMAL_ARGB = Color.LIGHT_GRAY.getARGB();
    public static final int SLIDER_BORDER_MOUSEOVER_ARGB = Color.WHITE.getARGB();
    public static final int SLIDER_BORDER_DRAGGING_ARGB = SLIDER_DRAGGING_ARGB;
    /**
     * The smaller this double, the more pixels the slider has to move before
     * affecting the value.
     */
    public static final double SLIDER_SENSITIVITY = 0.25;

    private double value;
    private final Units units;
    private final boolean positive;
    private final GuiInputDoubleGroup group;
    private boolean dragging;
    private double dragStartValue;

    public GuiInputDouble(GuiBaseScreen parent, String text, Units units, boolean positive, GuiInputDoubleGroup group) {
        super(parent, text);
        this.units = units;
        this.positive = positive;
        this.group = group == null ? null : group.register(this);
        setControlWidth(XBEGIN_SLIDER + MIN_SLIDER_WIDTH + R_XBEGIN_SLIDER); // also sets width
        height = PAD_TOP + parent.getFontRenderer().FONT_HEIGHT + PAD_BOTTOM;
    }

    public double getValue() {
        return value;
    }
    public void setValue(double value) {
        setValueFromGroup(value);
        if (group != null) {
            group.setValue(this.value);
        }
    }
    protected void setValueFromGroup(double value) {
        if (positive) {
            this.value = units.clampAtom(value);
        } else {
            this.value = units.clamp(value);
        }
    }
    private void setValueFromSlider(int xMouse) {
        final int xMinSlider = getControlXOffset() + XBEGIN_SLIDER;
        final int xMaxSlider = xPosition + width - R_XBEGIN_SLIDER;
        final int xMidSlider = xMinSlider + (xMaxSlider - xMinSlider)/2;
        setValue(dragStartValue + units.atom*(xMouse - xMidSlider)*SLIDER_SENSITIVITY);
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
        final int xControlBegin = getControlXOffset();
        final int xControlEnd = xPosition + width;

        // Value as text.
        String valueString = units.d2s(getValue());
        parent.getFontRenderer().drawString(
                valueString,
                xControlBegin + XEND_TEXT - XBEGIN_TEXT - parent.getFontRenderer().getStringWidth(valueString),
                yPosition + PAD_TOP,
                CONTROL_ENABLED_ARGB);

        // Minus button.
        drawMiniButton(xMouse, yMouse, "-",
                isMinusButtonEnabled(), false,
                xControlBegin + XBEGIN_MINUS,
                xControlBegin + XEND_MINUS, xControlBegin + XEND_MINUS);

        // Plus button.
        drawMiniButton(xMouse, yMouse, "+",
                isPlusButtonEnabled(), false,
                xControlBegin + XBEGIN_PLUS,
                xControlBegin + XEND_PLUS, xControlBegin + XEND_PLUS);

        // Slider.
        drawAndUpdateSlider(xMouse, yMouse,
                xControlBegin + XBEGIN_SLIDER, xControlEnd - R_XBEGIN_SLIDER);

        // Group button.
        if (group != null) {
            drawMiniButton(xMouse, yMouse, "=",
                    true, group.isLocked(),
                    xControlEnd - R_XEND_GROUP,
                    xControlEnd - R_XBEGIN_GROUP, xControlEnd);
        }
    }

    private void drawMiniButton(int xMouse, int yMouse, String text, boolean enabled, boolean locked, int xBegin, int xEnd, int xEndMouse) {
        final int buttonARGB;
        final int textARGB;
        if (!enabled) {
            buttonARGB = BUTTON_DISABLED_ARGB;
            textARGB = CONTROL_DISABLED_ARGB;
        } else {
            if (xMouse >= xBegin && xMouse <= xEndMouse &&
                    yMouse >= yPosition && yMouse <= yPosition + height) {
                buttonARGB = locked ? BUTTON_MOUSEOVERLOCKED_ARGB : BUTTON_MOUSEOVER_ARGB;
                textARGB = CONTROL_ENABLED_ARGB;
            } else {
                buttonARGB = locked ? BUTTON_LOCKED_ARGB : BUTTON_ENABLED_ARGB;
                textARGB = CONTROL_MOUSEOVER_ARGB;
            }
        }
        drawRect(xBegin, yPosition, xEnd, yPosition + height - 1, buttonARGB);
        parent.getFontRenderer().drawString(
                text,
                xBegin + (xEnd - xBegin - parent.getFontRenderer().getStringWidth(text))/2 + 1,
                yPosition + PAD_TOP,
                textARGB);
    }

    private void drawAndUpdateSlider(int xMouse, int yMouse, int xMin, int xMax) {
        // Determine where along the slider the vertical rectangle's center is.
        // Update value if we're actively dragging.
        final int x;
        if (dragging) {
            setValueFromSlider(xMouse);
            parent.rapidUpdate(this);
            x = Math.min(Math.max(xMin + SLIDER_HALF_WIDTH, xMouse), xMax - SLIDER_HALF_WIDTH);
        } else {
            x = xMin + (xMax - xMin)/2;
        }

        // Draw background horizontal line.
        final int y = yPosition + (height/2);
        drawRect(xMin, y - 1, xMax, y,     SLIDER_BGLINE0_ARGB);
        drawRect(xMin, y,     xMax, y + 1, SLIDER_BGLINE1_ARGB);

        // Draw vertical rectangle background.
        drawRect(
                x - SLIDER_HALF_WIDTH,
                yPosition,
                x + SLIDER_HALF_WIDTH,
                yPosition + height - 1,
                (dragging ? SLIDER_DRAGGING_ARGB : SLIDER_NORMAL_ARGB));

        // Draw vertical rectangle border on top the background.
        final int borderARGB;
        if (dragging) {
            borderARGB = SLIDER_BORDER_DRAGGING_ARGB;
        } else if (xMouse >= xMin && xMouse <= xMax &&
                yMouse >= yPosition && yMouse <= yPosition + height) {
            borderARGB = SLIDER_BORDER_MOUSEOVER_ARGB;
        } else {
            borderARGB = SLIDER_BORDER_NORMAL_ARGB;
        }
        GuiBaseScreen.drawRectBorder(
                x - SLIDER_HALF_WIDTH,
                yPosition,
                x + SLIDER_HALF_WIDTH,
                yPosition + height - 1,
                borderARGB,
                1);
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int xMouse, int yMouse) {
        if (!super.mousePressed(minecraft, xMouse, yMouse)) {
            return false;
        }
        final int xL = xMouse - getControlXOffset();
        final int xR = xPosition + width - xMouse;
        if (xL >= XBEGIN_TEXT && xL <= XEND_TEXT) {
            System.out.println("you clicked text");//TODO: turn into text input box
            return true;
        } else if (isMinusButtonEnabled() && xL >= XBEGIN_MINUS && xL <= XEND_MINUS) {
            setValue(getValue() - units.atom);
            return true;
        } else if (isPlusButtonEnabled() && xL >= XBEGIN_PLUS && xL <= XEND_PLUS) {
            setValue(getValue() + units.atom);
            return true;
        } else if (xL >= XBEGIN_SLIDER && xR >= R_XBEGIN_SLIDER) {
            dragging = true;
            dragStartValue = getValue();
            setValueFromSlider(xMouse);
            return true;
        } else if (group != null && xR <= R_XEND_GROUP) {
            if (group.isLocked()) {
                group.unlock();
            } else {
                group.lock(value);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void mouseReleased(int xMouse, int yMouse) {
        dragging = false;
    }
}
