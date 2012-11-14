package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import net.minecraft.client.Minecraft;
import net.minecraft.src.ScaledResolution;

/**
 * An alternate to spamming the chat window: flash text on the center of the
 * screen for a limited duration. Subsequent messages will overwrite the
 * current message, if any.
 * <p>
 * TODO: move this to LibShapeDraw eventually.
 * 
 * @author bencvt
 */
public class HUDMessage {
    private final Minecraft minecraft;
    private final Color color = Color.WHITE.copy();
    private String[] lines;

    public HUDMessage(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    private boolean isHiddenByGui(boolean highPriority) {
        if (minecraft.currentScreen == null && !minecraft.gameSettings.hideGUI) {
            return false;
        }
        return !highPriority;
    }

    /**
     * @param message
     * @param highPriority if true display the HUD message even if a GUI screen is active
     * @param startColor
     * @param fadeToColor if you want a static color, just set this to the same as startColor
     * @param fadeDuration how long the animation lasts in milliseconds
     */
    public void update(String message, boolean highPriority, ReadonlyColor startColor, ReadonlyColor fadeToColor, long fadeDuration) {
        clear();
        if (isHiddenByGui(highPriority)) {
            return;
        }
        lines = message.split("\n");
        if (lines.length > 0) {
            color.set(startColor).animateStart(fadeToColor, fadeDuration);
        }
    }

    public void clear() {
        color.animateStop();
        lines = null;
    }

    public void render() {
        if (!color.isAnimating() || isHiddenByGui(true)) {
            return;
        }
        int argb = color.getARGB();
        ScaledResolution res = new ScaledResolution(
                minecraft.gameSettings,
                minecraft.displayWidth,
                minecraft.displayHeight);
        int yStart = (res.getScaledHeight() - minecraft.fontRenderer.FONT_HEIGHT) * 1 / 16;
        for (int i = 0; i < lines.length; i++) {
            int x = (res.getScaledWidth() - minecraft.fontRenderer.getStringWidth(lines[i])) / 2;
            int y = yStart + i*minecraft.fontRenderer.FONT_HEIGHT;
            // TODO: Minecraft's font renderer has some whack-ass alpha channel
            // logic... may need to implement custom version for smooth fading.
            minecraft.fontRenderer.drawStringWithShadow(lines[i], x, y, argb);
        }
    }
}
