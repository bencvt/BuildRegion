package com.bencvt.minecraft.buildregion.ui;

import java.util.HashSet;

public class GuiInputDoubleGroup {
    public static final int UNLOCKED_ARGB = GuiInputDouble.BUTTON_ENABLED_ARGB;
    public static final int LOCKED_ARGB = GuiInputDouble.BUTTON_LOCKED_ARGB;
    public static final int WIDTH = 5;

    private final HashSet<GuiInputDouble> fields = new HashSet<GuiInputDouble>();
    private boolean locked;

    public GuiInputDoubleGroup register(GuiInputDouble field) {
        fields.add(field);
        return this;
    }

    public void setValue(double value) {
        if (locked) {
            for (GuiInputDouble field : fields) {
                field.setValueFromGroup(value);
            }
        }
    }

    public void setDragging(boolean dragging) {
        if (locked) {
            for (GuiInputDouble field : fields) {
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
        for (GuiInputDouble field : fields) {
            field.setValueFromGroup(value);
        }
    }

    public void lockIfAllEqual() {
        double value = 0.0;
        boolean first = true;
        boolean allEqual = true;
        for (GuiInputDouble field : fields) {
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
        for (GuiInputDouble field : fields) {
            int yTop = field.yPosition + (field.getHeight() + WIDTH)/2;
            yMin = Math.max(yMin, yTop);
            int yBottom = field.yPosition + field.getHeight() - (field.getHeight() + WIDTH)/2;
            yMax = Math.min(yMax, yBottom);
            xEnd = field.xPosition + field.getWidth();
            GuiInputDouble.drawRect(
                    xEnd - GuiInputDouble.R_XBEGIN_GROUP,
                    yTop,
                    xEnd - WIDTH,
                    yBottom,
                    argb);
        }
        GuiInputDouble.drawRect(xEnd - WIDTH, yMin, xEnd, yMax, argb);
    }
}
