package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BaseMod;
import net.minecraft.src.EnumOS;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.PlayerControllerHooks;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.bencvt.minecraft.buildregion.Controller;
import com.bencvt.minecraft.buildregion.lang.LocalizedString;
import com.bencvt.minecraft.buildregion.region.Direction3D;
import com.bencvt.minecraft.buildregion.region.RelativeDirection3D;

/**
 * Handle user input (keyboard/mouse events).
 * 
 * @author bencvt
 */
public class InputManager {
    public static final CustomKeyBinding KEYBIND_MODE = new CustomKeyBinding(
            Keyboard.KEY_B, false,
            "buildregion.mode",
            "BuildRegion mode");
    public static final CustomKeyBinding KEYBIND_SHIFT_BACK = new CustomKeyBinding(
            Keyboard.KEY_LBRACKET, true,
            "buildregion.shift.back",
            "BuildRegion shift ");
    public static final CustomKeyBinding KEYBIND_SHIFT_FWD = new CustomKeyBinding(
            Keyboard.KEY_RBRACKET, true,
            "buildregion.shift.fwd",
            "BuildRegion shift fwd");
    public static final CustomKeyBinding KEYBIND_SHIFT_UP = new CustomKeyBinding(
            Keyboard.KEY_UP, true,
            "buildregion.shift.up",
            "BuildRegion shift up");
    public static final CustomKeyBinding KEYBIND_SHIFT_DOWN = new CustomKeyBinding(
            Keyboard.KEY_DOWN, true,
            "buildregion.shift.down",
            "BuildRegion shift down");
    public static final CustomKeyBinding KEYBIND_SHIFT_LEFT = new CustomKeyBinding(
            Keyboard.KEY_LEFT, true,
            "buildregion.shift.left",
            "BuildRegion shift left");
    public static final CustomKeyBinding KEYBIND_SHIFT_RIGHT = new CustomKeyBinding(
            Keyboard.KEY_RIGHT, true,
            "buildregion.shift.right",
            "BuildRegion shift right");

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
        KEYBIND_MODE.register(mod);
        KEYBIND_SHIFT_BACK.register(mod);
        KEYBIND_SHIFT_FWD.register(mod);
        KEYBIND_SHIFT_UP.register(mod);
        KEYBIND_SHIFT_DOWN.register(mod);
        KEYBIND_SHIFT_LEFT.register(mod);
        KEYBIND_SHIFT_RIGHT.register(mod);
    }

    public void handleKeyboardEvent(KeyBinding key) {
        if (minecraft.currentScreen != null || minecraft.thePlayer == null) {
            return;
        }
        if (key == KEYBIND_MODE) {
            if (isShiftOrControlKeyDown() || !controller.isRegionActive()) {
                controller.cmdOpenGui();
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
                controller.cmdClear(true);
            }
        } else if (MOUSE_MOD_RIGHT_CLICK_SETS && Mouse.isButtonDown(1) && isMouseModKeyDown()) {
            long now = System.currentTimeMillis();
            if (now > lastMouseEvent + MOUSE_EVENT_INTERVAL) {
                lastMouseEvent = now;
                controller.cmdSetFacing(RelativeDirection3D.FORWARD);
            }
        }
        // TODO: mod+mousewheel to shift region forward/back
        // TODO: mod+middle button to bring up gui
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

    public String getUsage(String indent) {
        StringBuilder b = new StringBuilder();

        b.append(indent)
        .append("\u00a7c").append(MOUSE_MOD_KEY_NAME).append(i18n("input.rightclick"))
        .append("\u00a7r\t").append(i18n("usage.set")).append('\n');

        b.append(indent)
        .append("\u00a7c").append(MOUSE_MOD_KEY_NAME).append(i18n("input.leftclick"))
        .append("\u00a7r\t").append(i18n("usage.clear")).append('\n');

        b.append(indent)
        .append(KEYBIND_SHIFT_BACK.getKeyNameColored(false))
        .append(' ').append(i18n("and")).append(' ')
        .append(KEYBIND_SHIFT_FWD.getKeyNameColored(false))
        .append('\t').append(i18n("usage.move.backfwd")).append('\n');

        b.append(indent)
        .append(KEYBIND_SHIFT_UP.getKeyNameColored(false))
        .append(' ').append(i18n("and")).append(' ')
        .append(KEYBIND_SHIFT_DOWN.getKeyNameColored(false))
        .append('\t').append(i18n("usage.move.updown")).append('\n');

        b.append(indent)
        .append(KEYBIND_SHIFT_LEFT.getKeyNameColored(false))
        .append(' ').append(i18n("and")).append(' ')
        .append(KEYBIND_SHIFT_RIGHT.getKeyNameColored(false))
        .append('\t').append(i18n("usage.move.leftright")).append('\n');

        b.append(indent)
        .append(CustomKeyBinding.getShiftOrCtrlColored()).append(i18n("input.movementkey"))
        .append("\u00a7r\t").append(i18n("usage.resize")).append('\n');

        b.append(indent)
        .append(KEYBIND_MODE.getKeyNameColored(false))
        .append('\t').append(i18n("usage.mode")).append('\n');

        b.append(indent)
        .append(KEYBIND_MODE.getKeyNameColored(true))
        .append('\t').append(i18n("usage.gui")).append('\n');

        return b.toString();
    }

    private static String i18n(String key) {
        return LocalizedString.translate(key);
    }
}
