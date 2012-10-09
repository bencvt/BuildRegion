package net.minecraft.src;

import libshapedraw.LibShapeDraw;
import libshapedraw.event.LSDEventListener;
import libshapedraw.event.LSDGameTickEvent;
import libshapedraw.event.LSDPreRenderEvent;
import libshapedraw.event.LSDRespawnEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.src.PlayerControllerHooks.PlayerControllerEventListener;

import com.bencvt.minecraft.client.buildregion.Controller;
import com.bencvt.minecraft.client.buildregion.ui.InputManager;

/**
 * Front-end class that handles events raised by various APIs:
 * LibShapeDraw, ModLoader, and PlayerControllerHooks.
 * <p>
 * Simply passes everything along to the Controller and InputManager.
 * 
 * @author bencvt
 */
public class mod_BuildRegion extends BaseMod implements LSDEventListener, PlayerControllerEventListener {
    private Controller controller;
    private InputManager inputManager;

    @Override
    public String getName() {
        return "BuildRegion";
    }

    @Override
    public String getVersion() {
        return "1.0.3 [1.3.2]";
    }

    @Override
    public void load() {
        controller = new Controller(
                new LibShapeDraw().addEventListener(this),
                this,
                ModLoader.getMinecraftInstance());
        inputManager = controller.getInputManager();

        ModLoader.setInGameHook(this, true, false); // include partial ticks

        PlayerControllerHooks.register(this);
    }

    // ========
    // ModLoader events
    // ========

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

    // ========
    // LibShapeDraw events
    // ========

    @Override
    public void onRespawn(LSDRespawnEvent event) {
        controller.cmdClear(event.isNewServer());
    }

    @Override
    public void onGameTick(LSDGameTickEvent event) {
        // do nothing
    }

    @Override
    public void onPreRender(LSDPreRenderEvent event) {
        controller.updatePlayerPosition(event.getPlayerCoords());
    }

    // ========
    // PlayerControllerHooks events
    // ========

    @Override
    public boolean onBlockClick(boolean isLeftClick, int blockX, int blockY, int blockZ, int direction) {
        return inputManager.handleBlockClick(
                isLeftClick, blockX, blockY, blockZ, direction);
    }

    @Override
    public boolean onBlockDamage(int blockX, int blockY, int blockZ, int direction) {
        return inputManager.handleBlockClick(
                true, blockX, blockY, blockZ, direction);
    }

    @Override
    public boolean onEntityClick(boolean isLeftClick, Entity entity) {
        return !inputManager.shouldConsumeClick(isLeftClick);
    }
}
