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
    public RequestOffsetsPacket(int playerEntityId) {
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
                    Vec3d offsets = Offsetter.playerOffsetMap.get(requestedPlayer.getUniqueID());

                    if (offsets != null)
                        Networker.modChannel.send(PacketDistributor.PLAYER.with(() -> sender), new SetOffsetsPacket((PlayerEntity) requestedPlayer, offsets));
                }
            }
        }));

        context.setPacketHandled(true);
    }
}
