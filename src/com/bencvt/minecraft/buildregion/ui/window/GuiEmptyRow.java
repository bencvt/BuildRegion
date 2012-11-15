package com.bencvt.minecraft.buildregion.ui.window;

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
