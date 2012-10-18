package com.bencvt.minecraft.client.buildregion;

import libshapedraw.LibShapeDraw;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.src.BaseMod;
import net.minecraft.src.PlayerControllerHooks;

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
    private RegionBase region;

    public Controller(LibShapeDraw libShapeDraw, BaseMod mod, Minecraft minecraft) {
        if (!LibShapeDraw.isControllerInitialized()) { // TODO: replace with .verifyInitialized()
            throw new RuntimeException("LibShapeDraw does not appear to be installed properly");
        }
        this.minecraft = minecraft;
        inputManager = new InputManager(this, mod, minecraft);
        messageManager = new MessageManager(minecraft);
        shapeManager = new ShapeManager(libShapeDraw);
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public void cmdClear(boolean silent) {
        unlockRegion(silent);
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

    public void cmdMode() {
        cmdMode(BuildMode.getActiveMode().getNextMode());
    }
    public void cmdMode(BuildMode newMode) {
        if (newMode == null) {
            throw new IllegalArgumentException();
        }
        BuildMode.setActiveMode(newMode);
        messageManager.info("build region mode: " +
                BuildMode.getActiveMode().toString().toLowerCase()); // TODO: "\npress shift-<bind> for advanced options"
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

    private void unlockRegion(boolean silent) {
        if (region == null) {
            return;
        }
        region = null;
        shapeManager.animateFadeOut();
        if (!silent) {
            messageManager.info("build region unlocked\n");
        }
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
        } else if (BuildMode.getActiveMode() == BuildMode.INSIDE) {
            return region.isInsideRegion(x, y, z);
        } else if (BuildMode.getActiveMode() == BuildMode.OUTSIDE) {
            return !region.isInsideRegion(x, y, z);
        } else {
            return true;
        }
    }

    public void disallowedClick() {
        messageManager.info("misclick blocked by build region\n");
    }
}
