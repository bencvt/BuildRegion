package com.bencvt.minecraft.buildregion.ui.window;

/**
 * A display-only GuiLabeledControl that is rendered as a blank space.
 * 
 * @author bencvt
 */
public class GuiEmptyRow extends GuiLabeledControl {
    public GuiEmptyRow(GuiScreenBase parent, int height) {
        super(parent, null);
        this.height = height;
        setVisible(false);
    }

    @Override
    protected void drawControl(int xMouse, int yMouse) {
        // do nothing
    }
}
