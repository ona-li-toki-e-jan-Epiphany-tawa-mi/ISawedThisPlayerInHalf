package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.config.ClientConfig;
import com.epiphany.isawedthisplayerinhalf.networking.Networker;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains various functions to offset the actions taken by the player.
 */
public class Offsetter {
    private static final ConcurrentHashMap<UUID, Vec3d> playerOffsetMap = new ConcurrentHashMap<>();

    /**
     * Gets the offsets a player has.
     *
     * @param playerEntity The player to get the offsets from.
     *
     * @return The offsets a player has.
     */
    public static Vec3d getOffsets(PlayerEntity playerEntity) {
        Vec3d offsets = getOffsetsOrNull(playerEntity);
        return offsets != null ? offsets : Vec3d.ZERO;
    }

    /**
     * Gets the offsets an entity has, if they are a player.
     *
     * @param entity The entity to get the offsets from.
     *
     * @return The offsets an entity has.
     */
    public static Vec3d getOffsets(Entity entity) {
        return entity instanceof PlayerEntity ? getOffsets((PlayerEntity) entity) : Vec3d.ZERO;
    }

    /**
     * Returns the offsets a player has, or null, if they do not have any.
     *
     * @param playerEntity The player to get the offsets of.
     *
     * @return The offsets of the player, or null.
     */
    public static Vec3d getOffsetsOrNull(PlayerEntity playerEntity) {
        return playerOffsetMap.get(playerEntity.getUniqueID());
    }

    /**
     * Returns an unmodifiable set containing the UUIDs of the players that have offsets.
     *
     * @return A set containing the UUIDs of offset players.
     */
    public static Set<UUID> getOffsetPlayerUUIDs() {
        return Collections.unmodifiableSet(playerOffsetMap.keySet());
    }

    /**
     * Sets the offsets for the given player.
     *
     * @param playerEntity The player to set the offsets of.
     * @param offsets The offsets to set to the player.
     */
    public static void setOffsets(PlayerEntity playerEntity, Vec3d offsets) {
        UUID playerUUID = playerEntity.getUniqueID();

        final boolean MAGIC_BOOLEAN = true;
        DistExecutor.runForDist(
                // Client-side.
                () -> () -> {
                    enqueueWork(() -> {
                        playerOffsetMap.put(playerUUID, offsets);
                        RenderingOffsetter.setOffsets(playerEntity, offsets);
                    });

                    return MAGIC_BOOLEAN;
                },

                // Server-side.
                () -> () -> {
                    playerOffsetMap.put(playerUUID, offsets);
                    return MAGIC_BOOLEAN;
                }
        );
    }

    /**
     * Removes the given player's offsets.
     *
     * @param playerEntity The player to remove the offsets from.
     */
    public static void unsetOffsets(PlayerEntity playerEntity) {
        UUID playerUUID = playerEntity.getUniqueID();

        final boolean MAGIC_BOOLEAN = true;
        DistExecutor.runForDist(
                // Client-side.
                () -> () -> {
                    playerOffsetMap.remove(playerUUID);
                    RenderingOffsetter.unsetOffsets(playerUUID);

                    return MAGIC_BOOLEAN;
                },

                // Server-side.
                () -> () -> {
                    playerOffsetMap.remove(playerUUID);
                    return MAGIC_BOOLEAN;
                }
        );
    }

    /**
     * Clears the offsets of all players.
     */
    public static void clearAllOffsets() {
        final boolean MAGIC_BOOLEAN = true;
        DistExecutor.runForDist(
                // Client-side.
                () -> () -> {
                    enqueueWork(() -> {
                        playerOffsetMap.clear();
                        RenderingOffsetter.clearAllOffsets();
                    });

                    return MAGIC_BOOLEAN;
                },

                // Server-side.
                () -> () -> {
                    playerOffsetMap.clear();
                    return MAGIC_BOOLEAN;
                }
        );
    }

    /**
     * Runs the given function on the game's main thread.
     *
     * The function will be executed immediately if called on the main thread, else it places it onto the work queue to
     * be executed at the next opportunity.
     *
     * @param runnable The function to run.
     */
    @OnlyIn(Dist.CLIENT)
    private static void enqueueWork(Runnable runnable) {
        ThreadTaskExecutor<?> executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT);

        if (executor.isOnExecutionThread()) {
            runnable.run();

        } else
            executor.deferTask(runnable);
    }



    /**
     * Loads the clients offsets when the world loads. And, if on a dedicated server, sends them to it.
     */
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    public static void onPostHandleJoinGame() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPlayerEntity player = minecraft.player;
        Vec3d offsets = ClientConfig.getOffsets();

        setOffsets(player, offsets);

        if (!minecraft.isSingleplayer())
            Networker.sendServerOffsets(offsets);
    }

    /**
     * Clears the offset maps when the client leaves a server.
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onLeaveServer(ClientPlayerNetworkEvent.LoggedOutEvent loggedOutEvent) {
        clearAllOffsets();
    }

    /**
     * Request the offsets of players that are being loaded from the server.
     *
     * @param entityId The entity id the entity.
     * @param entity The entity being loaded.
     */
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    public static void onPostEntityLoad(int entityId, Entity entity) {
        Minecraft minecraft = Minecraft.getInstance();

        if (entity instanceof PlayerEntity && !minecraft.isSingleplayer() && !entity.equals(minecraft.player))
            Networker.requestOffsets(entityId);
    }

    /**
     * Unloads players from the offset map when they are being unloaded from the world.
     *
     * @param entity The entity being unloaded.
     */
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    public static void onEntityUnload(Entity entity) {
        if (entity instanceof PlayerEntity && !entity.equals(Minecraft.getInstance().player))
            unsetOffsets((PlayerEntity) entity);
    }


    /**
     * Removes players from the offset map when they leave.
     */
    @OnlyIn(Dist.DEDICATED_SERVER)
    @SubscribeEvent
    public static void onPlayerLeaveServer(PlayerEvent.PlayerLoggedOutEvent playerLoggedOutEvent) {
        unsetOffsets(playerLoggedOutEvent.getPlayer());
    }



    /**
     * Offsets the position of thrown items.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onItemDropped(ItemTossEvent itemTossEvent) {
        Vec3d offsets = getOffsets(itemTossEvent.getPlayer());

        if (!offsets.equals(Vec3d.ZERO)) {
            ItemEntity itemEntity = itemTossEvent.getEntityItem();

            itemTossEvent.getEntityItem().setPosition(
                    itemEntity.getPosX() + offsets.x,
                    itemEntity.getPosY() + offsets.y,
                    itemEntity.getPosZ() + offsets.z
            );
        }
    }
}
