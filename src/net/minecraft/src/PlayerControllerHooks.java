package net.minecraft.src;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;

import net.minecraft.client.Minecraft;

/**
 * Provide an API for player clicking on entities and blocks.
 * 
 * Implemented by inserting a proxy for Minecraft.playerController.
 * 
 * @author bencvt
 */
public class PlayerControllerHooks extends PlayerControllerMP {
    public static final int VERSION = 3;

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
        return eventListeners.add(listener);
    }

    public static boolean unregister(PlayerControllerEventListener listener) {
        return eventListeners.remove(listener);
    }

    /**
     * Must be called at least once for every new server connection.
     */
    public static void installHooks() {
        PlayerControllerMP pc = Minecraft.getMinecraft().playerController;
        if (pc == null || pc instanceof PlayerControllerHooks) {
            // not ready to be installed, or already installed
            return;
        }
        Minecraft.getMinecraft().playerController = new PlayerControllerHooks(pc);
    }

    protected PlayerControllerHooks(PlayerControllerMP orig) {
        super(null, null);
        try {
            // Do some funky reflection to copy every field from orig to this,
            // even ones marked as private and final.
            Field ff = Field.class.getDeclaredField("modifiers");
            ff.setAccessible(true);
            for (Field f : PlayerControllerMP.class.getDeclaredFields()) {
                f.setAccessible(true);
                ff.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                f.set(this, f.get(orig));
            }
        } catch (Exception e) {
            throw new RuntimeException("internal reflection error - unable to set up PlayerControllerMP proxy", e);
        }
    }

    @Override
    public void clickBlock(int x, int y, int z, int direction) {
        if (!dispatchBlockClickEvent(true, x, y, z, direction)) {
            return;
        }
        super.clickBlock(x, y, z, direction);
    }

    @Override
    public void onPlayerDamageBlock(int x, int y, int z, int direction) {
        if (!dispatchBlockDamageEvent(x, y, z, direction)) {
            return;
        }
        super.onPlayerDamageBlock(x, y, z, direction);
    }

    @Override
    public boolean onPlayerRightClick(EntityPlayer player, World world, ItemStack itemStack, int x, int y, int z, int direction, Vec3 hitVec) {
        if (!dispatchBlockClickEvent(false, x, y, z, direction)) {
            return false;
        }
        return super.onPlayerRightClick(player, world, itemStack, x, y, z, direction, hitVec);
    }

    @Override
    public void attackEntity(EntityPlayer player, Entity target) {
        if (!dispatchEntityClickEvent(true, target)) {
            return;
        }
        super.attackEntity(player, target);
    }

    @Override
    public boolean func_78768_b(EntityPlayer player, Entity target) {
        if (!dispatchEntityClickEvent(false, target)) {
            return false;
        }
        return super.func_78768_b(player, target);
    }

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

        // Is the player is attempting to place a slab adjacent to another slab?
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
}
