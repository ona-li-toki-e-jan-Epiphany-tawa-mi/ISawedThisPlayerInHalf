package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.config.ClientConfig;
import com.epiphany.isawedthisplayerinhalf.networking.Networker;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/*
 * MIT License
 *
 * Copyright (c) 2020-2022 ona-li-toki-e-jan-Epiphany-tawa-mi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/

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
     * Meant for updating the offsets of players locally, without sharing over the network. For changing the client's
     *   own offsets on all sides, use {@link Offsetter#trySetClientOffsets(Vec3d, Consumer)}.
     *
     * @param playerEntity The player to set the offsets of.
     * @param offsets The offsets to set to the player.
     */
    public static void setOffsets(PlayerEntity playerEntity, Vec3d offsets) {
        UUID playerUUID = playerEntity.getUniqueID();

        playerOffsetMap.put(playerUUID, offsets);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
                RenderingOffsetter.setOffsets(playerEntity, offsets));
    }

    /**
     * Attempts to set the client's offsets, succeeding if the server accepts them.
     *
     * @param offsets The offsets to set for the client.
     * @param onRespond A function that is called once the server returns a response; true if successfully set, false
     *   if not
     */
    @OnlyIn(Dist.CLIENT)
    public static void trySetClientOffsets(Vec3d offsets, @Nullable Consumer<Boolean> onRespond) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.isSingleplayer()) {
            setOffsets(minecraft.player, offsets);
            ClientConfig.setOffsets(offsets.x, offsets.y, offsets.z);

            if (onRespond != null)
                onRespond.accept(true);

        } else
            Networker.sendServerOffsets(offsets, onRespond);
    }

    /**
     * Removes the given player's offsets.
     *
     * @param playerEntity The player to remove the offsets from.
     */
    public static void unsetOffsets(PlayerEntity playerEntity) {
        UUID playerUUID = playerEntity.getUniqueID();

        playerOffsetMap.remove(playerUUID);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
                RenderingOffsetter.unsetOffsets(playerUUID));
    }

    /**
     * Clears the offsets of all players.
     */
    public static void clearAllOffsets() {
        playerOffsetMap.clear();
        DistExecutor.runWhenOn(Dist.CLIENT, () -> RenderingOffsetter::clearAllOffsets);
    }



    /**
     * Loads the clients offsets when the world loads.
     */
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    public static void onPostHandleJoinGame() {
        Minecraft minecraft = Minecraft.getInstance();

        if (!minecraft.isSingleplayer())
            setOffsets(minecraft.player, Vec3d.ZERO);
        trySetClientOffsets(ClientConfig.getOffsets(), null);
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
