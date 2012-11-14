package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.client.Minecraft;

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
