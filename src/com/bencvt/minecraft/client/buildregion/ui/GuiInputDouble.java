package com.bencvt.minecraft.client.buildregion.ui;

import com.bencvt.minecraft.client.buildregion.ui.GuiEnumSelect.Option;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;

public class GuiInputDouble extends GuiLabeledControl {
    private int labelWidth;
    private int valueWidth;
    private double value;

    public GuiInputDouble(String labelText) {
        super(labelText);
    }

    @Override
    public void setLayout(FontRenderer fontRenderer, int labelWidth, int x, int y) {
        this.labelWidth = labelWidth;
        xPosition = x;
        yPosition = y;
        height = fontRenderer.FONT_HEIGHT;
        valueWidth = fontRenderer.getStringWidth("-99999.5");
        width = labelWidth + LABEL_SPACING + valueWidth;
    }

    public double getValue() {
        return value;
    }
    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        if (!drawButton) {
            return;
        }
        minecraft.fontRenderer.drawString(
                displayString,
                xPosition + labelWidth - minecraft.fontRenderer.getStringWidth(displayString),
                yPosition,
                LABEL_COLOR_ARGB);

        int x = xPosition + labelWidth + LABEL_SPACING;
        String valueString = Double.toString(getValue());
        minecraft.fontRenderer.drawString(
                valueString,
                x + valueWidth - minecraft.fontRenderer.getStringWidth(valueString),
                yPosition,
                CONTROL_COLOR_ARGB);
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        if (!super.mousePressed(minecraft, mouseX, mouseY)) {
            return false;
        }
        // TODO
        return true;
    }
}
