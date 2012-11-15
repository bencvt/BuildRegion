package net.minecraft.src;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
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
    public static final int VERSION = 4;

    public interface PlayerControllerEventListener {
        /**
         * @param isLeftClick
         * @param blockX
         * @param blockY
         * @param blockZ
         * @param direction
         * @param cancelled true if this event was cancelled by another
         *                  PlayerControllerEventListener, false normally
         * @return whether to allow the click event to be processed.
         *         <p>
         *         Normally, just return the value of the <b>cancelled</b>
         *         parameter. Alternately, returning <b>true</b> will
         *         explicitly cancel the event, while <b>false</b> will
         *         explicitly allow the event.
         */
        public boolean onBlockClick(boolean isLeftClick, int blockX, int blockY, int blockZ, int direction, boolean cancelled);

        /**
         * Block damage events happen when you hold down the left-click mouse
         * button in creative mode.
         * 
         * @param blockX
         * @param blockY
         * @param blockZ
         * @param direction
         * @param cancelled true if this event was cancelled by another
         *                  PlayerControllerEventListener, false normally
         * @return whether to allow the click event to be processed.
         *         <p>
         *         Normally, just return the value of the <b>cancelled</b>
         *         parameter. Alternately, returning <b>true</b> will
         *         explicitly cancel the event, while <b>false</b> will
         *         explicitly allow the event.
         */
        public boolean onBlockDamage(int blockX, int blockY, int blockZ, int direction, boolean cancelled);

        /**
         * @param isLeftClick
         * @param entity
         * @param cancelled true if this event was cancelled by another
         *                  PlayerControllerEventListener, false normally
         * @return whether to allow the click event to be processed.
         *         <p>
         *         Normally, just return the value of the <b>cancelled</b>
         *         parameter. Alternately, returning <b>true</b> will
         *         explicitly cancel the event, while <b>false</b> will
         *         explicitly allow the event.
         */
        public boolean onEntityClick(boolean isLeftClick, Entity entity, boolean cancelled);
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
        if (dispatchBlockClickEvent(true, x, y, z, direction)) {
            return;
        }
        super.clickBlock(x, y, z, direction);
    }

    @Override
    public void onPlayerDamageBlock(int x, int y, int z, int direction) {
        if (dispatchBlockDamageEvent(x, y, z, direction)) {
            return;
        }
        super.onPlayerDamageBlock(x, y, z, direction);
    }

    @Override
    public boolean onPlayerRightClick(EntityPlayer player, World world, ItemStack itemStack, int x, int y, int z, int direction, Vec3 hitVec) {
        if (dispatchBlockClickEvent(false, x, y, z, direction)) {
            return false;
        }
        return super.onPlayerRightClick(player, world, itemStack, x, y, z, direction, hitVec);
    }

    @Override
    public void attackEntity(EntityPlayer player, Entity target) {
        if (dispatchEntityClickEvent(true, target)) {
            return;
        }
        super.attackEntity(player, target);
    }

    // This method should be deobfuscated as something like rightClickEntity or
    // interactWithEntity.
    @Override
    public boolean func_78768_b(EntityPlayer player, Entity target) {
        if (dispatchEntityClickEvent(false, target)) {
            return false;
        }
        return super.func_78768_b(player, target);
    }

    /** @return true if the click event should be cancelled */
    protected boolean dispatchBlockClickEvent(boolean isLeftClick, int blockX, int blockY, int blockZ, int direction) {
        boolean cancelled = false;
        for (PlayerControllerEventListener listener : eventListeners) {
            cancelled = listener.onBlockClick(isLeftClick, blockX, blockY, blockZ, direction, cancelled);
        }
        return cancelled;
    }

    /** @return true if the click event should be cancelled */
    protected boolean dispatchBlockDamageEvent(int blockX, int blockY, int blockZ, int direction) {
        boolean cancelled = false;
        for (PlayerControllerEventListener listener : eventListeners) {
            cancelled = listener.onBlockDamage(blockX, blockY, blockZ, direction, cancelled);
        }
        return cancelled;
    }

    /** @return true if the click event should be cancelled */
    protected boolean dispatchEntityClickEvent(boolean isLeftClick, Entity entity) {
        boolean cancelled = false;
        for (PlayerControllerEventListener listener : eventListeners) {
            cancelled = listener.onEntityClick(isLeftClick, entity, cancelled);
        }
        return cancelled;
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

    /**
     * Clunky list of block IDs that normally intercept right-click when
     * attempting to place a block adjacent to it. I.e., the block's class
     * overrides onBlockActivated() to always return true when the player is
     * holding an ItemBlock.
     * <p>
     * There are also several block classes that *sometimes* return true; they
     * are either special-cased or ignored.
     */
    private static final HashSet<Integer> blockIdsConsumingRightClick =
            new HashSet<Integer>(Arrays.asList(new Integer[] {
                    Block.anvil.blockID,
                    Block.beacon.blockID,
                    Block.bed.blockID,
                    Block.brewingStand.blockID,
                    Block.cake.blockID,
                    Block.cauldron.blockID,
                    Block.chest.blockID,
                    Block.commandBlock.blockID,
                    Block.dispenser.blockID,
                    Block.doorSteel.blockID,
                    Block.doorWood.blockID,
                    Block.dragonEgg.blockID,
                    Block.enchantmentTable.blockID,
                    Block.enderChest.blockID,
                    Block.fenceGate.blockID,
                    Block.lever.blockID,
                    Block.music.blockID, // a.k.a. note block
                    Block.redstoneRepeaterActive.blockID,
                    Block.redstoneRepeaterIdle.blockID,
                    Block.stoneButton.blockID,
                    Block.stoneOvenActive.blockID,
                    Block.stoneOvenIdle.blockID,
                    Block.trapdoor.blockID,
                    Block.woodenButton.blockID,
                    Block.workbench.blockID // a.k.a. crafting table
            }));

    public static boolean isRightClickConsumerBlock(int blockX, int blockY, int blockZ) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            return false;
        }
        int blockId = mc.theWorld.getBlockId(blockX, blockY, blockZ);
        if (blockIdsConsumingRightClick.contains(blockId)) {
            return true;
        }

        final ItemStack heldItemStack = mc.thePlayer == null ? null : mc.thePlayer.inventory.getCurrentItem();
        // Special case: jukeboxes
        if (blockId == Block.jukebox.blockID) {
            if (mc.theWorld.getBlockMetadata(blockX, blockY, blockZ) != 0) {
                return true;
            } else if (heldItemStack != null && heldItemStack.getItem() instanceof ItemRecord) {
                return true;
            }
            // Else the player is right-clicking on a non-empty jukebox with
            // something other than a record.
        }
        // Other special cases (e.g., flower pots, redstone ore, TNT) are
        // ignored.
        // 
        // Other block types don't override onBlockActivated() but can
        // still consume right-clicks depending on the held item (e.g., using a
        // hoe on dirt). We ignore those cases too.
        // 
        // Finally, it's worth noting that item frames are entities, not
        // blocks, so it's also not considered by this method.
        return false;
    }
}
