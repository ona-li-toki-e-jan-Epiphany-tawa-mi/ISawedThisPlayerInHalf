package com.epiphany.isawedthisplayerinhalf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.UUID;

public class Offsetter {
    public static final HashMap<UUID, Vec3d> playerOffsetMap = new HashMap<>();

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

        if (!playerOffsetMap.containsKey(playerUUID))
            playerOffsetMap.put(playerUUID, new Vec3d(2, 0, 0));

        return playerOffsetMap.get(playerUUID);
    }

    /**
     * Sets the offsets for the given player.
     *
     * @param player The player to set the offsets of.
     * @param offsets The offsets to set to the player
     */
    public static void setOffsets(PlayerEntity player, Vec3d offsets) {
        playerOffsetMap.put(player.getUniqueID(), offsets);
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
