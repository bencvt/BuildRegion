package com.bencvt.minecraft.buildregion;

import java.io.File;

import libshapedraw.LibShapeDraw;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.src.mod_BuildRegion;

import com.bencvt.minecraft.buildregion.region.Direction3D;
import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.region.RelativeDirection3D;
import com.bencvt.minecraft.buildregion.ui.GuiBuildRegion;
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

        cmdSet(newRegion);
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
        double amount = dir.axisDirection * curRegion.getUnits(dir.axis).atom;
        System.out.println("expand="+expand+" dir="+dir+" amount="+amount);//XXX
        if (expand) {
            if (!curRegion.expand(dir.axis, amount)) {
                return;
            }
        } else {
            curRegion.addOriginCoord(dir.axis, amount);
        }

        // Update UI.
        shapeManager.updateRegion(curRegion);
        if (expand) {
            messageManager.info("build region " +
                    (amount > 0.0 ? "expanded:\n" : "contracted:\n") +
                    curRegion);
        } else {
            messageManager.info("build region moved " +
                    dir.toString().toLowerCase() + ":\n" + curRegion);
        }
    }

    public void cmdMode(BuildMode newMode) {
        buildMode.setValue(newMode);
        messageManager.info("build region mode: " +
                newMode.toString().toLowerCase() +
                "\npress " + inputManager.getGuiKeybind() + " for more options");
    }

    public void cmdModeNext() {
        cmdMode(buildMode.getValue().getNextMode());
    }

    public void cmdOpenGui() {
        minecraft.displayGuiScreen(new GuiBuildRegion(this, minecraft.fontRenderer));
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
                messageManager.error("ambiguous direction\nface north, south, east, west, up, or down");
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
