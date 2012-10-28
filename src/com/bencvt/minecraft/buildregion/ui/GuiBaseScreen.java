package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiScreen;

public abstract class GuiBaseScreen extends GuiScreen {
    private boolean disableAutoClickSound;

    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    /**
     * When a control is clicked, its parent GuiScreen's actionPerformed method
     * is automatically invoked. However this does not cover other ways that a
     * control can be updated, such as dragging the mouse.
     * 
     * @param control the control being updated
     * @param rapid if true the control is being rapidly updated and the screen
     *              should expect subsequent updates within a few milliseconds
     */
    public void controlUpdate(GuiBaseControl control, boolean rapid) {
        // Do nothing. The child class can override.
    }

    @Override
    protected void mouseClicked(int xMouse, int yMouse, int mouseButton) {
        if (disableAutoClickSound) {
            // We have to hackishly change the sound volume setting because we
            // can't cleanly avoid calling super.mouseClicks. It sets
            // selectedButton, a private field.
            float origVolume = mc.gameSettings.soundVolume;
            mc.gameSettings.soundVolume = 0.0F;
            super.mouseClicked(xMouse, yMouse, mouseButton);
            mc.gameSettings.soundVolume = origVolume;
        } else {
            super.mouseClicked(xMouse, yMouse, mouseButton);
        }
    }

    /**
     * Manually play the click sound, but only if automatic click sounds are
     * disabled.
     */
    public void playClickSoundManual() {
        if (disableAutoClickSound) {
            mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
        }
    }

    /**
     * Manually play the click sound, regardless of whether automatic click
     * sounds are enabled. 
     */
    public void playClickSoundAbsolute() {
        mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
    }

    public boolean isDisableAutoClickSound() {
        return disableAutoClickSound;
    }
    public void setDisableAutoClickSound(boolean disableAutoClickSound) {
        this.disableAutoClickSound = disableAutoClickSound;
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
