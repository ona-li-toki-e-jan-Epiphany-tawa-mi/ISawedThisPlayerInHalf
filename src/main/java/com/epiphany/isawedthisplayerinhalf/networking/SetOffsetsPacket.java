package com.epiphany.isawedthisplayerinhalf.networking;

import com.epiphany.isawedthisplayerinhalf.ISawedThisPlayerInHalf;
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
import org.apache.logging.log4j.Level;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * A packet used for sending a player's offsets.
 */
public class SetOffsetsPacket implements IPacket {
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

        context.enqueueWork(() -> DistExecutor.<Boolean>runForDist(
                // Client-side.
                () -> () -> {
                    // Security.
                    // TODO Add translations.
                    if (!Double.isFinite(this.xOffset) || !Double.isFinite(this.yOffset) || !Double.isFinite(this.zOffset)) {
                        ISawedThisPlayerInHalf.LOGGER.log(Level.WARN, "Server attempted to send invalid offsets for the player with id " + this.playerID + "! - will not store!");
                        return false;
                    }


                    Minecraft minecraftInstance = Minecraft.getInstance();
                    Entity possiblePlayer = minecraftInstance.world.getEntityByID(this.playerID);

                    if (possiblePlayer instanceof PlayerEntity && !possiblePlayer.equals(minecraftInstance.player))
                        Offsetter.setOffsets((PlayerEntity) possiblePlayer, new Vec3d(this.xOffset, this.yOffset, this.zOffset));

                    return true;
                },

                // Server-side.
                () -> () -> {
                    ServerPlayerEntity sender = context.getSender();

                    if (sender != null) {
                        // Security.
                        if (!Double.isFinite(this.xOffset) || !Double.isFinite(this.yOffset) || !Double.isFinite(this.zOffset)) {
                            ISawedThisPlayerInHalf.LOGGER.log(Level.WARN, "Player " + sender.getName().getString() + " attempted to send invalid offsets! - will not store nor send to clients");
                            return false;
                        }

                        int truePlayerID = sender.getEntityId();

                        if (truePlayerID != this.playerID) {
                            this.playerID = truePlayerID;
                            ISawedThisPlayerInHalf.LOGGER.log(Level.WARN, "Player " + sender.getName().getString() + " attempted to send offsets using an invalid id! - will use true id instead");
                        }


                        Offsetter.setOffsets(sender, new Vec3d(this.xOffset, this.yOffset, this.zOffset));


                        // Routes packet to the other players on the server with the mod.
                        UUID senderUUID = sender.getUniqueID();
                        PlayerList playerList = sender.getServer().getPlayerList();

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
