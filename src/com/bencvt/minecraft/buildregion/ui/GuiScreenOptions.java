package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;

/**
 * TODO: user-specified options. Possible options:
 * Remember region type between sessions: {YES, NO}
 * New region placement: {ORIGIN, CORNER}
 * GUI camera adjust: {YES, NO}
 * Check for new BuildRegion version: {YES, NO}
 * WorldGuard integration: {YES, PROMPT, NO}
 * 
 * @author bencvt
 */
public class GuiScreenOptions extends GuiScreenBase {
    public GuiScreenOptions(FontRenderer fontRenderer) {
        super(fontRenderer);
    }

    private final GuiStandardButton buttonDone = new GuiStandardButton(this, "Done");

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
