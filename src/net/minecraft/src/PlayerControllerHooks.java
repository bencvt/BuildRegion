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
    public static final int VERSION = 5;

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
}
