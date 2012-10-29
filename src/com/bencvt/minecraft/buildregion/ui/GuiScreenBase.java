package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiScreen;

/**
 * Abstract GuiScreen base class. Same as vanilla Minecraft's, plus various
 * utility methods:<ul>
 * <li>Minecraft and FontRenderer fields are set in the constructor, so they
 *     will never be null. Better than than having to wait for initGui.</li>
 * <li>Add parentScreen, open, and close for screens that open
 *     other screens temporarily.</li>
 * <li>Add controlUpdate handler</li>
 * <li>drawRectBorder</li>
 * </ul>
 * TODO: add an option to temporarily hide the HUD, or at least chat, while the screen is open
 * @author bencvt
 */
public abstract class GuiScreenBase extends GuiScreen {
    public final GuiScreenBase parentScreen;
    private boolean needToPlayClickSound;

    public GuiScreenBase(GuiScreenBase parentScreen) {
        this.parentScreen = parentScreen;
        mc = Minecraft.getMinecraft();
        fontRenderer = mc.fontRenderer;
    }

    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public void open() {
        mc.displayGuiScreen(this);
    }
    public void open(GuiScreen newScreen) {
        mc.displayGuiScreen(newScreen);
    }
    public void close() {
        mc.displayGuiScreen(parentScreen);
    }

    /**
     * When a control is clicked, its parent GuiScreen's actionPerformed method
     * is invoked, assuming the control's mousePressed method returns true.
     * <p>
     * However this does not cover other ways that a control can be updated,
     * such as dragging the mouse.
     * 
     * @param control the control being updated
     * @param rapid if true the control is being rapidly updated and the screen
     *              should expect subsequent updates within a few milliseconds
     */
    public void controlUpdate(GuiControlBase control, boolean rapid) {
        // do nothing by default
    }

    /** Intended to be called from GuiControlBase.mousePressed. */
    public boolean muteNextClickSound() {
        needToPlayClickSound = false;
        return true;
    }

    @Override
    protected void mouseClicked(int xMouse, int yMouse, int mouseButton) {
        needToPlayClickSound = true;

        float origVolume = mc.gameSettings.soundVolume;
        mc.gameSettings.soundVolume = 0.0F;
        super.mouseClicked(xMouse, yMouse, mouseButton);
        mc.gameSettings.soundVolume = origVolume;

        if (needToPlayClickSound) {
            mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
            needToPlayClickSound = false;
        }
    }

    public static void drawRectBorder(int xLeft, int yTop, int xRight, int yBottom, int borderARGB, int borderThickness) {
        // Draw top border, including corners.
        drawRect(
                xLeft,
                yTop,
                xRight,
                yTop + borderThickness,
                borderARGB);
        // Draw bottom border, including corners.
        drawRect(
                xLeft,
                yBottom - borderThickness,
                xRight,
                yBottom,
                borderARGB);
        // Draw left border, excluding corners.
        drawRect(
                xLeft,
                yTop + borderThickness,
                xLeft + borderThickness,
                yBottom - borderThickness,
                borderARGB);
        // Draw right border, excluding corners.
        drawRect(
                xRight - borderThickness,
                yTop + borderThickness,
                xRight,
                yBottom - borderThickness,
                borderARGB);
    }
}
