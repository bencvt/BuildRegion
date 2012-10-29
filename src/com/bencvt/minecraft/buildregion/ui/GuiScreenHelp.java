package com.bencvt.minecraft.buildregion.ui;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import libshapedraw.primitive.Color;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiSlot;
import net.minecraft.src.Tessellator;

import com.bencvt.minecraft.buildregion.Controller;

/**
 * The help screen, which is just a scrollable text area.
 * 
 * @author bencvt
 */
public class GuiScreenHelp extends GuiScreenBase {
    public final static int MARGIN = 48;
    public final static int TEXT_ARGB = Color.WHITE.getARGB();
    public final static URI WEBSITE_URL = URI.create("http://bit.ly/BuildRegion");

    private final GuiStandardButton buttonDone = new GuiStandardButton(this, i18n("button.done"));
    private final ArrayList<String> lines = new ArrayList<String>();
    private final HashMap<Integer, String> columnLines = new HashMap<Integer, String>();
    private final Controller controller;
    private int urlLineNum;
    private int firstColumnWidth;
    private GuiSlot contents;

    public GuiScreenHelp(GuiScreenBase parentScreen, Controller controller) {
        super(parentScreen);
        this.controller = controller;
    }

    private void getLines(Controller controller) {
        lines.clear();
        lines.addAll(fontRenderer.listFormattedStringToWidth(i18n("help.about"), width - MARGIN*2));
        lines.add("");
        lines.add(i18n("help.author") + " bencvt");
        urlLineNum = lines.size();
        lines.add(i18n("help.url") + " \u00a79\u00a7n" + WEBSITE_URL);
        lines.add(i18n("help.usage"));
        lines.addAll(Arrays.asList(controller.getInputManager().getUsage("  ").split("\n")));
        lines.add("");

        columnLines.clear();
        firstColumnWidth = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int t = line.indexOf('\t');
            if (t >= 0) {
                String firstColumn = line.substring(0, t);
                firstColumnWidth = Math.max(firstColumnWidth, fontRenderer.getStringWidth(firstColumn));
                lines.set(i, firstColumn);
                columnLines.put(i, line.substring(t + 1));
            }
        }
    }

    @Override
    public void initGui() {
        getLines(controller);

        buttonDone.setWidth(200).setPositionXY((width - buttonDone.getWidth())/2, height - 30);
        controlList.add(buttonDone);

        final GuiScreenHelp _this = this;
        contents = new GuiSlot(mc, width, height, 16, height - 28, fontRenderer.FONT_HEIGHT + 1) {
            private boolean openedUrl;

            @Override
            protected int getSize() {
                return lines.size();
            }

            @Override
            protected void elementClicked(int slotNum, boolean doubleClick) {
                if (slotNum == urlLineNum && !openedUrl) {
                    try {
                        Desktop.getDesktop().browse(WEBSITE_URL);
                    } catch (Throwable t) {
                        // do nothing
                    }
                    openedUrl = true;
                }
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
            protected void drawSlot(int slotNum, int xPos, int yPos, int slotContentHeight, Tessellator tess) {
                getFontRenderer().drawString(lines.get(slotNum), MARGIN, yPos, TEXT_ARGB);
                if (columnLines.containsKey(slotNum)) {
                    getFontRenderer().drawString(columnLines.get(slotNum),
                            firstColumnWidth + MARGIN + 8, yPos, TEXT_ARGB);
                }
            }

            @Override
            protected void drawContainerBackground(Tessellator tess) {
                // do nothing
            }

            @Override
            protected int func_77225_g() {
                // should be deobfuscated as something like getScrollbarXPos
                return width - 10;
            }
        };
    }

    @Override
    public void drawScreen(int xMouse, int yMouse, float partialTick) {
        drawDefaultBackground();
        contents.drawScreen(xMouse, yMouse, partialTick);
        drawCenteredString(fontRenderer, controller.getModTitle(), width / 2, 4, TEXT_ARGB);
        super.drawScreen(xMouse, yMouse, partialTick);
    }

    @Override
    protected void onControlClick(GuiButton guiButton) {
        if (guiButton == buttonDone) {
            close();
        }
    }
}
