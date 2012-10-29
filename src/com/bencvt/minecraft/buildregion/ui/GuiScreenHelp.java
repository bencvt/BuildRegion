package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.primitive.Color;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiSlot;
import net.minecraft.src.Tessellator;

import com.bencvt.minecraft.buildregion.Controller;

/**
 * TODO: show blurb, usage, credits. Buttons to open URL and to close
 *  
 * @author bencvt
 */
public class GuiScreenHelp extends GuiScreenBase {
    public final String title;
    public final String[] lines;
    public final int maxLineWidth;
    private final GuiStandardButton buttonDone = new GuiStandardButton(this, "Done");
    private GuiSlot contents;

    public GuiScreenHelp(GuiScreenBase parentScreen, Controller controller) {
        super(parentScreen);
        title = controller.getModTitle();
        lines = controller.getInputManager().getUsage().split("\n");
        int w = 0;
        for (String line : lines) {
            w = Math.max(w, fontRenderer.getStringWidth(line));
        }
        maxLineWidth = w;
    }

    @Override
    public void initGui() {
        buttonDone.setWidth(150).setPositionXY(width/2 + 2, height - 30);
        controlList.add(buttonDone);

        final int xOff = (width - maxLineWidth)/2;
        contents = new GuiSlot(mc, width, height, 16, height - 28, fontRenderer.FONT_HEIGHT + 1) {
            @Override
            protected int getSize() {
                return lines.length;
            }
            @Override
            protected void elementClicked(int slotNum, boolean doubleClick) {
                // do nothing
            }
            @Override
            protected boolean isSelected(int slotNum) {
                return false;
            }
            @Override
            protected void drawBackground() {
                // do nothing
            }
            @Override
            protected void drawSlot(int slotNum, int var2, int yPos, int var4, Tessellator tess) {
                getFontRenderer().drawString(lines[slotNum], xOff, yPos, Color.WHITE.getARGB());
            }
            @Override
            protected void drawContainerBackground(Tessellator tess) {
                // do nothing
            }
        };
    }

    @Override
    public void drawScreen(int xMouse, int yMouse, float partialTick) {
        this.drawDefaultBackground();
        contents.drawScreen(xMouse, yMouse, partialTick);
        drawCenteredString(fontRenderer, title, width / 2, 4, 0xffffffff);
        super.drawScreen(xMouse, yMouse, partialTick);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) {
        if (guiButton == buttonDone) {
            close();
        }
    }
}
