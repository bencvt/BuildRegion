package net.minecraft.src;

import net.minecraft.client.Minecraft;

import com.bencvt.minecraft.client.buildregion.Controller;
import com.bencvt.minecraft.client.buildregion.ui.InputManager;

/**
 * ModLoader front-end.
 * Passes everything along to the Controller and InputManager.
 * 
 * @author bencvt
 */
public class mod_BuildRegion extends BaseMod {
    private Minecraft minecraft;
    private Controller controller;
    private InputManager inputManager;
    private long lastShiftLeftClick;

    @Override
    public String getName() {
        return "BuildRegion";
    }

    @Override
    public String getVersion() {
        return "1.0 [1.3.2]";
    }

    @Override
    public void load() {
        minecraft = ModLoader.getMinecraftInstance();
        controller = new Controller(minecraft);
        inputManager = controller.getInputManager();
        inputManager.register(this);
        ModLoader.setInGameHook(this, true, false); // include partial ticks
    }

    @Override
    public boolean onTickInGame(float partialTickTime, Minecraft minecraft) {
        inputManager.checkForMouseEvent();
        controller.render();
        return true;
    }

    @Override
    public void keyboardEvent(KeyBinding key) {
        inputManager.handleKeyboardEvent(key);
    }
}
