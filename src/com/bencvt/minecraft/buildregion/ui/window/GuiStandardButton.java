package com.bencvt.minecraft.buildregion.ui.window;

import net.minecraft.client.Minecraft;

public class GuiStandardButton extends GuiControlBase {
    public static final int PAD = 8;

    public GuiStandardButton(GuiScreenBase parent, String text) {
        super(parent, text);
        height = 20;
        width = PAD*2 + parent.getFontRenderer().getStringWidth(getText());
    }

    @Override
    public void drawButton(Minecraft minecraft, int xMouse, int yMouse) {
        if (parent.isMouseLooking()) {
            // Disable mouseover highlighting if the player is looking around
            // by holding right-click.
            super.drawButton(minecraft, Integer.MIN_VALUE, Integer.MIN_VALUE);
        } else {
            super.drawButton(minecraft, xMouse, yMouse);
        }
    }
}
