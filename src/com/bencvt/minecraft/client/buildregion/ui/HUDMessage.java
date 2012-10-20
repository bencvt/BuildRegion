package com.bencvt.minecraft.client.buildregion.ui;

import libshapedraw.animation.trident.Timeline;
import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiChat;
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
    private String[] lines;
    private Color color;
    private Timeline timeline;

    public HUDMessage(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    /**
     * @param message
     * @param startColor
     * @param fadeToColor if you want a static color, just set this to the same as startColor
     * @param fadeDuration how long the animation lasts in milliseconds
     */
    public void update(String message, ReadonlyColor startColor, ReadonlyColor fadeToColor, long fadeDuration) {
        clear();

        lines = message.split("\n");
        color = startColor.copy();

        timeline = new Timeline(color);
        timeline.addPropertyToInterpolate("red",   startColor.getRed(),   fadeToColor.getRed());
        timeline.addPropertyToInterpolate("blue",  startColor.getBlue(),  fadeToColor.getBlue());
        timeline.addPropertyToInterpolate("green", startColor.getGreen(), fadeToColor.getGreen());
        timeline.addPropertyToInterpolate("alpha", startColor.getAlpha(), fadeToColor.getAlpha());
        timeline.setDuration(fadeDuration);
        timeline.play();
    }

    public void clear() {
        if (timeline != null) {
            timeline.abort();
            timeline = null;
        }
        lines = null;
    }

    public void render() {
        if (timeline == null || timeline.isDone()) {
            return;
        }
        if (minecraft.currentScreen != null && !(minecraft.currentScreen instanceof GuiChat)) {
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
