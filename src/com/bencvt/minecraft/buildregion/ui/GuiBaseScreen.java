package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiScreen;

public abstract class GuiBaseScreen extends GuiScreen {
    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public void rapidUpdate(GuiBaseControl control) {
        // Do nothing. The child class can override.
    }
}
