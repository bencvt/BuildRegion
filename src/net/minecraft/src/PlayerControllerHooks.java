package net.minecraft.src;

import java.util.LinkedHashSet;

import net.minecraft.client.Minecraft;

/**
 * Provide an API for player clicking on entities and blocks.
 * 
 * @author bencvt
 */
public class PlayerControllerHooks {
    public static final int VERSION = 1;

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
    public static boolean isBuildReplaceBlock(int blockX, int blockY, int blockZ) {
        final WorldClient world = Minecraft.getMinecraft().theWorld;
        if (world == null) {
            return false;
        }
        int blockId = world.getBlockId(blockX, blockY, blockZ);
        return blockId == Block.snow.blockID ||
                blockId == Block.vine.blockID ||
                blockId == Block.tallGrass.blockID ||
                blockId == Block.deadBush.blockID;
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
