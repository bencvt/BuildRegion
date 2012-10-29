package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.src.GuiButton;

/**
 * Abstract GuiButton base class. Same as vanilla Minecraft's, plus various
 * utility methods:<ul>
 * <li>Automatically set the button id</li>
 * <li>Add a reference to the GuiScreenBase parent window</li>
 * <li>Add fluent getter/setter methods for non-final public fields. The fields
 *     are still publicly mutable (ugh), but at least there's methods too.</li>
 * </ul>
 * @author bencvt
 */
public abstract class GuiControlBase extends GuiButton {
    private static int topId;

    protected final GuiScreenBase parent;

    public GuiControlBase(GuiScreenBase parent, String text) {
        super(topId, 0, 0, 0, 0, text);
        // Caller is responsible for setting xPosition and yPosition using position().
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

    public final GuiControlBase position(int xPosition, int yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        return this;
    }
}
