package com.bencvt.minecraft.buildregion.ui.window;

import libshapedraw.primitive.Color;
import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.bencvt.minecraft.buildregion.region.Units;

/**
 * Allow the user to input a constrained floating-point number in various ways.
 * Designed to be flexible and user-friendly. The user can change the number
 * by:<ul>
 * <li>clicking on the number itself and then entering the number on the
 *     keyboard;</li>
 * <li>clicking plus (+) and minus (-) buttons;</li>
 * <li>using the click-and-drag slider; or</li>
 * <li>hovering the mouse over the control and using the mouse scroll wheel.</li>
 * </ul>
 * Holding the shift key while clicking the +/- buttons or using the mouse
 * wheel will increase the magnitude of the change.
 * 
 * @author bencvt
 */
public class GuiInputNumber extends GuiLabeledControl {
    public static final int PAD_TOP = 2;
    public static final int PAD_BOTTOM = 1;

    public static final int XBEGIN_TEXT   = 0;
    public static final int XEND_TEXT     = XBEGIN_TEXT + 32;
    public static final int XBEGIN_MINUS  = XEND_TEXT + 2;
    public static final int XEND_MINUS    = XBEGIN_MINUS + 11;
    public static final int XBEGIN_PLUS   = XEND_MINUS + 2;
    public static final int XEND_PLUS     = XBEGIN_PLUS + 11;
    public static final int XBEGIN_SLIDER = XEND_PLUS + 2;
    public static final int MIN_SLIDER_WIDTH = 100;
    public static final int R_XBEGIN_GROUP   = GuiInputNumberGroup.WIDTH + 3;
    public static final int R_XEND_GROUP     = R_XBEGIN_GROUP + 11;
    public static final int R_XBEGIN_SLIDER  = R_XEND_GROUP + 2;

    public static final int TEXT_VALID_ARGB = Color.DODGER_BLUE.getARGB();
    public static final int TEXT_INVALID_ARGB = Color.CRIMSON.getARGB();
    public static final int TEXT_MOUSEOVER_ARGB = Color.BLACK.copy().scaleAlpha(0.25).getARGB();
    public static final int TEXT_CURSOR_ARGB = Color.WHITE.getARGB();

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
    private String valueInput;
    private boolean valueInputValid;
    private Units units;
    private final boolean positive;
    private final GuiInputNumberGroup group;
    private boolean dragging;
    private Double dragBaseValue;

    public GuiInputNumber(GuiScreenBase parent, String text, Units units, boolean positive, GuiInputNumberGroup group) {
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
        setValue(dragBaseValue + units.atom*(xMouse - xMidSlider)*SLIDER_SENSITIVITY);
    }

    public Units getUnits() {
        return units;
    }
    public void setUnits(Units units) {
        if (units == null) {
            throw new IllegalArgumentException();
        }
        this.units = units;
        setValueFromGroup(value); // re-clamp
    }

    private boolean isMinusButtonEnabled() {
        return !positive || value > units.atom;
    }

    private boolean isPlusButtonEnabled() {
        return true;
    }

    private void setDragging(boolean dragging) {
        this.dragging = dragging;
        if (!dragging) {
            dragBaseValue = null;
        }
        if (group != null) {
            group.setDragging(dragging);
        }
        Mouse.setGrabbed(dragging);
    }
    protected void setDraggingFromGroup(boolean dragging) {
        this.dragging = dragging;
    }

    @Override
    protected int getLabelYOffset() {
        return PAD_TOP;
    }

    private double getFactor() {
        return parent.isShiftKeyDown() ? 10.0 : 1.0;
    }

    @Override
    protected void mouseWheelScrolled(boolean back) {
        final double prev = value;
        if (back) {
            setValue(value - units.atom*getFactor());
        } else {
            setValue(value + units.atom*getFactor());
        }
        if (value != prev) {
            parent.actionPerformedByControl(this);
        }
        parent.setControlConsumingKeys(null);
    }

    @Override
    protected void drawControl(int xMouse, int yMouse) {
        final int xControlBegin = getControlXOffset();
        final int xControlEnd = xPosition + width;

        // Value as text.
        drawValueString(xMouse, yMouse,
                xControlBegin + XBEGIN_TEXT, xControlBegin + XEND_TEXT);

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

    private void drawValueString(int xMouse, int yMouse, int xBegin, int xEnd) {
        final int yEnd = yPosition + height - 1;
        final String text;
        if (parent.getControlConsumingKeys() == this) {
            drawRect(xBegin, yPosition, xEnd, yEnd, valueInputValid ? TEXT_VALID_ARGB : TEXT_INVALID_ARGB);
            drawRect(xEnd, yPosition, xEnd - 1, yEnd, TEXT_CURSOR_ARGB);
            text = valueInput;
        } else if (parent.isMouseOver(xMouse, xBegin, xEnd, yMouse, yPosition, yEnd)) {
            drawRect(xBegin, yPosition, xEnd, yEnd, TEXT_MOUSEOVER_ARGB);
            text = units.d2s(value);
        } else {
            text = units.d2s(value);
        }
        parent.getFontRenderer().drawString(
                text,
                xEnd - XBEGIN_TEXT - parent.getFontRenderer().getStringWidth(text) - 1,
                yPosition + PAD_TOP,
                CONTROL_ENABLED_ARGB);
    }

    private void drawMiniButton(int xMouse, int yMouse, String text, boolean enabled, boolean locked, int xBegin, int xEnd, int xEndMouse) {
        final int buttonARGB;
        final int textARGB;
        if (!enabled) {
            buttonARGB = BUTTON_DISABLED_ARGB;
            textARGB = CONTROL_DISABLED_ARGB;
        } else {
            if (parent.isMouseOver(
                    xMouse, xBegin, xEndMouse,
                    yMouse, yPosition, yPosition + height)) {
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
        // Update the value we're actively dragging.
        if (dragging && dragBaseValue != null) {
            final double prev = value;
            setValueFromSlider(xMouse);
            if (value != prev) {
                parent.actionPerformedByControl(this);
            }
        }
        // Else we're either not dragging, or this slider is mirroring another
        // slider in its group.

        // Draw background horizontal line.
        final int y = yPosition + (height/2);
        drawRect(xMin, y - 1, xMax, y,     SLIDER_BGLINE0_ARGB);
        drawRect(xMin, y,     xMax, y + 1, SLIDER_BGLINE1_ARGB);

        // Determine where along the slider the vertical rectangle's center is.
        final int x;
        if (dragging) {
            x = Math.min(Math.max(xMin + SLIDER_HALF_WIDTH, xMouse), xMax - SLIDER_HALF_WIDTH);
        } else {
            x = xMin + (xMax - xMin)/2;
        }

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
        } else if (parent.isMouseOver(
                xMouse, xMin, xMax,
                yMouse, yPosition, yPosition + height)) {
            borderARGB = SLIDER_BORDER_MOUSEOVER_ARGB;
        } else {
            borderARGB = SLIDER_BORDER_NORMAL_ARGB;
        }
        GuiScreenBase.drawRectBorder(
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
            parent.setControlConsumingKeys(this);
            valueInput = units.d2s(value);
            valueInputValid = true;
            return true;
        } else if (isMinusButtonEnabled() && xL >= XBEGIN_MINUS && xL <= XEND_MINUS) {
            setValue(value - units.atom*getFactor());
            return true;
        } else if (isPlusButtonEnabled() && xL >= XBEGIN_PLUS && xL <= XEND_PLUS) {
            setValue(value + units.atom*getFactor());
            return true;
        } else if (xL >= XBEGIN_SLIDER && xR >= R_XBEGIN_SLIDER) {
            setDragging(true);
            dragBaseValue = value;
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
        setDragging(false);
    }

    @Override
    public boolean keyTyped(char keyChar, int keyCode) {
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_TAB ||
                keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RIGHT) {
            parent.setControlConsumingKeys(null);
            return true;
        } else if (keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_LEFT) {
            if (valueInput.isEmpty()) {
                return false;
            }
            valueInput = valueInput.substring(0, valueInput.length() - 1);
        } else if (keyChar == '-' && valueInput.isEmpty()) {
            valueInput = "-";
        } else if (keyChar == '.' && valueInput.indexOf('.') < 0 && units != Units.WHOLE) {
            valueInput += ".";
        } else if (keyChar >= '0' && keyChar <= '9') {
            if (units == Units.HALF) {
                int dec = valueInput.indexOf('.');
                if (dec >= 0) {
                    if (dec <= valueInput.length() - 2) {
                        return false;
                    } else if (keyChar != '0' && keyChar != '5') {
                        return false;
                    }
                }
            }
            valueInput += keyChar;
        } else {
            return false;
        }

        // Attempt to parse and set the value.
        double parsed = 0.0;
        if (valueInput.isEmpty() || valueInput.equals("-") || valueInput.equals(".")) {
            valueInputValid = false;
        } else {
            try {
                parsed = Double.parseDouble(valueInput);
                valueInputValid = true;
            } catch (NumberFormatException e) {
                // Should never happen since we were super-picky about what
                // chars to accept above.
                valueInputValid = false;
            }
        }
        if (valueInputValid) {
            final double prev = value;
            setValue(parsed);
            if (value != parsed) {
                // Value was capped.
                valueInputValid = false;
                setValue(prev);
            } else {
                parent.actionPerformedByControl(this);
            }
        }

        return true;
    }
}
