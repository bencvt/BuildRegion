package com.bencvt.minecraft.buildregion;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.BlockHalfSlab;
import net.minecraft.src.Entity;
import net.minecraft.src.ItemRecord;
import net.minecraft.src.ItemSlab;
import net.minecraft.src.ItemStack;
import net.minecraft.src.PlayerControllerHooks;
import net.minecraft.src.PlayerControllerHooks.PlayerControllerEventListener;

import com.bencvt.minecraft.buildregion.region.Direction3D;

public class BlockClickHandler implements PlayerControllerEventListener {
    private final Minecraft minecraft;
    private final Controller controller;

    public BlockClickHandler(Controller controller) {
        minecraft = Minecraft.getMinecraft();
        this.controller = controller;
        PlayerControllerHooks.register(this);
    }

    @Override
    public boolean onBlockClick(boolean isLeftClick, int blockX, int blockY, int blockZ, int direction, boolean cancelled) {
        return cancelled || handleBlockClick(isLeftClick, blockX, blockY, blockZ, direction);
    }

    @Override
    public boolean onBlockDamage(int blockX, int blockY, int blockZ, int direction, boolean cancelled) {
        return cancelled || handleBlockClick(true, blockX, blockY, blockZ, direction);
    }

    @Override
    public boolean onEntityClick(boolean isLeftClick, Entity entity, boolean cancelled) {
        return cancelled || controller.getInputManager().consumesClick(isLeftClick);
    }

    /**
     * @return true if we're consuming/cancelling the event, or
     *         false if the click should be allowed,
     */
    private boolean handleBlockClick(boolean isLeftClick, int blockX, int blockY, int blockZ, int direction) {
        if (controller.getInputManager().consumesClick(isLeftClick)) {
            return true;
        }
        if (minecraft.thePlayer == null || minecraft.theWorld == null) {
            // Block click events shouldn't be happening when a world isn't
            // loaded, but just in case...
            return false;
        }
        final BuildMode buildMode = controller.getBuildMode().getValue();
        if (buildMode == BuildMode.DISPLAY || controller.getCurRegion() == null) {
            // There isn't a build region active to prevent clicks.
            return false;
        }
        if (isLeftClick) {
            if (isExcludedBlock(minecraft.theWorld.getBlockId(blockX, blockY, blockZ))) {
                // Allow certain block types to be destroyed no matter what.
                return false;
            }
            // Else this is a normal block being destroyed.
        } else {
            if (isRightClickConsumerBlock(blockX, blockY, blockZ)) {
                // Do not block access to crafting tables, buttons, doors,
                // chests, furnaces, etc.
                return false;
            } else if (isPlayerHoldingExcludedBlock()) {
                // Allow certain block types to be placed no matter what.
                return false;
            } else if (!isBuildReplaceBlock(blockX, blockY, blockZ, direction)) {
                // Placing a block on top of snow, tall grass, etc., or turning
                // a single slab into a double slab. Adjust the block coords
                // accordingly.
                Direction3D dir = Direction3D.fromValue(direction);
                blockX = dir.getNeighborX(blockX);
                blockY = dir.getNeighborY(blockY);
                blockZ = dir.getNeighborZ(blockZ);
            }
            // Else this is a normal block being placed.
        }

        boolean insideRegion = controller.getCurRegion().isInsideRegion(blockX, blockY, blockZ);
        if ((buildMode == BuildMode.INSIDE && !insideRegion) ||
                (buildMode == BuildMode.OUTSIDE && insideRegion)) {
            controller.notifyDenyClick();
            return true;
        }
        return false;
    }

    /**
     * @return true if the specified block id is always allowed to be
     *         placed/destroyed. Being able to light your work area with
     *         minimal fuss is an important quality-of-life issue in survival
     *         mode!
     */
    private static boolean isExcludedBlock(int blockId) {
        return blockId == Block.torchWood.blockID;
    }

    private boolean isPlayerHoldingExcludedBlock() {
        ItemStack held = minecraft.thePlayer.getCurrentEquippedItem();
        return held == null ? false : isExcludedBlock(held.itemID);
    }

    /**
     * @return true if the block at the specified position is a special block
     *         that, when attempting to place a block adjacent to it, replaces
     *         the original block instead.
     */
    private boolean isBuildReplaceBlock(int blockX, int blockY, int blockZ, int direction) {
        // Certain blocks that you can walk through are always treated as air
        // by Minecraft when placing another block on top of it.
        int blockId = minecraft.theWorld.getBlockId(blockX, blockY, blockZ);
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
        ItemStack heldItemStack = minecraft.thePlayer.getCurrentEquippedItem();
        if (heldItemStack == null || !(heldItemStack.getItem() instanceof ItemSlab)) {
            return false;
        }
        ItemSlab heldSlab = (ItemSlab) heldItemStack.getItem();

        // Do the slab types match?
        if (!slabBlockIdMatches(heldSlab, blockId)) {
            return false;
        }
        int blockMetadata = minecraft.theWorld.getBlockMetadata(blockX, blockY, blockZ);
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

    private static boolean slabBlockIdMatches(ItemSlab slab, int blockId) {
        try {
            for (Field field : ItemSlab.class.getDeclaredFields()) {
                if (field.getType() == BlockHalfSlab.class) {
                    field.setAccessible(true);
                    BlockHalfSlab block = (BlockHalfSlab) field.get(slab);
                    // isOpaqueCube returns true if the block is a double slab.
                    if (!block.isOpaqueCube() && blockId == block.blockID) {
                        return true;
                    }
                    // Else keep looking; there should be two BlockHalfSlab
                    // fields which are listed by getDeclaredFields in
                    // (theoretically) any order.
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("internal reflection error", e);
        }
        return false;
    }

    /**
     * Determine whether the block at the specified coordinate will consume
     * the player's right-click rather than letting the player place a block
     * adjacent to it.
     * <p>
     * Unfortunately there's no clean method or API for this, so we rely on a
     * list of known block IDs and a few special cases.
     * 
     * @return true if the block will definitely consume the right-click,
     *         false if it will probably not.
     */
    private boolean isRightClickConsumerBlock(int blockX, int blockY, int blockZ) {
        final int blockId = minecraft.theWorld.getBlockId(blockX, blockY, blockZ);

        // Check the list of IDs for blocks that override onBlockActivated() to
        // always return true when the player is holding an ItemBlock.
        if (blockIdsConsumingRightClick.contains(blockId)) {
            return true;
        }

        // Special case: jukeboxes
        if (blockId == Block.jukebox.blockID) {
            if (minecraft.theWorld.getBlockMetadata(blockX, blockY, blockZ) != 0) {
                // Jukebox isn't empty; right-clicking will pop the record out.
                return true;
            }
            final ItemStack held = minecraft.thePlayer.getCurrentEquippedItem();
            if (held != null && held.getItem() instanceof ItemRecord) {
                // Player is holding a record to put into an empty jukebox.
                return true;
            }
            // Else the player is right-clicking on a non-empty jukebox with
            // something other than a record. Minecraft will let a block be
            // placed there.
        }

        // Other special cases (e.g., flower pots, redstone ore, TNT) are
        // ignored, as they rely on a specific item being held... it would be
        // a massive amount of duplicated and hard-to-maintain code if we
        // included them all.
        // 
        // Other block types don't override onBlockActivated() but can
        // still consume right-clicks depending on the held item (e.g., using a
        // hoe on dirt). We ignore those cases too, for the same reason.
        // 
        // Finally, it's worth noting that item frames are entities, not
        // blocks, so they are also not considered by this method.
        return false;
    }

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
}
