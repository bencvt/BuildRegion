package net.minecraft.src;

import libshapedraw.LibShapeDraw;
import libshapedraw.event.LSDEventListener;
import libshapedraw.event.LSDGameTickEvent;
import libshapedraw.event.LSDPreRenderEvent;
import libshapedraw.event.LSDRespawnEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.src.PlayerControllerHooks.PlayerControllerEventListener;

import com.bencvt.minecraft.buildregion.Controller;
import com.bencvt.minecraft.buildregion.UpdateCheck;
import com.bencvt.minecraft.buildregion.ui.InputManager;

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
    private UpdateCheck updateCheck;

    @Override
    public String getName() {
        return "BuildRegion";
    }

    public String getModVersion() {
        return "1.1.1";
    }

    @Override
    public String getVersion() {
        return getModVersion() + " [1.4.2]";
    }

    @Override
    public void load() {
        controller = new Controller(
                new LibShapeDraw().addEventListener(this).verifyInitialized(),
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
        controller.renderHUD();
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
        if (event.isNewServer()) {
            controller.cmdReset();
        } else {
            controller.cmdClear();
        }
        PlayerControllerHooks.installHooks();
        if (updateCheck == null) {
            updateCheck = new UpdateCheck(getModVersion(), controller.getModDirectory());
        }
    }

    @Override
    public void onGameTick(LSDGameTickEvent event) {
        if (updateCheck != null && updateCheck.getResult() != null) {
            GuiNewChat chat = Minecraft.getMinecraft().ingameGUI.getChatGUI();
            for (String line : updateCheck.getResult().split("\n")) {
                chat.printChatMessage(line);
            }
            updateCheck.setResult(null);
        }
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
