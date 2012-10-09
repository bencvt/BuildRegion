package com.bencvt.minecraft.client.buildregion;

import libshapedraw.event.LSDEventListener;
import libshapedraw.event.LSDGameTickEvent;
import libshapedraw.event.LSDPreRenderEvent;
import libshapedraw.event.LSDRespawnEvent;
import net.minecraft.src.Entity;
import net.minecraft.src.PlayerControllerHooks.PlayerControllerEventListener;

/**
 * React to events generated by the LibShapeDraw and PlayerControllerHooks
 * APIs, passing them on to the Controller as needed.
 * 
 * @author bencvt
 */
public class Listener implements LSDEventListener, PlayerControllerEventListener {
    private final Controller controller;

    public Listener(Controller controller) {
        this.controller = controller;
    }

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

    @Override
    public boolean onBlockClick(boolean isLeftClick, int blockX, int blockY, int blockZ, int direction) {
        return controller.getInputManager().handleBlockClick(
                isLeftClick, blockX, blockY, blockZ, direction);
    }

    @Override
    public boolean onBlockDamage(int blockX, int blockY, int blockZ, int direction) {
        return controller.getInputManager().handleBlockClick(
                true, blockX, blockY, blockZ, direction);
    }

    @Override
    public boolean onEntityClick(boolean isLeftClick, Entity entity) {
        return !controller.getInputManager().shouldConsumeClick(isLeftClick);
    }
}
