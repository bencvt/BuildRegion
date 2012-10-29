package com.bencvt.minecraft.buildregion.ui;


public class GuiStandardButton extends GuiControlBase {
    public static final int PAD = 8;

    public GuiStandardButton(GuiScreenBase parent, String text) {
        super(parent, text);
        height = 20;
        width = PAD*2 + parent.getFontRenderer().getStringWidth(getText());
    }
}
