package com.bencvt.minecraft.buildregion.ui.window;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiControls;

/**
 * TODO: user-specified options. Possible options:
 * Allow torch placement regardless of region: {YES, NO}
 * Remember region type between sessions: {YES, NO}
 * New region placement: {ORIGIN, CORNER}
 * GUI camera adjust: {YES, NO}
 * Check for new BuildRegion version: {YES, NO}
 * WorldGuard integration: {YES, PROMPT, NO}
 * 
 * @author bencvt
 */
public class GuiScreenOptions extends GuiScreenBase {
    private final GuiStandardButton buttonControls = new GuiStandardButton(this, i18n("button.controls"));
    private final GuiStandardButton buttonDone = new GuiStandardButton(this, i18n("button.done"));

    public GuiScreenOptions(GuiScreenBase parentScreen) {
        super(parentScreen);
    }

    @Override
    public void initGui() {
        buttonControls.setWidth(150).setPositionXY(width/2 - 152, height - 30);
        controlList.add(buttonControls);

        buttonDone.setWidth(150).setPositionXY(width/2 + 2, height - 30);
        controlList.add(buttonDone);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) {
        if (guiButton == buttonDone) {
            close();
        } else if (guiButton == buttonControls) {
            open(new GuiControls(this, mc.gameSettings));
        }
    }
}
