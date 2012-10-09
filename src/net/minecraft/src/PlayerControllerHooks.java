package net.minecraft.src;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;

import net.minecraft.client.Minecraft;

/**
 * Provide an API for player clicking on entities and blocks.
 * 
 * @author bencvt
 */
public class PlayerControllerHooks {
    public static final int VERSION = 2;

    public interface PlayerControllerEventListener {
        /**
         * @param isLeftClick
         * @param blockX
         * @param blockY
         * @param blockZ
         * @param blockFace
         * @return false to cancel the click event
         */
        public boolean onBlockClick(boolean isLeftClick, int blockX, int blockY, int blockZ, int direction);

        /**
         * Block damage events happen when you hold down the left-click mouse
         * button in creative mode.
         * 
         * @param blockX
         * @param blockY
         * @param blockZ
         * @param direction
         * @return false to cancel the damage event
         */
        public boolean onBlockDamage(int blockX, int blockY, int blockZ, int direction);

        /**
         * @param isLeftClick
         * @param entity
         * @return false to cancel the click event
         */
        public boolean onEntityClick(boolean isLeftClick, Entity entity);
    }

    private static LinkedHashSet<PlayerControllerEventListener> eventListeners = new LinkedHashSet();

    public static boolean register(PlayerControllerEventListener listener) {
        return register(listener, true);
    }
    public static boolean register(PlayerControllerEventListener listener, boolean verifyPatch) {
        if (verifyPatch && !isPlayerControllerMPPatched()) {
            throw new RuntimeException("Unable to register click events. " +
                    "This is most likely due to a mod overwriting " +
                    PlayerControllerMP.class.getSimpleName() + ".class (PlayerControllerMP).");
        }
        return eventListeners.add(listener);
    }

    public static boolean isPlayerControllerMPPatched() {
        try {
            return PlayerControllerMP.playerControllerHooks.getClass().equals(PlayerControllerHooks.class);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean unregister(PlayerControllerEventListener listener) {
        return eventListeners.remove(listener);
    }

    /**
     * @return true if the block at the specified position is a special block
     *         that, when attempting to place a block adjacent to it, replaces
     *         the original block instead.
     */
    public static boolean isBuildReplaceBlock(int blockX, int blockY, int blockZ, int direction) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return false;
        }

        // Certain blocks that you can walk through are always treated as air
        // by Minecraft when placing another block on top of it.
        int blockId = mc.theWorld.getBlockId(blockX, blockY, blockZ);
        if (blockId == Block.snow.blockID ||
                blockId == Block.vine.blockID ||
                blockId == Block.tallGrass.blockID ||
                blockId == Block.deadBush.blockID) {
            return true;
        }

        // Is the player is attempting place a slab adjacent to another slab?
        if (!(Block.blocksList[blockId] instanceof BlockHalfSlab)) {
            return false;
        }
        ItemStack heldItemStack = mc.thePlayer.inventory.getCurrentItem();
        if (heldItemStack == null || !(heldItemStack.getItem() instanceof ItemSlab)) {
            return false;
        }
        ItemSlab heldSlab = (ItemSlab) heldItemStack.getItem();

        // Do the slab types match?
        if (blockId != getSlabBlockId(heldSlab)) {
            return false;
        }
        int blockMetadata = mc.theWorld.getBlockMetadata(blockX, blockY, blockZ);
        int slabSubtype = blockMetadata & 7;
        if (slabSubtype != heldItemStack.getItemDamage()) {
            return false;
        }

        // Is the player hitting it in the right spot for it to become a
        // double slab?
        boolean isUpsideDown = (blockMetadata & 8) != 0;
        if (!((direction == 0 && isUpsideDown) || (direction == 1 && !isUpsideDown))) {
            return false;
        }

        // We have a winner.
        return true;
    }

    private static int getSlabBlockId(ItemSlab slab) {
        try {
            for (Field field : ItemSlab.class.getDeclaredFields()) {
                if (field.getType() == BlockHalfSlab.class) {
                    field.setAccessible(true);
                    return ((BlockHalfSlab) field.get(slab)).blockID;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("internal reflection error", e);
        }
        throw new RuntimeException("internal reflection error - missing field");
    }

    // ====
    // ==== Dispatch functions, should only be called from modified vanilla classes
    // ====

    protected boolean dispatchBlockClickEvent(boolean isLeftClick, int blockX, int blockY, int blockZ, int direction) {
        for (PlayerControllerEventListener listener : eventListeners) {
            if (!listener.onBlockClick(isLeftClick, blockX, blockY, blockZ, direction)) {
                return false;
            }
        }
        return true;
    }

    protected boolean dispatchBlockDamageEvent(int blockX, int blockY, int blockZ, int direction) {
        boolean result = true;
        for (PlayerControllerEventListener listener : eventListeners) {
            if (!listener.onBlockDamage(blockX, blockY, blockZ, direction)) {
                return false;
            }
        }
        return true;
    }

    protected boolean dispatchEntityClickEvent(boolean isLeftClick, Entity entity) {
        boolean result = true;
        for (PlayerControllerEventListener listener : eventListeners) {
            if (!listener.onEntityClick(isLeftClick, entity)) {
                return false;
            }
        }
        return true;
    }

    protected PlayerControllerHooks() {
        // Do nothing; just be protected because only vanilla classes should be
        // instantiating this object. Client code uses the static methods.
    }
}
