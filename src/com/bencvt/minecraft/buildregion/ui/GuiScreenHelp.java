package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;

/**
 * TODO: show blurb, usage, credits. Buttons to open URL and to close
 *  
 * @author bencvt
 */
public class GuiScreenHelp extends GuiScreenBase {
    private final GuiStandardButton buttonDone = new GuiStandardButton(this, "Done");

    public GuiScreenHelp(FontRenderer fontRenderer) {
        super(fontRenderer);
    }

    @Override
    public void initGui() {
        buttonDone.position(
                (width - buttonDone.getWidth())/2,
                (height - buttonDone.getHeight())/2);
        controlList.add(buttonDone);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) {
        if (guiButton == buttonDone) {
            mc.displayGuiScreen(null);
        }
    }
}
