package com.bencvt.minecraft.client.buildregion.ui;

import libshapedraw.primitive.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;

public abstract class GuiLabeledControl extends GuiButton {
    public static final int LABEL_SPACING = 4;
    public static final int LABEL_COLOR_ARGB = Color.LIGHT_GRAY.getARGB();
    public static final int CONTROL_COLOR_ARGB = Color.WHITE.getARGB();
    private static int topId;

    public GuiLabeledControl(String labelText) {
        super(topId, 0, 0, 0, 0, labelText);
        // xPosition, yPosition, width, and height deferred to setLayout
        topId++;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public abstract void setLayout(FontRenderer fontRenderer, int labelWidth, int x, int y);

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        // do nothing
    }
}
