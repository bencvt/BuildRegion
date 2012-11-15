package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import net.minecraft.client.Minecraft;

/**
 * Handle sending text messages to the user.
 * 
 * @author bencvt
 */
public class MessageManager {
    public static final ReadonlyColor MESSAGE_COLOR_INFO = Color.WHITE;
    public static final ReadonlyColor MESSAGE_COLOR_ERROR = new Color(0xff6060ff);
    public static final long MESSAGE_DURATION_INFO = 1500;
    public static final long MESSAGE_DURATION_ERROR = 3000;

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final HUDMessage hudMessage = new HUDMessage();

    public void info(String message) {
        hudMessage.update(message, false, MESSAGE_COLOR_INFO, MESSAGE_COLOR_INFO, MESSAGE_DURATION_INFO);
    }

    public void error(String message) {
        hudMessage.update(message, true, MESSAGE_COLOR_ERROR, MESSAGE_COLOR_ERROR, MESSAGE_DURATION_ERROR);
    }

    public void chat(String message) {
        minecraft.ingameGUI.getChatGUI().printChatMessage(message);
    }

    public void render() {
        hudMessage.render();
    }
}
