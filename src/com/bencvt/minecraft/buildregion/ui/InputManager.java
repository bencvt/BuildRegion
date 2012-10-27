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
import com.bencvt.minecraft.buildregion.region.RelativeDirection3D;

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

    public static final String PROPNAME_SHIFT_UP = "buildregion.shift.up";
    public static final KeyBinding KEYBIND_SHIFT_UP = new KeyBinding(
            PROPNAME_SHIFT_UP,
            Keyboard.KEY_UP);

    public static final String PROPNAME_SHIFT_DOWN = "buildregion.shift.down";
    public static final KeyBinding KEYBIND_SHIFT_DOWN = new KeyBinding(
            PROPNAME_SHIFT_DOWN,
            Keyboard.KEY_DOWN);

    public static final String PROPNAME_SHIFT_LEFT = "buildregion.shift.left";
    public static final KeyBinding KEYBIND_SHIFT_LEFT = new KeyBinding(
            PROPNAME_SHIFT_LEFT,
            Keyboard.KEY_LEFT);

    public static final String PROPNAME_SHIFT_RIGHT = "buildregion.shift.right";
    public static final KeyBinding KEYBIND_SHIFT_RIGHT = new KeyBinding(
            PROPNAME_SHIFT_RIGHT,
            Keyboard.KEY_RIGHT);

    public static boolean IS_MAC = Minecraft.getOs() == EnumOS.MACOS;
    // Control-left-click on Mac OS X is right-click, so use command instead.
    // The fancy command symbol is "\u2318" but it looks weird (undersized) in
    // Minecraft's default font.
    public static final String MOUSE_MOD_KEY_NAME = IS_MAC ? "cmd-" : "ctrl-";
    public static final int MOUSE_MOD_KEY_L = IS_MAC ? Keyboard.KEY_LMETA : Keyboard.KEY_LCONTROL;
    public static final int MOUSE_MOD_KEY_R = IS_MAC ? Keyboard.KEY_RMETA : Keyboard.KEY_RCONTROL;

    public static final boolean MOUSE_MOD_LEFT_CLICK_CLEARS = true;
    public static final boolean MOUSE_MOD_RIGHT_CLICK_SETS = true;
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
        ModLoader.registerKey(mod, KEYBIND_SHIFT_UP, false);
        ModLoader.registerKey(mod, KEYBIND_SHIFT_DOWN, false);
        ModLoader.registerKey(mod, KEYBIND_SHIFT_LEFT, false);
        ModLoader.registerKey(mod, KEYBIND_SHIFT_RIGHT, false);

        ModLoader.addLocalization(PROPNAME_MODE,        "BuildRegion mode");
        ModLoader.addLocalization(PROPNAME_SHIFT_BACK,  "BuildRegion shift back");
        ModLoader.addLocalization(PROPNAME_SHIFT_FWD,   "BuildRegion shift fwd");
        ModLoader.addLocalization(PROPNAME_SHIFT_UP,    "BuildRegion shift up");
        ModLoader.addLocalization(PROPNAME_SHIFT_DOWN,  "BuildRegion shift down");
        ModLoader.addLocalization(PROPNAME_SHIFT_LEFT,  "BuildRegion shift left");
        ModLoader.addLocalization(PROPNAME_SHIFT_RIGHT, "BuildRegion shift right");
    }

    public void handleKeyboardEvent(KeyBinding key) {
        if (minecraft.currentScreen != null || minecraft.thePlayer == null) {
            return;
        }
        if (key == KEYBIND_MODE) {
            if (isShiftOrControlKeyDown()) {
                minecraft.displayGuiScreen(new GuiBuildRegion(controller, minecraft.fontRenderer));
            } else {
                controller.cmdModeNext();
            }
        } else if (key == KEYBIND_SHIFT_BACK) {
            controller.cmdAdjustFacing(isShiftOrControlKeyDown(), RelativeDirection3D.BACK);
        } else if (key == KEYBIND_SHIFT_FWD) {
            controller.cmdAdjustFacing(isShiftOrControlKeyDown(), RelativeDirection3D.FORWARD);
        } else if (key == KEYBIND_SHIFT_UP) {
            controller.cmdAdjustFacing(isShiftOrControlKeyDown(), RelativeDirection3D.UP);
        } else if (key == KEYBIND_SHIFT_DOWN) {
            controller.cmdAdjustFacing(isShiftOrControlKeyDown(), RelativeDirection3D.DOWN);
        } else if (key == KEYBIND_SHIFT_LEFT) {
            controller.cmdAdjustFacing(isShiftOrControlKeyDown(), RelativeDirection3D.LEFT);
        } else if (key == KEYBIND_SHIFT_RIGHT) {
            controller.cmdAdjustFacing(isShiftOrControlKeyDown(), RelativeDirection3D.RIGHT);
        }
    }

    public void checkForMouseEvent() {
        if (MOUSE_MOD_LEFT_CLICK_CLEARS && Mouse.isButtonDown(0) && isMouseModKeyDown()) {
            long now = System.currentTimeMillis();
            if (now > lastMouseEvent + MOUSE_EVENT_INTERVAL) {
                lastMouseEvent = now;
                controller.cmdClear();
            }
        } else if (MOUSE_MOD_RIGHT_CLICK_SETS && Mouse.isButtonDown(1) && isMouseModKeyDown()) {
            long now = System.currentTimeMillis();
            if (now > lastMouseEvent + MOUSE_EVENT_INTERVAL) {
                lastMouseEvent = now;
                controller.cmdSetFacing(RelativeDirection3D.FORWARD);
            }
        }
        // TODO: mod+mousewheel to shift region forward/back
    }

    private boolean isMouseModKeyDown() {
        return Keyboard.isKeyDown(MOUSE_MOD_KEY_L) || Keyboard.isKeyDown(MOUSE_MOD_KEY_R);
    }

    private boolean isShiftOrControlKeyDown() {
        return (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ||
                Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ||
                Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ||
                Keyboard.isKeyDown(Keyboard.KEY_RCONTROL));
    }

    public boolean shouldConsumeClick(boolean isLeftClick) {
        if (isLeftClick && MOUSE_MOD_LEFT_CLICK_CLEARS && isMouseModKeyDown()) {
            return true;
        }
        if (!isLeftClick && MOUSE_MOD_RIGHT_CLICK_SETS && isMouseModKeyDown()) {
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

    public String getUserBinding(String propName) {
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
        final String shiftOrCtrl = "shift-\u00a7r or \u00a7cctrl-";
        final String sep = "\u00a7r and \u00a7c";
        final String mid = "\u00a7r \u2014 ";
        final String rarr = "\u27f6";
        final String keyMode  = getUserBinding(PROPNAME_MODE);
        final String keyBack  = getUserBinding(PROPNAME_SHIFT_BACK);
        final String keyFwd   = getUserBinding(PROPNAME_SHIFT_FWD);
        final String keyUp    = getUserBinding(PROPNAME_SHIFT_UP);
        final String keyDown  = getUserBinding(PROPNAME_SHIFT_DOWN);
        final String keyLeft  = getUserBinding(PROPNAME_SHIFT_LEFT);
        final String keyRight = getUserBinding(PROPNAME_SHIFT_RIGHT);

        // TODO: probably better to make a GuiUsageScreen for this instead
        chat.printChatMessage(pre + MOUSE_MOD_KEY_NAME + "left-click" + mid + "clear");
        chat.printChatMessage(pre + MOUSE_MOD_KEY_NAME + "right-click" + mid + "set");
        chat.printChatMessage(pre + keyMode + mid + "toggle mode");
        chat.printChatMessage(pre + shiftOrCtrl + keyMode + mid + "open gui");
        chat.printChatMessage(pre + keyBack + sep + keyFwd   + mid + "move region back/forward");
        chat.printChatMessage(pre + keyUp   + sep + keyDown  + mid + "move region up/down");
        chat.printChatMessage(pre + keyLeft + sep + keyRight + mid + "move region left/right");
        chat.printChatMessage(pre + shiftOrCtrl + "\u00a7rmovement key" + mid + "expand region");
        chat.printChatMessage("To adjust key bindings: Esc " + rarr +
                " Options... " + rarr + " Controls...");
    }

    public String getGuiKeybind() {
        return "shift- or ctrl-" + getUserBinding(PROPNAME_MODE);
    }
}
