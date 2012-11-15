package com.bencvt.minecraft.buildregion.ui.window;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Tessellator;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.bencvt.minecraft.buildregion.lang.LocalizedString;

/**
 * Abstract GuiScreen base class. Same as vanilla Minecraft's, plus various
 * utility methods:<ul>
 * <li>Minecraft and FontRenderer fields are set in the constructor, so they
 *     will never be null. Better than having to wait for initGui.</li>
 * <li>Add parentScreen, open, and close for screens that open
 *     other screens temporarily.</li>
 * <li>Add actionPerformedByControl handler.</li>
 * <li>Add the optional (enabled by default) ability to look around while in
 *     the GUI by moving the mouse while holding the right button.</li>
 * <li>Add drawBottomOverlay and drawRectBorder.</li>
 * </ul>
 * @author bencvt
 */
public abstract class GuiScreenBase extends GuiScreen {
    public static int BOTTOM_OVERLAY_HEIGHT = 28;

    public final GuiScreenBase parentScreen;
    private boolean allowMouseLook;
    private boolean mouseLooking;
    private GuiControlBase controlConsumingKeys;

    public GuiScreenBase(GuiScreenBase parentScreen) {
        this.parentScreen = parentScreen;
        allowMouseLook = true;
        mc = Minecraft.getMinecraft();
        fontRenderer = mc.fontRenderer;
    }

    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public void open() {
        open(this);
    }
    public void open(GuiScreen newScreen) {
        Mouse.setGrabbed(false);
        mc.displayGuiScreen(newScreen);
    }
    public void close() {
        Mouse.setGrabbed(false);
        mc.displayGuiScreen(parentScreen);
    }

    /**
     * When a control is clicked, its parent GuiScreen's actionPerformed method
     * is invoked, assuming the control's mousePressed method returns true.
     * <p>
     * However this does not cover other ways that a control can be updated,
     * such as dragging the mouse or using the mouse wheel. This method can be
     * called by child controls when those events occur.
     */
    public void actionPerformedByControl(GuiControlBase guiButton) {
        // do nothing by default
    }

    public GuiControlBase getControlConsumingKeys() {
        return controlConsumingKeys;
    }
    /**
     * Designate a child control to send all keyboard events to.
     * Cancelled if the mouse is clicked.
     */
    public void setControlConsumingKeys(GuiControlBase controlConsumingKeys) {
        this.controlConsumingKeys = controlConsumingKeys;
    }

    @Override
    protected void mouseClicked(int xMouse, int yMouse, int button) {
        controlConsumingKeys = null;
        super.mouseClicked(xMouse, yMouse, button);
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if (controlConsumingKeys == null || !controlConsumingKeys.keyTyped(keyChar, keyCode)) {
            super.keyTyped(keyChar, keyCode);
        }
    }

    @Override
    public void drawScreen(int xMouse, int yMouse, float partialTick) {
        super.drawScreen(xMouse, yMouse, partialTick);
        if (allowMouseLook) {
            boolean newState = Mouse.isButtonDown(1);
            if (newState != mouseLooking) {
                Mouse.setGrabbed(newState);
                mouseLooking = newState;
            }
            if (mouseLooking) {
                // Player is holding the right mouse button and moving around.
                // The updated angles won't affect anything until the next render
                // frame as the world has already been rendered at this point.
                mouseLook();
            }
        }
    }

    public boolean isAllowMouseLook() {
        return allowMouseLook;
    }
    public void setAllowMouseLook(boolean allowMouseLook) {
        this.allowMouseLook = allowMouseLook;
    }

    public boolean isMouseLooking() {
        return mouseLooking;
    }

    public boolean isMouseOver(int xMouse, int xMin, int xMax, int yMouse, int yMin, int yMax) {
        return !mouseLooking && xMouse >= xMin && xMouse <= xMax && yMouse >= yMin && yMouse <= yMax;
    }

    /** Adjust the game world camera to match mouse movements. */
    protected void mouseLook() {
        // The coefficients below are from EntityRenderer.updateCameraAndRender,
        // excluding smooth camera handling.
        float factor = mc.gameSettings.mouseSensitivity*0.6F + 0.2F;
        factor = 8.0F*factor*factor*factor;
        float adjustYaw = Mouse.getDX()*factor;
        float adjustPitch = Mouse.getDY()*factor;
        if (mc.gameSettings.invertMouse) {
            adjustPitch = -adjustPitch;
        }
        mc.thePlayer.setAngles(adjustYaw, adjustPitch);
    }

    /** Hide the HUD */
    public void drawBottomOverlay() {
        final int top = height - BOTTOM_OVERLAY_HEIGHT;
        final Tessellator tess = Tessellator.instance;

        // Draw darkened background texture
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/gui/background.png"));
        final double txSize = 32.0;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        tess.startDrawingQuads();
        tess.setColorRGBA_I(0x404040, 255);
        tess.addVertexWithUV(  0.0, height, 0.0,    0.0/txSize, height/txSize);
        tess.addVertexWithUV(width, height, 0.0,  width/txSize, height/txSize);
        tess.addVertexWithUV(width,    top, 0.0,  width/txSize,    top/txSize);
        tess.addVertexWithUV(  0.0,    top, 0.0,    0.0/txSize,    top/txSize);
        tess.draw();

        // Draw gradient on top edge
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tess.startDrawingQuads();
        tess.setColorRGBA_I(0x000000, 255);
        tess.addVertexWithUV(  0.0, top, 0.0,   0.0, 1.0);
        tess.addVertexWithUV(width, top, 0.0,   1.0, 1.0);
        tess.setColorRGBA_I(0x000000, 0);
        tess.addVertexWithUV(width,     top - 4, 0.0,   1.0, 0.0);
        tess.addVertexWithUV(  0.0,     top - 4, 0.0,   0.0, 0.0);
        tess.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
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
