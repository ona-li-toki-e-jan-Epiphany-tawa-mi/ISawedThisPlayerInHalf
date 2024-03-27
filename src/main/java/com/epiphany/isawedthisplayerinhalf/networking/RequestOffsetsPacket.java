package com.epiphany.isawedthisplayerinhalf.networking;

import com.epiphany.isawedthisplayerinhalf.Offsetter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

/*
 * MIT License
 *
 * Copyright (c) 2021 ona-li-toki-e-jan-Epiphany-tawa-mi
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
 * A packet used for querying a certain player's offsets from the server.
 */
public class RequestOffsetsPacket implements IPacket {
    private final int playerID;

    /**
     * Creates a new RequestOffsetsPacket.
     *
     * @param playerEntityId The entity id of the player.
     */
    RequestOffsetsPacket(int playerEntityId) {
        this.playerID = playerEntityId;
    }

    /**
     * Recreates a RequestOffsetsPacket from the information sent from the other side.
     *
     * @param packetBuffer The buffer of the sent packet.
     */
    public RequestOffsetsPacket(PacketBuffer packetBuffer) {
        this.playerID = packetBuffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer packetBuffer) {
        packetBuffer.writeInt(playerID);
    }



    @Override
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();

            if (sender != null) {
                Entity requestedPlayer = sender.world.getEntityByID(playerID);

                if (requestedPlayer instanceof PlayerEntity) {
                    Vec3d offsets = Offsetter.getOffsetsOrNull((PlayerEntity) requestedPlayer);

                    if (offsets != null)
                        Networker.modChannel.send(PacketDistributor.PLAYER.with(() -> sender), new SetOffsetsPacket((PlayerEntity) requestedPlayer, offsets.x, offsets.y, offsets.z));
                }
            }
        }));

        context.setPacketHandled(true);
    }
}
