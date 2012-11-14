package com.bencvt.minecraft.buildregion.ui.window;

import java.util.HashSet;

public class GuiInputNumberGroup {
    public static final int UNLOCKED_ARGB = GuiInputNumber.BUTTON_ENABLED_ARGB;
    public static final int LOCKED_ARGB = GuiInputNumber.BUTTON_LOCKED_ARGB;
    public static final int WIDTH = 5;

    private final HashSet<GuiInputNumber> fields = new HashSet<GuiInputNumber>();
    private boolean locked;

    public GuiInputNumberGroup register(GuiInputNumber field) {
        fields.add(field);
        return this;
    }

    public void setValue(double value) {
        if (locked) {
            for (GuiInputNumber field : fields) {
                field.setValueFromGroup(value);
            }
        }
    }

    public void setDragging(boolean dragging) {
        if (locked) {
            for (GuiInputNumber field : fields) {
                field.setDraggingFromGroup(dragging);
            }
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void unlock() {
        locked = false;
    }

    public void lock(double value) {
        locked = true;
        for (GuiInputNumber field : fields) {
            field.setValueFromGroup(value);
        }
    }

    public void lockIfAllEqual() {
        double value = 0.0;
        boolean first = true;
        boolean allEqual = true;
        for (GuiInputNumber field : fields) {
            if (first) {
                value = field.getValue();
                first = false;
            } else if (field.getValue() != value) {
                allEqual = false;
                break;
            }
        }
        locked = allEqual;
    }

    public void draw() {
        final int argb = locked ? LOCKED_ARGB : UNLOCKED_ARGB;
        int yMin = Integer.MIN_VALUE;
        int yMax = Integer.MAX_VALUE;
        int xEnd = 0;
        for (GuiInputNumber field : fields) {
            int yTop = field.yPosition + (field.getHeight() + WIDTH)/2;
            yMin = Math.max(yMin, yTop);
            int yBottom = field.yPosition + field.getHeight() - (field.getHeight() + WIDTH)/2;
            yMax = Math.min(yMax, yBottom);
            xEnd = field.xPosition + field.getWidth();
            GuiInputNumber.drawRect(
                    xEnd - GuiInputNumber.R_XBEGIN_GROUP,
                    yTop,
                    xEnd - WIDTH,
                    yBottom,
                    argb);
        }
        GuiInputNumber.drawRect(xEnd - WIDTH, yMin, xEnd, yMax, argb);
    }
}
