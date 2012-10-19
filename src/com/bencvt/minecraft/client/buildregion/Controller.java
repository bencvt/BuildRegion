package com.bencvt.minecraft.client.buildregion;

import libshapedraw.LibShapeDraw;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.src.mod_BuildRegion;

import com.bencvt.minecraft.client.buildregion.region.Direction3D;
import com.bencvt.minecraft.client.buildregion.region.RegionBase;
import com.bencvt.minecraft.client.buildregion.region.RegionPlane;
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
    private RegionBase region; // TODO: RegionManager class

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
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public void cmdReset() {
        buildMode.setValueNoAnimation(BuildMode.INSIDE);
        region = null;
        shapeManager.reset();
    }

    public void cmdClear() {
        if (region == null) {
            return;
        }
        region = null;
        shapeManager.animateFadeOut();
        messageManager.info("build region unlocked\n");
    }

    public void cmdSet() {
        Direction3D dir = getFacingDirection();
        if (dir == null) {
            return;
        }

        // Remember stuff about existing region, if any.
        boolean shiftRegion = region != null && region.isValidAxis(dir.axis);

        // Define new region.
        Vector3 origin = new Vector3(
                minecraft.thePlayer.posX,
                minecraft.thePlayer.posY,
                minecraft.thePlayer.posZ);
        region = new RegionPlane(dir.axis, origin);
        region.shiftCoord(dir.axis, dir.axisDirection * 2);

        // Update UI.
        if (shiftRegion) {
            shapeManager.animateShift(dir.axis, region.getCoord(dir.axis));
        } else {
            shapeManager.animateFadeOut();
            shapeManager.animateFadeIn(region);
        }
        messageManager.info("build region locked to " + region + "\n");
    }

    public void cmdShift(int amount) {
        if (region == null) {
            cmdSet();
            return;
        }
        Direction3D dir = getFacingDirection();
        if (dir == null) {
            return;
        }
        if (!region.isValidAxis(dir.axis)) {
            cmdSet();
            return;
        }

        // Update region.
        region.shiftCoord(dir.axis, amount * dir.axisDirection);

        // Update UI.
        shapeManager.animateShift(dir.axis, region.getCoord(dir.axis));
        messageManager.info("build region shifted to " + region + "\n");
    }

    public void cmdModeNext() {
        buildMode.setValue(buildMode.getValue().getNextMode());
    }

    public void cmdMode(BuildMode newMode) {
        buildMode.setValue(newMode);
        messageManager.info("build region mode: " +
                newMode.toString().toLowerCase()); // TODO: "\npress shift-<bind> for advanced options"
    }

    private Direction3D getFacingDirection() {
        Direction3D dir = Direction3D.fromYawPitch(
                minecraft.thePlayer.rotationYaw,
                minecraft.thePlayer.rotationPitch);
        if (dir == null) {
            messageManager.error("ambiguous direction\nface north, south, east, west, up, or down");
        }
        return dir;
    }

    public void render() {
        messageManager.render();
    }

    public void updatePlayerPosition(ReadonlyVector3 playerCoords) {
        shapeManager.updateObserverPosition(playerCoords);
    }

    public boolean canBuild(double x, double y, double z) {
        if (region == null) {
            return true;
        } else if (buildMode.getValue() == BuildMode.INSIDE) {
            return region.isInsideRegion(x, y, z);
        } else if (buildMode.getValue() == BuildMode.OUTSIDE) {
            return !region.isInsideRegion(x, y, z);
        } else {
            return true;
        }
    }

    public void disallowedClick() {
        messageManager.info("misclick blocked by build region\n");
    }

    public String getModTitle() {
        return modTitle;
    }

    public ReadonlyBuildModeValue getBuildMode() {
        return buildMode;
    }
}
