package com.bencvt.minecraft.buildregion;

import java.io.File;

import libshapedraw.LibShapeDraw;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.src.mod_BuildRegion;

import com.bencvt.minecraft.buildregion.lang.LocalizedString;
import com.bencvt.minecraft.buildregion.region.Direction3D;
import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.region.RelativeDirection3D;
import com.bencvt.minecraft.buildregion.ui.GuiScreenDefineRegion;
import com.bencvt.minecraft.buildregion.ui.InputManager;
import com.bencvt.minecraft.buildregion.ui.MessageManager;
import com.bencvt.minecraft.buildregion.ui.ShapeManager;

/**
 * Primary class that ties everything together.
 * 
 * @author bencvt
 */
public class Controller {
    public static final String MOD_VERSION = "1.1.1-SNAPSHOT";
    public static final String MINECRAFT_VERSION = "1.4.2";

    private final Minecraft minecraft;
    private final InputManager inputManager;
    private final MessageManager messageManager;
    private final ShapeManager shapeManager;
    private final String modTitle;
    private final File modDirectory;
    private final BuildModeValue buildMode;
    private RegionBase curRegion;
    private RegionBase prevRegion; // will never be null

    public Controller(LibShapeDraw libShapeDraw, mod_BuildRegion mod, Minecraft minecraft) {
        this.minecraft = minecraft;
        inputManager = new InputManager(this, mod, minecraft);
        messageManager = new MessageManager(minecraft);
        shapeManager = new ShapeManager(this, libShapeDraw);
        modTitle = mod.getName() + " v" + MOD_VERSION;
        modDirectory = new File(Minecraft.getMinecraftDir(), "mods" + File.separator + mod.getName());
        buildMode = new BuildModeValue(BuildMode.INSIDE);
        cmdReset();
    }

    // ========
    // cmd methods to update state
    // ========

    public void cmdReset() {
        buildMode.setValueNoAnimation(BuildMode.INSIDE);
        curRegion = null;
        prevRegion = RegionBase.DEFAULT_REGION;
        shapeManager.reset();
    }

    public void cmdClear(boolean animate) {
        if (curRegion == null) {
            return;
        }
        prevRegion = curRegion;
        curRegion = null;
        shapeManager.updateRegion(curRegion, animate);
        messageManager.info(i18n("hud.cleared"));
    }

    public void cmdSet(RegionBase newRegion, boolean animate) {
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
    }

    public void cmdSetFacing(RelativeDirection3D relDir) {
        RegionBase protoRegion = getPrototypeRegion();
        Direction3D dir = getFacingDirection(relDir, false);
        if (dir == null) {
            return;
        }
        Vector3 origin = new Vector3(
                minecraft.thePlayer.posX,
                minecraft.thePlayer.posY,
                minecraft.thePlayer.posZ).floor();
        RegionBase newRegion = protoRegion.copyUsing(origin, dir.axis);
        // Move the origin so it's in front of the player.
        newRegion.addOriginCoord(dir.axis, dir.axisDirection * 2);

        cmdSet(newRegion, true);
    }

    public void cmdAdjustFacing(boolean expand, RelativeDirection3D relDir) {
        Direction3D dir = getFacingDirection(relDir, true);
        if (dir == null) {
            return;
        }
        if (curRegion == null || !curRegion.canAdjustAlongAxis(expand, dir.axis)) {
            cmdSetFacing(relDir);
            return;
        }

        // Update region.
        // TODO: lock radii for sphere/cylinder
        double amount = dir.axisDirection * curRegion.getUnits(dir.axis).atom;
        if (expand) {
            if (!curRegion.expand(dir.axis, amount)) {
                return;
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
    }

    public void cmdMode(BuildMode newMode) {
        buildMode.setValue(newMode);
        messageManager.info(i18n("hud.mode", newMode,
                inputManager.KEYBIND_MODE.getKeyName(true)));
    }

    public void cmdModeNext() {
        cmdMode(buildMode.getValue().getNextMode());
    }

    public void cmdOpenGui() {
        new GuiScreenDefineRegion(this).open();
    }

    // ========
    // Methods called from mod_BuildRegion, InputManager, and GuiBuildRegion
    // to react to game events
    // ========

    public void renderHUD() {
        messageManager.render();
    }

    public void updatePlayerPosition(ReadonlyVector3 playerCoords) {
        shapeManager.updateObserverPosition(playerCoords);
    }

    public void notifyDenyClick() {
        messageManager.info(i18n("hud.misclicked"));
    }

    public void toggleGui(boolean isGuiScreenActive) {
        shapeManager.setGuiScreenActive(isGuiScreenActive);
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

    public boolean canBuild(double x, double y, double z) {
        if (curRegion == null) {
            return true;
        } else if (buildMode.getValue() == BuildMode.INSIDE) {
            return curRegion.isInsideRegion(x, y, z);
        } else if (buildMode.getValue() == BuildMode.OUTSIDE) {
            return !curRegion.isInsideRegion(x, y, z);
        } else {
            return true;
        }
    }

    public boolean isRegionActive() {
        return curRegion != null;
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

    public String i18n(String key, Object ... args) {
        return LocalizedString.translate(key, args);
    }

    // ========
    // Internal helper methods
    // ========

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
}
