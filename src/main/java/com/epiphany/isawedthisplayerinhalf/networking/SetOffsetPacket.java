package com.epiphany.isawedthisplayerinhalf.networking;

import com.epiphany.isawedthisplayerinhalf.Offsetter;
import net.minecraft.client.Minecraft;
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

// TODO Client does not receive offsets from other players when those other players are not loaded on that client.

/**
 * A packet used for sending a player's offset.
 */
public class SetOffsetPacket {
    private final String playerName;
    private final Vec3d offsets;

    /**
     * Creates a new set offset packet.
     *
     * @param player The player who's offset is being set.
     * @param offsets The offsets to be set to the player.
     */
    public SetOffsetPacket(PlayerEntity player, Vec3d offsets) {
        this.playerName = player.getGameProfile().getName();
        this.offsets = offsets;
    }

    /**
     * Creates a new SetOffset packet.
     *
     * @param player The player who's offset is being set.
     * @param x The x offset to be set to the player.
     * @param y The y offset to be set to the player.
     * @param z The z offset to be set to the player.
     */
    public SetOffsetPacket(PlayerEntity player, double x, double y, double z) {
        this(player, new Vec3d(x, y, z));
    }

    /**
     * Recreates offset packets from the information sent from the other side.
     *
     * @param packetBuffer The sent packet.
     */
    public SetOffsetPacket(PacketBuffer packetBuffer) {
        this.offsets = new Vec3d(packetBuffer.readDouble(), packetBuffer.readDouble(), packetBuffer.readDouble());
        this.playerName = packetBuffer.readString(packetBuffer.readInt());
    }

    /**
     * Converts the data of the packet into bytes and loads it into a packet buffer.
     *
     * @param packetBuffer The packet buffer to load data into.
     */
    public void toBytes(PacketBuffer packetBuffer) {
        packetBuffer.writeDouble(this.offsets.x);
        packetBuffer.writeDouble(this.offsets.y);
        packetBuffer.writeDouble(this.offsets.z);

        packetBuffer.writeInt(this.playerName.length());
        packetBuffer.writeString(this.playerName);
    }

    /**
     * Handles incoming and outgoing SetOffset packets.
     */
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork( () -> DistExecutor.<Boolean>runForDist(
                // Client-side.
                () -> () -> {
                    // Finds the player with the given name and sets their offset.
                    for (PlayerEntity player : Minecraft.getInstance().world.getPlayers())
                        if (player.getGameProfile().getName().equals(this.playerName)) {
                            Offsetter.setOffsets(player, this.offsets);
                            break;
                        }

                    return true;
                },

                // Server-side.
                () -> () -> {
                    ServerPlayerEntity player = context.getSender();

                    if (player != null) {
                        UUID playerUUID = player.getUniqueID();
                        // Sends all other players' offsets to the sender if they just joined the server.
                        boolean informSender = !Offsetter.playerOffsetMap.containsKey(playerUUID);
                        PlayerList playerList = player.getServer().getPlayerList();

                        Offsetter.setOffsets(playerUUID, this.offsets);

                        // Routes packet to the other players on the server with the mod.
                        for (UUID otherPlayerID : Offsetter.playerOffsetMap.keySet()) {
                            ServerPlayerEntity otherPlayer = playerList.getPlayerByUUID(otherPlayerID);

                            if (otherPlayer != null) {
                                if (!otherPlayer.getGameProfile().getName().equals(this.playerName))
                                    Networker.modChannel.send(PacketDistributor.PLAYER.with(() -> otherPlayer), this);

                                if (informSender)
                                    Networker.modChannel.send(PacketDistributor.PLAYER.with(() -> player), new SetOffsetPacket(otherPlayer, Offsetter.playerOffsetMap.get(otherPlayerID)));
                            }
                        }
                    }

                    return true;
                }
        ));
        context.setPacketHandled(true);
    }
}
