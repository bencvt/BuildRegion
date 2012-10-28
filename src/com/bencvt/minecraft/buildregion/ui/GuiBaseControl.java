package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.src.GuiButton;

public abstract class GuiBaseControl extends GuiButton {
    private static int topId;

    protected final GuiBaseScreen parent;

    protected GuiBaseControl(GuiBaseScreen parent, String text) {
        super(topId, 0, 0, 0, 0, text);
        // caller is responsible for setting xPosition and yPosition using position()
        topId++;
        this.parent = parent;
    }

    public final boolean isVisible() {
        return drawButton;
    }
    public final void setVisible(boolean visible) {
        drawButton = visible;
    }

    public final boolean isEnabled() {
        return enabled;
    }
    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public final String getText() {
        return displayString;
    }
    public final void setText(String text) {
        displayString = text;
    }
    
    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    public final GuiBaseControl position(int xPosition, int yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        return this;
    }
}
