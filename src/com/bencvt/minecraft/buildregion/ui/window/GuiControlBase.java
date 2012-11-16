package com.bencvt.minecraft.buildregion.ui.window;

import net.minecraft.src.GuiButton;

/**
 * Abstract GuiButton base class. Same as vanilla Minecraft's, plus various
 * utility methods:<ul>
 * <li>Automatically set the button id.</li>
 * <li>Add a reference to the GuiScreenBase parent screen.</li>
 * <li>Add fluent getter/setter methods for non-final public fields. The fields
 *     are still publicly mutable (ugh), but at least there's methods too.</li>
 * <li>Add keyTyped method, which is called by the parent screen after calling
 *     parent.setControlConsumingKeys(this).</li>
 * </ul>
 * @author bencvt
 */
public abstract class GuiControlBase extends GuiButton {
    private static int topId;

    protected final GuiScreenBase parent;

    public GuiControlBase(GuiScreenBase parent, String text) {
        super(topId, 0, 0, 0, 0, text == null ? "" : text);
        // Caller is responsible for setting xPosition and yPosition.
        // Derived class is responsible for setting width and height.
        topId++;
        this.parent = parent;
    }

    public final boolean isVisible() {
        return drawButton;
    }
    public final GuiControlBase setVisible(boolean visible) {
        drawButton = visible;
        return this;
    }

    public final boolean isEnabled() {
        return enabled;
    }
    public final GuiControlBase setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public final String getText() {
        return displayString;
    }
    public final GuiControlBase setText(String text) {
        displayString = text;
        return this;
    }

    public final int getWidth() {
        return width;
    }
    public final GuiControlBase setWidth(int width) {
        this.width = width;
        return this;
    }

    public final int getHeight() {
        return height;
    }
    public final GuiControlBase setHeight(int height) {
        this.height = height;
        return this;
    }

    public final int getXPosition() {
        return xPosition;
    }
    public final GuiControlBase setXPosition(int xPosition) {
        this.xPosition = xPosition;
        return this;
    }

    public final int getYPosition() {
        return yPosition;
    }
    public final GuiControlBase setYPosition(int yPosition) {
        this.yPosition = yPosition;
        return this;
    }

    public final GuiControlBase setPositionXY(int xPosition, int yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        return this;
    }

    public final boolean isMouseOver(int xMouse, int yMouse) {
        return parent.isMouseOver(
                xMouse, xPosition, xPosition + width,
                yMouse, yPosition, yPosition + height);
    }

    public boolean keyTyped(char keyChar, int keyCode) {
        return false;
    }
}
