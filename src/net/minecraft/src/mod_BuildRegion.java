package net.minecraft.src;

import net.minecraft.client.Minecraft;

import com.bencvt.minecraft.buildregion.Controller;

/**
 * Front-end class, does the bare minimum of processing. Simply instantiates
 * the Controller and passes ModLoader events to it.
 * 
 * @author bencvt
 */
public class mod_BuildRegion extends BaseMod {
    private Controller controller;

    @Override
    public String getName() {
        return "BuildRegion";
    }

    @Override
    public String getVersion() {
        return Controller.MOD_VERSION + " [" + Controller.MINECRAFT_VERSION + "]";
    }

    @Override
    public void load() {
        controller = new Controller(this);
        ModLoader.setInGameHook(this, true, false); // include partial ticks
    }

    @Override
    public boolean onTickInGame(float partialTickTime, Minecraft minecraft) {
        controller.onRenderTick();
        return true;
    }

    @Override
    public void keyboardEvent(KeyBinding key) {
        controller.getInputManager().handleKeyboardEvent(key, false);
    }
}
