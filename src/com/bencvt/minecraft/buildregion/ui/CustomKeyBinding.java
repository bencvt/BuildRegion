package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.src.BaseMod;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.ModLoader;

import org.lwjgl.input.Keyboard;

import com.bencvt.minecraft.buildregion.lang.LocalizedString;

public class CustomKeyBinding extends KeyBinding {
    private final boolean allowRepeat;
    private boolean registered;

    public CustomKeyBinding(int defaultKeyCode, boolean allowRepeat, String bindingId, String description) {
        super(bindingId, defaultKeyCode);
        this.allowRepeat = allowRepeat;
        ModLoader.addLocalization(bindingId, description);
    }

    public void register(BaseMod mod) {
        if (!registered) {
            ModLoader.registerKey(mod, this, allowRepeat);
            registered = true;
        }
    }

    public String getKeyName() {
        if (keyCode < 0) {
            return "MOUSE";
        }
        String keyName = Keyboard.getKeyName(keyCode);
        if (keyName.equals("LBRACKET")) {
            return "[";
        } else if (keyName.equals("RBRACKET")) {
            return "]";
        } else {
            return keyName;
        }
    }

    public String getKeyName(boolean withShiftOrCtrl) {
        if (withShiftOrCtrl) {
            return "shift- " + LocalizedString.translate("or") + " ctrl-" + getKeyName();
        } else {
            return getKeyName();
        }
    }

    public String getKeyNameColored(boolean withShiftOrCtrl) {
        if (withShiftOrCtrl) {
            return getShiftOrCtrlColored() + getKeyName() + "\u00a7r";
        } else {
            return "\u00a7c" + getKeyName() + "\u00a7r";
        }
    }

    public static String getShiftOrCtrlColored() {
        return "\u00a7cshift-\u00a7r " + LocalizedString.translate("or") +" \u00a7cctrl-\u00a7c";
    }
}
