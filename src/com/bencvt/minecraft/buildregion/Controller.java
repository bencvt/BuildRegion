package com.bencvt.minecraft.buildregion;

import java.io.File;

import libshapedraw.LibShapeDraw;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.src.mod_BuildRegion;

import com.bencvt.minecraft.buildregion.region.Direction3D;
import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.ui.InputManager;
import com.bencvt.minecraft.buildregion.ui.MessageManager;
import com.bencvt.minecraft.buildregion.ui.ShapeManager;

/**
 * Primary class that ties everything together.
 * 
 * @author bencvt
 */
public class Controller {
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
        modTitle = mod.getName() + " v" + mod.getModVersion();
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

    public void cmdClear() {
        if (curRegion == null) {
            return;
        }
        prevRegion = curRegion;
        curRegion = null;
        shapeManager.updateRegion(curRegion);
        messageManager.info("build region unlocked");
    }

    public void cmdSet(RegionBase newRegion) {
        if (newRegion == null) {
            cmdClear();
        }
        if (curRegion != null) {
            prevRegion = curRegion;
        }
        curRegion = newRegion;

        // Update UI.
        shapeManager.updateRegion(curRegion);
        messageManager.info("build region locked:\n" + curRegion);
    }

    public void cmdSetFacing() {
        RegionBase protoRegion = getPrototypeRegion();
        Direction3D dir = getFacingDirection(false);
        if (dir == null) {
            return;
        }
        Vector3 origin = new Vector3(
                minecraft.thePlayer.posX,
                minecraft.thePlayer.posY,
                minecraft.thePlayer.posZ).floor();
        RegionBase newRegion = protoRegion.copyUsing(origin, dir.axis);
        // Move the origin so it's in front of the player.
        newRegion.shiftOriginCoord(dir.axis, dir.axisDirection * 2);

        cmdSet(newRegion);
    }

    public void cmdShiftFacing(int amount) {
        Direction3D dir = getFacingDirection(true);
        if (dir == null) {
            return;
        }
        if (curRegion == null || !curRegion.canShiftAlongAxis(dir.axis)) {
            cmdSetFacing();
            return;
        }

        // Update region.
        curRegion.shiftOriginCoord(dir.axis, dir.axisDirection * amount);

        // Update UI.
        shapeManager.updateRegion(curRegion);
        messageManager.info("build region shifted:\n" + curRegion);
    }

    public void cmdMode(BuildMode newMode) {
        buildMode.setValue(newMode);
        messageManager.info("build region mode: " +
                newMode.toString().toLowerCase()); // TODO: "\npress shift-<bind> for advanced options"
    }

    public void cmdModeNext() {
        cmdMode(buildMode.getValue().getNextMode());
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
        messageManager.info("misclick blocked by build region");
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

    // ========
    // Internal helper methods
    // ========

    private Direction3D getFacingDirection(boolean unambiguously) {
        if (unambiguously) {
            Direction3D dir = Direction3D.fromYawPitchUnambiguously(
                    minecraft.thePlayer.rotationYaw,
                    minecraft.thePlayer.rotationPitch);
            if (dir == null) {
                messageManager.error("ambiguous direction\nface north, south, east, west, up, or down");
            }
            return dir;
        } else {
            return Direction3D.fromYawPitch(
                    minecraft.thePlayer.rotationYaw,
                    minecraft.thePlayer.rotationPitch);
        }
    }
}
