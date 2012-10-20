package com.bencvt.minecraft.client.buildregion;

import libshapedraw.LibShapeDraw;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.src.mod_BuildRegion;

import com.bencvt.minecraft.client.buildregion.region.Axis;
import com.bencvt.minecraft.client.buildregion.region.Direction3D;
import com.bencvt.minecraft.client.buildregion.region.RegionBase;
import com.bencvt.minecraft.client.buildregion.region.RegionPlane;
import com.bencvt.minecraft.client.buildregion.region.RegionType;
import com.bencvt.minecraft.client.buildregion.ui.InputManager;
import com.bencvt.minecraft.client.buildregion.ui.MessageManager;
import com.bencvt.minecraft.client.buildregion.ui.ShapeManager;

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
    private final BuildModeValue buildMode;
    private RegionBase curRegion;
    private RegionBase prevRegion; // will never be null

    public Controller(LibShapeDraw libShapeDraw, mod_BuildRegion mod, Minecraft minecraft) {
        if (!LibShapeDraw.isControllerInitialized()) { // TODO: replace with .verifyInitialized()
            throw new RuntimeException("LibShapeDraw does not appear to be installed properly");
        }
        this.minecraft = minecraft;
        inputManager = new InputManager(this, mod, minecraft);
        messageManager = new MessageManager(minecraft);
        shapeManager = new ShapeManager(this, libShapeDraw);
        modTitle = mod.getName() + " v" + mod.getModVersion();
        buildMode = new BuildModeValue(BuildMode.INSIDE);
        cmdReset();
    }

    // ========
    // cmd methods to update state
    // ========

    public void cmdReset() {
        buildMode.setValueNoAnimation(BuildMode.INSIDE);
        curRegion = null;
        prevRegion = new RegionPlane(new Vector3(0, 63, 0), Axis.Y);
        shapeManager.reset();
    }

    public void cmdClear() {
        if (curRegion == null) {
            return;
        }
        prevRegion = curRegion;
        curRegion = null;
        shapeManager.animateFadeOut();
        messageManager.info("build region unlocked\n");
    }

    public void cmdDenyClick() {
        messageManager.info("misclick blocked by build region\n");
    }

    public void cmdSet(RegionBase newRegion) {
        if (curRegion != null) {
            prevRegion = curRegion;
        }
        curRegion = newRegion;

        // Update UI.
        // TODO: make the shapeManager smart enough to animate with a shift or
        // even a rotate instead of a fade when appropriate.
        //shapeManager.animateShift(dir.axis, region.getCoord(dir.axis));
        shapeManager.animateFadeOut();
        shapeManager.animateFadeIn(curRegion);
        messageManager.info("build region locked to " + curRegion + "\n");
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
                minecraft.thePlayer.posZ);
        RegionBase newRegion = protoRegion.copyUsing(origin, dir.axis);
        // Move the origin so it's in front of the player.
        newRegion.shiftCoord(dir.axis, dir.axisDirection * 2);

        cmdSet(newRegion);
    }

    public void cmdShiftFacing(int amount) {
        Direction3D dir = getFacingDirection(true);
        if (dir == null) {
            return;
        }
        if (curRegion == null || !curRegion.isValidAxis(dir.axis)) {
            cmdSetFacing();
            return;
        }

        // Update region.
        curRegion.shiftCoord(dir.axis, amount * dir.axisDirection);

        // Update UI.
        shapeManager.animateShift(dir.axis, curRegion.getCoord(dir.axis));
        messageManager.info("build region shifted to " + curRegion + "\n");
    }

    public void cmdMode(BuildMode newMode) {
        buildMode.setValue(newMode);
        messageManager.info("build region mode: " +
                newMode.toString().toLowerCase()); // TODO: "\npress shift-<bind> for advanced options"
    }

    public void cmdModeNext() {
        buildMode.setValue(buildMode.getValue().getNextMode());
    }

    // ========
    // Methods called from mod_BuildRegion to react to game events
    // ========

    public void renderHUD() {
        messageManager.render();
    }

    public void updatePlayerPosition(ReadonlyVector3 playerCoords) {
        shapeManager.updateObserverPosition(playerCoords);
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

    private RegionBase getPrototypeRegion() {
        if (curRegion != null) {
            return curRegion;
        }
        if (prevRegion != null) {
            return prevRegion;
        }
        throw new IllegalStateException("prevRegion should never be null");
    }
}
