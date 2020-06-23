package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.rendering.PlayerRendererWrapper;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

import java.util.HashMap;
import java.util.UUID;

/**
 * Contains various functions to offset the actions taken by the player.
 */
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
        return playerOffsetMap.containsKey(playerUUID) ? copyVector(playerOffsetMap.get(playerUUID)) : new Vec3d(2, 0, 0);
    }

    /**
     * Sets the offsets for the given player.
     *
     * @param player The player to set the offsets of.
     * @param offsets The offsets to set to the player.
     */
    public static void setOffsets(PlayerEntity player, Vec3d offsets) {
        Vec3d offsetsCopy = copyVector(offsets);

        playerOffsetMap.put(player.getUniqueID(), offsetsCopy);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            PlayerRendererWrapper wrappedRenderer = RenderingOffsetter.getRenderer(player);

            if (wrappedRenderer != null) {
                wrappedRenderer.setOffsets(offsetsCopy);

                if (offsetsCopy.lengthSquared() == 0)
                    wrappedRenderer.reset();
            }
        });
    }

    private static Vec3d copyVector(Vec3d vector) {
        return new Vec3d(vector.x, vector.y, vector.z);
    }



    /**
     * Offsets the initial position of the raycast in the function pick of Entity, if the entity is a player.
     *
     * @param entity The entity whose raycast is being offset.
     * @param initialPosition The initial position of the raycast.
     *
     * @return The offset position for the raycast to use.
     */
    public static Vec3d offsetRaycast(Vec3d initialPosition, Entity entity) {
        return entity instanceof PlayerEntity ? initialPosition.add(getOffsets((PlayerEntity) entity)) : initialPosition;
    }
}
