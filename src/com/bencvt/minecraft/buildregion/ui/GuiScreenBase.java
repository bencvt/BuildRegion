package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.primitive.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Tessellator;

import org.lwjgl.opengl.GL11;

import com.bencvt.minecraft.buildregion.lang.LocalizedString;

/**
 * Abstract GuiScreen base class. Same as vanilla Minecraft's, plus various
 * utility methods:<ul>
 * <li>Minecraft and FontRenderer fields are set in the constructor, so they
 *     will never be null. Better than than having to wait for initGui.</li>
 * <li>Add parentScreen, open, and close for screens that open
 *     other screens temporarily.</li>
 * <li>Add onControlClick and onControlUpdate handlers</li>
 * <li>Add drawBottomOverlay and drawRectBorder</li>
 * </ul>
 * @author bencvt
 */
public abstract class GuiScreenBase extends GuiScreen {
    public final GuiScreenBase parentScreen;
    private Boolean needToPlayClickSound;

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

    /** Intended to be called from GuiControlBase.mousePressed. */
    public final boolean muteNextClickSound() {
        needToPlayClickSound = false;
        return true;
    }

    @Override
    protected final void mouseClicked(int xMouse, int yMouse, int mouseButton) {
        needToPlayClickSound = null;

        float origVolume = mc.gameSettings.soundVolume;
        mc.gameSettings.soundVolume = 0.0F;
        super.mouseClicked(xMouse, yMouse, mouseButton);
        mc.gameSettings.soundVolume = origVolume;

        if (needToPlayClickSound != null && needToPlayClickSound) {
            mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
        }
    }

    @Override
    protected final void actionPerformed(GuiButton guiButton) {
        if (needToPlayClickSound == null) {
            needToPlayClickSound = true;
        }
        onControlClick(guiButton);
    }

    /**
     * Called whenever a control is clicked, assuming the control's
     * mousePressed method returns true.
     */
    protected void onControlClick(GuiButton guiButton) {
        // do nothing by default
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
    public void onControlUpdate(GuiControlBase control, boolean rapid) {
        // do nothing by default
    }

    public static int BOTTOM_OVERLAY_HEIGHT = 28;

    public void drawBottomOverlay() {
        final int top = height - BOTTOM_OVERLAY_HEIGHT;
        drawRect(0, top, width, height, Color.PALE_GOLDENROD.getARGB());
        //if(true)return;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/gui/background.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        //GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorRGBA_I(4210752, 255);
        //tess.setColorRGBA(0x40, 0x40, 0x40, 0xff);
        tess.addVertexWithUV(0.0,   top,    0.0, 0.0,        top/32.0);
        tess.addVertexWithUV(width, top,    0.0, width/32.0, top/32.0);
        tess.setColorRGBA_I(4210752, 255);
        //tess.setColorRGBA(0x40, 0x40, 0x40, 0xff);
        tess.addVertexWithUV(width, height, 0.0, width/32.0, height/32.0);
        tess.addVertexWithUV(0.0,   height, 0.0, 0.0,        height/32.0);
        tess.draw();
        // TODO: more rendering to do.....
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

    public static String i18n(String key, Object ... args) {
        return LocalizedString.translate(key, args);
    }
}
