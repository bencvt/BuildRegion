package com.bencvt.minecraft.buildregion;

import java.io.File;

import libshapedraw.LibShapeDraw;
import libshapedraw.event.LSDEventListener;
import libshapedraw.event.LSDGameTickEvent;
import libshapedraw.event.LSDPreRenderEvent;
import libshapedraw.event.LSDRespawnEvent;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.src.PlayerControllerHooks;
import net.minecraft.src.mod_BuildRegion;

import com.bencvt.minecraft.buildregion.lang.LocalizedString;
import com.bencvt.minecraft.buildregion.region.Direction3D;
import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.region.RelativeDirection3D;
import com.bencvt.minecraft.buildregion.region.Units;
import com.bencvt.minecraft.buildregion.ui.InputManager;
import com.bencvt.minecraft.buildregion.ui.MessageManager;
import com.bencvt.minecraft.buildregion.ui.window.GuiScreenDefineRegion;
import com.bencvt.minecraft.buildregion.ui.world.ShapeManager;

/**
 * Primary class that ties everything together.
 * 
 * @author bencvt
 */
public class Controller implements LSDEventListener {
    public static final String MOD_VERSION = "2.0.2-SNAPSHOT";
    public static final String MINECRAFT_VERSION = "1.4.5";
    public static final String MIN_LIBSHAPEDRAW_VERSION = "1.3";

    private final Minecraft minecraft;
    private final InputManager inputManager;
    private final MessageManager messageManager;
    private final ShapeManager shapeManager;
    private final String modTitle;
    private final File modDirectory;
    private final BuildModeValue buildMode;
    private UpdateCheck updateCheck;
    private RegionBase curRegion;
    private RegionBase prevRegion; // will never be null

    public Controller(mod_BuildRegion mod) {
        minecraft = Minecraft.getMinecraft();
        inputManager = new InputManager(this, mod);
        messageManager = new MessageManager();
        shapeManager = new ShapeManager(this);
        modTitle = mod.getName() + " v" + MOD_VERSION;
        modDirectory = new File(Minecraft.getMinecraftDir(), "mods" + File.separator + mod.getName());
        buildMode = new BuildModeValue(BuildMode.INSIDE);
        cmdReset();

        // Register for PlayerControllerHooks events.
        new BlockClickHandler(this);

        // Register for LibShapeDraw events.
        if (!libshapedraw.ApiInfo.isVersionAtLeast(MIN_LIBSHAPEDRAW_VERSION)) {
            throw new RuntimeException(
                    modTitle + " requires LibShapeDraw v" +
                    MIN_LIBSHAPEDRAW_VERSION + " or greater.");
        }
        new LibShapeDraw().verifyInitialized().addEventListener(this);
    }

    // ========
    // cmd methods to update state
    // ========

    public boolean cmdReset() {
        buildMode.setValueNoAnimation(BuildMode.INSIDE);
        curRegion = null;
        prevRegion = RegionBase.DEFAULT_REGION;
        shapeManager.reset();
        return true;
    }

    public boolean cmdClear(boolean animate) {
        if (curRegion == null) {
            return false;
        }
        prevRegion = curRegion;
        curRegion = null;
        shapeManager.updateRegion(curRegion, animate);
        messageManager.info(i18n("hud.cleared"));
        return true;
    }

    public boolean cmdSet(RegionBase newRegion, boolean animate) {
        if (newRegion == null) {
            cmdClear(animate);
        }
        if (curRegion != null) {
            prevRegion = curRegion;
        }
        curRegion = newRegion;

        // Update UI.
        shapeManager.updateRegion(curRegion, animate);
        messageManager.info(i18n("hud.set", curRegion));
        // TODO: option to keep curRegion visible in a corner of the screen at all times as part of the HUD
        return true;
    }

    public boolean cmdSetFacing(RelativeDirection3D relDir) {
        RegionBase protoRegion = getPrototypeRegion();
        Direction3D dir = getFacingDirection(relDir, false);
        if (dir == null) {
            return false;
        }
        RegionBase newRegion = protoRegion.copyUsing(getBlockInFrontOfPlayerWork(dir), dir.axis);
        return cmdSet(newRegion, true);
    }

    public boolean cmdAdjustFacing(boolean expand, RelativeDirection3D relDir) {
        Direction3D dir = getFacingDirection(relDir, true);
        if (dir == null) {
            return false;
        }
        if (curRegion == null || !curRegion.canAdjustAlongAxis(expand, dir.axis)) {
            return cmdSetFacing(relDir);
        }

        // Update region.
        // TODO: lock radii for sphere/cylinder
        double amount = dir.axisDirection * curRegion.getUnits(dir.axis).atom;
        if (expand) {
            if (!curRegion.expand(dir.axis, amount)) {
                return false;
            }
        } else {
            curRegion.addOriginCoord(dir.axis, amount);
        }

        // Update UI.
        shapeManager.updateRegion(curRegion, true);
        if (expand) {
            String s = i18n("hud.resized." + (amount > 0.0 ? "expanded" : "contracted"));
            messageManager.info(i18n("hud.resized", s, curRegion));
        } else {
            messageManager.info(i18n("hud.moved", dir, curRegion));
        }
        return true;
    }

    public boolean cmdMode(BuildMode newMode) {
        buildMode.setValue(newMode);
        messageManager.info(i18n("hud.mode", newMode,
                inputManager.KEYBIND_MODE.getKeyName(true)));
        return true;
    }

    public boolean cmdModeNext() {
        return cmdMode(buildMode.getValue().getNextMode());
    }

    public boolean cmdOpenGui() {
        new GuiScreenDefineRegion(this).open();
        return true;
    }

    // ========
    // LibShapeDraw events
    // ========

    @Override
    public void onRespawn(LSDRespawnEvent event) {
        if (event.isNewServer()) {
            cmdReset();
        } else {
            cmdClear(false);
        }
        PlayerControllerHooks.installHooks();
        if (updateCheck == null) {
            updateCheck = new UpdateCheck(Controller.MOD_VERSION, getModDirectory());
        }
    }

    @Override
    public void onGameTick(LSDGameTickEvent event) {
        if (updateCheck != null && updateCheck.getResult() != null) {
            for (String line : updateCheck.getResult().split("\n")) {
                messageManager.chat(line);
            }
            updateCheck.setResult(null);
        }
    }

    @Override
    public void onPreRender(LSDPreRenderEvent event) {
        shapeManager.updateObserverPosition(event.getPlayerCoords());
    }

    // ========
    // Other methods to react to game events, called from mod_BuildRegion and
    // BlockClickHandler
    // ========

    public void onRenderTick() {
        inputManager.handleInput(false);
        messageManager.render();
    }

    public void notifyDenyClick() {
        messageManager.info(i18n("hud.misclicked"));
    }

    // ========
    // Misc accessors
    // ========

    public InputManager getInputManager() {
        return inputManager;
    }

    public String getModTitle() {
        return modTitle;
    }

    public File getModDirectory() {
        return modDirectory;
    }

    public ReadonlyBuildModeValue getBuildMode() {
        return buildMode;
    }

    public RegionBase getCurRegion() {
        return curRegion;
    }

    public RegionBase getPrototypeRegion() {
        if (curRegion != null) {
            return curRegion;
        }
        if (prevRegion != null) {
            return prevRegion;
        }
        throw new IllegalStateException("prevRegion should never be null");
    }

    public ReadonlyVector3 getBlockInFrontOfPlayer() {
        return getBlockInFrontOfPlayerWork(getFacingDirection(RelativeDirection3D.FORWARD, false));
    }

    // ========
    // Internal helper methods
    // ========

    private ReadonlyVector3 getBlockInFrontOfPlayerWork(Direction3D dir) {
        return Units.WHOLE.clamp(new Vector3(
                minecraft.thePlayer.posX,
                minecraft.thePlayer.posY,
                minecraft.thePlayer.posZ))
                .addComponent(dir.axis, dir.axisDirection*2.0);
    }

    private Direction3D getFacingDirection(RelativeDirection3D relDir, boolean unambiguously) {
        if (relDir == RelativeDirection3D.UP) {
            return Direction3D.UP;
        }
        if (relDir == RelativeDirection3D.DOWN) {
            return Direction3D.DOWN;
        }
        if (unambiguously) {
            Direction3D dir = Direction3D.fromYawPitchUnambiguously(
                    minecraft.thePlayer.rotationYaw,
                    minecraft.thePlayer.rotationPitch);
            if (dir == null) {
                messageManager.error(i18n("hud.ambiguous"));
                return null;
            }
            return dir.getRelative(relDir);
        } else {
            return Direction3D.fromYawPitch(
                    minecraft.thePlayer.rotationYaw,
                    minecraft.thePlayer.rotationPitch).getRelative(relDir);
        }
    }

    public static String i18n(String key, Object ... args) {
        return LocalizedString.translate(key, args);
    }
}
