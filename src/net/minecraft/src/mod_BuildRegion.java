package net.minecraft.src;

import libshapedraw.LibShapeDraw;
import libshapedraw.event.LSDEventListener;
import libshapedraw.event.LSDGameTickEvent;
import libshapedraw.event.LSDPreRenderEvent;
import libshapedraw.event.LSDRespawnEvent;
import net.minecraft.client.Minecraft;

import com.bencvt.minecraft.buildregion.Controller;
import com.bencvt.minecraft.buildregion.UpdateCheck;
import com.bencvt.minecraft.buildregion.ui.InputManager;

/**
 * Front-end class that hooks into various APIs: LibShapeDraw, ModLoader, and
 * PlayerControllerHooks.
 * <p>
 * This class does the bare minimum of processing: it simply passes everything
 * along to the Controller, InputManager, and UpdateCheck wherever possible.
 * 
 * @author bencvt
 */
public class mod_BuildRegion extends BaseMod implements LSDEventListener {
    private Controller controller;
    private InputManager inputManager;
    private UpdateCheck updateCheck;

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
        controller = new Controller(this,
                new LibShapeDraw().addEventListener(this).verifyInitialized());
        inputManager = controller.getInputManager();

        ModLoader.setInGameHook(this, true, false); // include partial ticks
    }

    // ========
    // ModLoader events
    // ========

    @Override
    public boolean onTickInGame(float partialTickTime, Minecraft minecraft) {
        inputManager.handleInput(false);
        controller.renderHUD();
        return true;
    }

    @Override
    public void keyboardEvent(KeyBinding key) {
        inputManager.handleKeyboardEvent(key, false);
    }

    // ========
    // LibShapeDraw events
    // ========

    @Override
    public void onRespawn(LSDRespawnEvent event) {
        if (event.isNewServer()) {
            controller.cmdReset();
        } else {
            controller.cmdClear(false);
        }
        PlayerControllerHooks.installHooks();
        if (updateCheck == null) {
            updateCheck = new UpdateCheck(Controller.MOD_VERSION, controller.getModDirectory());
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
}
