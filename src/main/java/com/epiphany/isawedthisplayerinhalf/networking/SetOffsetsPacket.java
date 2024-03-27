package com.epiphany.isawedthisplayerinhalf.networking;

import com.epiphany.isawedthisplayerinhalf.ISawedThisPlayerInHalf;
import com.epiphany.isawedthisplayerinhalf.Offsetter;
import com.epiphany.isawedthisplayerinhalf.ServerTranslations;
import com.epiphany.isawedthisplayerinhalf.config.ClientConfig;
import com.epiphany.isawedthisplayerinhalf.config.ServerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/*
 * MIT License
 *
 * Copyright (c) 2021-2022 ona-li-toki-e-jan-Epiphany-tawa-mi
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
 * A packet used for sending a player's offsets.
 */
public class SetOffsetsPacket implements IPacket {
    private static final Map<UUID, Integer> warningCounter = new HashMap<>();
    // TODO (MAYBE MAYBE NOT) Set up a queue for this if trySetClientOffsets(V,C) in Offsetter is used more than now.
    private static Vec3d expectedOffsets = null;
    private static Consumer<Boolean> responseListener = null;

    /**
     * Updates the offsets that are expected from the server.
     * When the server sends a SetOffsetsPacket over to the client for their own player, this is used to make sure that
     *   this change is desired and that it should update the client's offsets.
     * Once accepted the underlying variable will be set back to null.
     *
     * @param newExpectedOffsets The new offsets expected from the server.
     */
    static void setExpectedOffsets(@Nullable Vec3d newExpectedOffsets) {
        expectedOffsets = newExpectedOffsets;
    }

    /**
     * Sets the function to call once the server serves a response packet; true if offsets were successfully set, false
     *   if not.
     * Once called the underlying variable will be set back to null.
     *
     * @param onRespond The function to call when the server responds.
     */
    static void setResponseListener(@Nullable Consumer<Boolean> onRespond) {
        responseListener = onRespond;
    }

    private int playerID;
    private final double xOffset, yOffset, zOffset;

    /**
     * Creates a new SetOffsetsPacket.
     *
     * @param player The player whose offsets are being sent.
     * @param xOffset The x-offset of the player.
     * @param yOffset The y-offset of the player.
     * @param zOffset The z-offset of the player.
     */
    SetOffsetsPacket(PlayerEntity player, double xOffset, double yOffset, double zOffset) {
        this.playerID = player.getEntityId();
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    /**
     * Recreates a SetOffsetsPacket from the information sent from the other side.
     *
     * @param packetBuffer The buffer of the sent packet.
     */
    public SetOffsetsPacket(PacketBuffer packetBuffer) {
        this.playerID = packetBuffer.readInt();
        this.xOffset = packetBuffer.readDouble();
        this.yOffset = packetBuffer.readDouble();
        this.zOffset = packetBuffer.readDouble();
    }

    @Override
    public void toBytes(PacketBuffer packetBuffer) {
        packetBuffer.writeInt(this.playerID);
        packetBuffer.writeDouble(this.xOffset);
        packetBuffer.writeDouble(this.yOffset);
        packetBuffer.writeDouble(this.zOffset);
    }



    @Override
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        final boolean MAGIC_BOOLEAN = true;
        context.enqueueWork(() -> DistExecutor.runForDist(
                // Client-side.
                () -> () -> {
                    Minecraft minecraftInstance = Minecraft.getInstance();
                    Entity possiblePlayer = minecraftInstance.world.getEntityByID(this.playerID);


                    // Security.
                    if (!Double.isFinite(this.xOffset) || !Double.isFinite(this.yOffset) || !Double.isFinite(this.zOffset)) {
                        ISawedThisPlayerInHalf.LOGGER.log(Level.WARN,
                                I18n.format("network.error.set_offsets.invalid_offsets", this.playerID));

                        if (responseListener != null && expectedOffsets != null && possiblePlayer.equals(minecraftInstance.player)) {
                            responseListener.accept(false);
                            responseListener = null;

                            expectedOffsets = null;
                        }

                        return MAGIC_BOOLEAN;
                    }


                    if (possiblePlayer instanceof PlayerEntity)
                        if (possiblePlayer.equals(minecraftInstance.player)) {
                            boolean response;

                            // Checks to see if the change is desired.
                            if (expectedOffsets != null && expectedOffsets.x == this.xOffset && expectedOffsets.y == this.yOffset
                                    && expectedOffsets.z == this.zOffset) {
                                Offsetter.setOffsets((PlayerEntity) possiblePlayer, new Vec3d(this.xOffset, this.yOffset, this.zOffset));
                                ClientConfig.setOffsets(this.xOffset, this.yOffset, this.zOffset);

                                response = true;

                            } else {
                                response = false;
                                ISawedThisPlayerInHalf.LOGGER.log(Level.WARN,
                                        I18n.format("network.error.set_offsets.invalid_offsets", this.playerID));
                            }

                            expectedOffsets = null;

                            if (responseListener != null) {
                                responseListener.accept(response);
                                responseListener = null;
                            }

                        } else
                            Offsetter.setOffsets((PlayerEntity) possiblePlayer, new Vec3d(this.xOffset, this.yOffset, this.zOffset));

                    return MAGIC_BOOLEAN;
                },

                // Server-side.
                () -> () -> {
                    ServerPlayerEntity sender = context.getSender();
                    if (sender == null)
                        return MAGIC_BOOLEAN;

                    // Security.
                    if (!Double.isFinite(this.xOffset) || !Double.isFinite(this.yOffset) || !Double.isFinite(this.zOffset)) {
                        ISawedThisPlayerInHalf.LOGGER.log(Level.WARN, ServerTranslations.translateAndFormatKey(
                                "network.error.set_offsets.invalid_offsets", sender.getName().getString()));

                        Networker.modChannel.send(PacketDistributor.PLAYER.with(() -> sender), this);


                        // Kicks players if they send too many invalid packets.
                        UUID senderUUID = sender.getUniqueID();
                        int warnings = warningCounter.getOrDefault(senderUUID, 1);

                        if (ServerConfig.shouldKickOnInvalid() && warnings > ServerConfig.getKickWarningCount()) {
                            sender.connection.disconnect(new TranslationTextComponent("network.disconnect.invalid_offsets"));
                            warningCounter.remove(senderUUID);

                            ISawedThisPlayerInHalf.LOGGER.log(Level.WARN, ServerTranslations.translateAndFormatKey(
                                    "network.disconnected_player.invalid_offsets", sender.getName().getString()));

                        } else
                            warningCounter.put(senderUUID, warnings + 1);


                        if (Offsetter.getOffsetsOrNull(sender) == null)
                            Offsetter.setOffsets(sender, Vec3d.ZERO);


                        return MAGIC_BOOLEAN;
                    }

                    this.playerID = sender.getEntityId();


                    Offsetter.setOffsets(sender, new Vec3d(this.xOffset, this.yOffset, this.zOffset));


                    // Routes packet to the other players on the server with the mod.
                    UUID senderUUID = sender.getUniqueID();
                    PlayerList playerList = sender.getServer().getPlayerList();

                    for (UUID otherPlayerUUID : Offsetter.getOffsetPlayerUUIDs()) {
                        ServerPlayerEntity otherPlayer = playerList.getPlayerByUUID(otherPlayerUUID);
                        Networker.modChannel.send(PacketDistributor.PLAYER.with(() -> otherPlayer), this);
                    }

                    return MAGIC_BOOLEAN;
                }
        ));

        context.setPacketHandled(true);
    }


    /**
     * Removes players' warning counts when they leave.
     */
    @OnlyIn(Dist.DEDICATED_SERVER)
    @SubscribeEvent
    public static void onPlayerLeaveServer(PlayerEvent.PlayerLoggedOutEvent playerLoggedOutEvent) {
        warningCounter.remove(playerLoggedOutEvent.getPlayer().getUniqueID());
    }
}
