package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BaseMod;
import net.minecraft.src.EnumOS;
import net.minecraft.src.GuiNewChat;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.ModLoader;
import net.minecraft.src.PlayerControllerHooks;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.bencvt.minecraft.buildregion.Controller;
import com.bencvt.minecraft.buildregion.region.Direction3D;

/**
 * Handle user input (keyboard/mouse events).
 * 
 * @author bencvt
 */
public class InputManager {
    public static final String PROPNAME_MODE = "buildregion.mode";
    public static final KeyBinding KEYBIND_MODE = new KeyBinding(
            PROPNAME_MODE,
            Keyboard.KEY_B);

    public static final String PROPNAME_SHIFT_BACK = "buildregion.shift.back";
    public static final KeyBinding KEYBIND_SHIFT_BACK = new KeyBinding(
            PROPNAME_SHIFT_BACK,
            Keyboard.KEY_LBRACKET);

    public static final String PROPNAME_SHIFT_FWD = "buildregion.shift.fwd";
    public static final KeyBinding KEYBIND_SHIFT_FWD = new KeyBinding(
            PROPNAME_SHIFT_FWD,
            Keyboard.KEY_RBRACKET);

    public static boolean IS_MAC = Minecraft.getOs() == EnumOS.MACOS;
    // Control-left-click on Mac OS X is right-click, so use command instead.
    // The fancy command symbol is "\u2318" but it looks weird (undersized) in
    // Minecraft's default font.
    public static final String MOD_KEY_NAME = IS_MAC ? "cmd-" : "ctrl-";
    public static final int MOD_KEY_L = IS_MAC ? Keyboard.KEY_LMETA : Keyboard.KEY_LCONTROL;
    public static final int MOD_KEY_R = IS_MAC ? Keyboard.KEY_RMETA : Keyboard.KEY_RCONTROL;

    public static final boolean MOD_LEFT_CLICK_CLEARS = true;
    public static final boolean MOD_RIGHT_CLICK_SETS = true;
    public static final long MOUSE_EVENT_INTERVAL = 200;

    private final Controller controller;
    private final Minecraft minecraft;
    private long lastMouseEvent;

    public InputManager(Controller controller, BaseMod mod, Minecraft minecraft) {
        this.controller = controller;
        this.minecraft = minecraft;

        ModLoader.registerKey(mod, KEYBIND_MODE, false);
        ModLoader.registerKey(mod, KEYBIND_SHIFT_BACK, false);
        ModLoader.registerKey(mod, KEYBIND_SHIFT_FWD, false);

        ModLoader.addLocalization(PROPNAME_MODE, "BuildRegion mode");
        ModLoader.addLocalization(PROPNAME_SHIFT_BACK, "BuildRegion shift back");
        ModLoader.addLocalization(PROPNAME_SHIFT_FWD, "BuildRegion shift fwd");
    }

    public void handleKeyboardEvent(KeyBinding key) {
        if (minecraft.currentScreen != null || minecraft.thePlayer == null) {
            return;
        }
        if (key == KEYBIND_MODE) {
            if (isShiftKeyDown()) {
                controller.cmdModeNext();
            } else {
                //showUsage(); // TODO: move to gui
                minecraft.displayGuiScreen(new GuiBuildRegion(controller, minecraft.fontRenderer));
            }
        } else if (key == KEYBIND_SHIFT_BACK) {
            if (isShiftKeyDown()) {
                controller.cmdClear();
            } else {
                controller.cmdShiftFacing(-1);
            }
        } else if (key == KEYBIND_SHIFT_FWD) {
            if (isShiftKeyDown()) {
                controller.cmdSetFacing();
            } else {
                controller.cmdShiftFacing(1);
            }
        }
    }

    public void checkForMouseEvent() {
        if (MOD_LEFT_CLICK_CLEARS && Mouse.isButtonDown(0) && isModKeyDown()) {
            long now = System.currentTimeMillis();
            if (now > lastMouseEvent + MOUSE_EVENT_INTERVAL) {
                lastMouseEvent = now;
                controller.cmdClear();
            }
        } else if (MOD_RIGHT_CLICK_SETS && Mouse.isButtonDown(1) && isModKeyDown()) {
            long now = System.currentTimeMillis();
            if (now > lastMouseEvent + MOUSE_EVENT_INTERVAL) {
                lastMouseEvent = now;
                controller.cmdSetFacing();
            }
        }
        // TODO: mod+mousewheel to shift region
    }

    private boolean isModKeyDown() {
        return Keyboard.isKeyDown(MOD_KEY_L) || Keyboard.isKeyDown(MOD_KEY_R);
    }

    private boolean isShiftKeyDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    public boolean shouldConsumeClick(boolean isLeftClick) {
        if (isLeftClick && MOD_LEFT_CLICK_CLEARS && isModKeyDown()) {
            return true;
        }
        if (!isLeftClick && MOD_RIGHT_CLICK_SETS && isModKeyDown()) {
            return true;
        }
        return false;
    }

    public boolean handleBlockClick(boolean isLeftClick, int blockX, int blockY, int blockZ, int direction) {
        if (shouldConsumeClick(isLeftClick)) {
            return false;
        }
        if (!isLeftClick &&
                !PlayerControllerHooks.isBuildReplaceBlock(blockX, blockY, blockZ, direction)) {
            Direction3D dir = Direction3D.fromValue(direction);
            blockX = dir.getNeighborX(blockX);
            blockY = dir.getNeighborY(blockY);
            blockZ = dir.getNeighborZ(blockZ);
        }
        boolean allow = controller.canBuild(blockX, blockY, blockZ);
        if (!allow) {
            controller.notifyDenyClick();
        }
        return allow;
    }

    private String getUserBinding(String propName) {
        for (KeyBinding kb : minecraft.gameSettings.keyBindings) {
            if (kb.keyDescription.equals(propName)) {
                if (kb.keyCode < 0) {
                    return "MOUSE";
                }
                String keyName = Keyboard.getKeyName(kb.keyCode);
                if (keyName.equals("LBRACKET")) {
                    keyName = "[";
                } else if (keyName.equals("RBRACKET")) {
                    keyName = "]";
                }
                return keyName;
            }
        }
        return "UNKNOWN";
    }

    public void showUsage() {
        final GuiNewChat chat = minecraft.ingameGUI.getChatGUI();
        chat.printChatMessage(controller.getModTitle() + " usage:");

        final String pre = "  \u00a7c";
        final String mid = "\u00a7r \u2014 ";
        final String rarr = "\u27f6";
        final String keyMode = getUserBinding(PROPNAME_MODE);
        final String keyBack = getUserBinding(PROPNAME_SHIFT_BACK);
        final String keyFwd = getUserBinding(PROPNAME_SHIFT_FWD);

        chat.printChatMessage(pre + MOD_KEY_NAME + "left-click" +
                "\u00a7r or \u00a7cshift-" + keyBack + mid + "clear");
        chat.printChatMessage(pre + MOD_KEY_NAME + "right-click" +
                "\u00a7r or \u00a7cshift-" + keyFwd + mid + "set");
        chat.printChatMessage(pre + keyMode + mid + "open gui");
        chat.printChatMessage(pre + "shift-" + keyMode + mid + "toggle mode");
        chat.printChatMessage(pre + keyBack + mid + "shift back");
        chat.printChatMessage(pre + keyFwd + mid + "shift fwd");
        chat.printChatMessage("To adjust key binding: Esc " + rarr +
                " Options... " + rarr + " Controls...");
    }
}
