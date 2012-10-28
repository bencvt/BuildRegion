package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.client.Minecraft;

public class GuiStandardButton extends GuiBaseControl {
    public static final int PAD = 8;

    public GuiStandardButton(GuiBaseScreen parent, String text) {
        super(parent, text);
        height = 20;
        width = PAD*2 + parent.getFontRenderer().getStringWidth(text);
    }
}
