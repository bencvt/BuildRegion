package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.client.Minecraft;

/**
 * Allow GUI screens to hide the chat window while the screen is open.
 * This can greatly reduce the amount of visual clutter while still allowing
 * the in-game world to be rendered in the background.
 * <p>
 * There is no method built-in to Minecraft's UI system to do this, so we do
 * some minor hackery here:
 * <p>
 * Temporarily change gameSettings.chatVisibility to hide the chat window,
 * restoring it when the screen calls our hide() method as it closes.
 * <p>
 * In case the screen doesn't call hide() for whatever reason, we also have
 * a thread running that checks whether the screen was closed.
 * 
 * @author bencvt
 */
public class ChatHider extends Thread {
    private static ChatHider thread;
    private static int origChatVisibility;
    private boolean killed;

    private ChatHider() {
        super("BuildRegion GUI chat hider");
    }

    @Override
    public void run() {
        while (!killed && Minecraft.getMinecraft().currentScreen != null) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                break;
            }
        }
        if (!killed) {
            show();
        }
    }

    public synchronized static void show() {
        if (thread != null) {
            Minecraft.getMinecraft().gameSettings.chatVisibility = origChatVisibility;
            thread.killed = true;
            thread = null;
        }
    }

    public synchronized static void hide() {
        if (thread != null) {
            return;
        }
        origChatVisibility = Minecraft.getMinecraft().gameSettings.chatVisibility;
        if (origChatVisibility == 2) {
            // already hidden by user preference
            return;
        }
        Minecraft.getMinecraft().gameSettings.chatVisibility = 2;
        thread = new ChatHider();
        thread.start();
    }
}
