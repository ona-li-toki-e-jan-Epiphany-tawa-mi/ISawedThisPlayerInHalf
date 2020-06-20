package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.rendering.PlayerRendererWrapper;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

import java.util.HashMap;
import java.util.UUID;

public class Offsetter {
    private static final HashMap<UUID, Vec3d> playerOffsetMap = new HashMap<>();

    /**
     * Gets the offsets a player has.
     * If the player has no offsets, new ones will be created.
     *
     * @param player The player to get the offsets from.
     *
     * @return The offsets a player has.
     */
    public static Vec3d getOffsets(PlayerEntity player) {
        UUID playerUUID = player.getUniqueID();
        Vec3d offset;

        if (!playerOffsetMap.containsKey(playerUUID)) {
            offset = new Vec3d(2, 0, 0);
            playerOffsetMap.put(playerUUID, offset);

        } else
            offset = playerOffsetMap.get(playerUUID);

        return offset;
    }

    /**
     * Sets the offsets for the given player.
     *
     * @param player The player to set the offsets of.
     * @param offsets The offsets to set to the player
     */
    public static void setOffsets(PlayerEntity player, Vec3d offsets) {
        playerOffsetMap.put(player.getUniqueID(), offsets);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            PlayerRendererWrapper wrappedRenderer = RenderingOffsetter.getRenderer(player);

            if (wrappedRenderer != null) {
                wrappedRenderer.setOffsets(offsets);

                if (offsets.lengthSquared() == 0)
                    wrappedRenderer.reset();
            }
        });
    }

    /**
     * Offsets the initial position of the raycast in the function pick of Entity, if the entity is a player.
     *
     * @param entity The entity whose raycast is being offset.
     * @param initialPosition The initial position of the raycast.
     *
     * @return The offset position for the raycast to use.
     */
    public static Vec3d offsetRaycast(Entity entity, Vec3d initialPosition) {
        return entity instanceof PlayerEntity ? initialPosition.add(getOffsets((PlayerEntity) entity)) : initialPosition;
    }



    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent breakEvent) {

    }
}
