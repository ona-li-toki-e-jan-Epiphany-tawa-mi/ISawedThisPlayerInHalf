package com.epiphany.isawedthisplayerinhalf.networking;

import com.epiphany.isawedthisplayerinhalf.Offsetter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

// TODO Check out what returning false does for packet handling.
// TODO Move from using vec3d to 3 doubles.

/**
 * A packet used for sending a player's offsets.
 */
public class SetOffsetsPacket implements IPacket {
    private final int playerID;
    private final Vec3d offsets;

    /**
     * Creates a new SetOffsetsPacket.
     *
     * @param player The player whose offsets are being sent.
     * @param offsets The offsets to be set to the player.
     */
    SetOffsetsPacket(PlayerEntity player, Vec3d offsets) {
        this.playerID = player.getEntityId();
        this.offsets = offsets;
    }

    /**
     * Recreates a SetOffsetsPacket from the information sent from the other side.
     *
     * @param packetBuffer The buffer of the sent packet.
     */
    public SetOffsetsPacket(PacketBuffer packetBuffer) {
        this.playerID = packetBuffer.readInt();
        this.offsets = new Vec3d(packetBuffer.readDouble(), packetBuffer.readDouble(), packetBuffer.readDouble());
    }

    @Override
    public void toBytes(PacketBuffer packetBuffer) {
        packetBuffer.writeInt(this.playerID);
        packetBuffer.writeDouble(this.offsets.x);
        packetBuffer.writeDouble(this.offsets.y);
        packetBuffer.writeDouble(this.offsets.z);
    }



    @Override
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> DistExecutor.<Boolean>runForDist(
                // Client-side.
                () -> () -> {
                    Entity possiblePlayer = Minecraft.getInstance().world.getEntityByID(this.playerID);

                    if (possiblePlayer instanceof PlayerEntity && !possiblePlayer.equals(Minecraft.getInstance().player))
                        Offsetter.setOffsets((PlayerEntity) possiblePlayer, this.offsets);

                    return true;
                },

                // Server-side.
                () -> () -> {
                    ServerPlayerEntity sender = context.getSender();

                    if (sender != null) {
                        Offsetter.setOffsets(sender, this.offsets);


                        UUID senderUUID = sender.getUniqueID();
                        PlayerList playerList = sender.getServer().getPlayerList();

                        // Routes packet to the other players on the server with the mod.
                        for (UUID otherPlayerUUID : Offsetter.getOffsetPlayerUUIDs()) {
                            if (otherPlayerUUID.equals(senderUUID))
                                continue;

                            ServerPlayerEntity otherPlayer = playerList.getPlayerByUUID(otherPlayerUUID);
                            Networker.modChannel.send(PacketDistributor.PLAYER.with(() -> otherPlayer), this);
                        }
                    }

                    return true;
                }
        ));

        context.setPacketHandled(true);
    }
}
