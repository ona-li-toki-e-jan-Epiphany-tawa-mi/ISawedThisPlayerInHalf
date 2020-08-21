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
     * @param offsets The offset to be set to the player.
     */
    public SetOffsetPacket(PlayerEntity player, Vec3d offsets) {
        playerName = player.getGameProfile().getName();
        this.offsets = offsets;
    }

    /**
     * Creates a new set offset packet.
     *
     * @param player The player who's offset is being set.
     * @param x The x offset.
     * @param y The y offset.
     * @param z The z offset.
     */
    public SetOffsetPacket(PlayerEntity player, double x, double y, double z) {
        this(player, new Vec3d(x, y, z));
    }

    public SetOffsetPacket(PacketBuffer packetBuffer) {
        offsets = new Vec3d(packetBuffer.readDouble(), packetBuffer.readDouble(), packetBuffer.readDouble());
        playerName = packetBuffer.readString(packetBuffer.readInt());
    }

    public void toBytes(PacketBuffer packetBuffer) {
        packetBuffer.writeDouble(offsets.x);
        packetBuffer.writeDouble(offsets.y);
        packetBuffer.writeDouble(offsets.z);

        packetBuffer.writeInt(playerName.length());
        packetBuffer.writeString(playerName);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork( () -> DistExecutor.<Boolean>runForDist(
                // Client-side.
                () -> () -> {
                    // Finds the player with the given name and sets their offset.
                    for (PlayerEntity player : Minecraft.getInstance().world.getPlayers())
                        if (player.getGameProfile().getName().equals(playerName)) {
                            Offsetter.setOffsets(player, offsets);
                            break;
                        }

                    return true;
                },

                // Server-side.
                () -> () -> {
                    ServerPlayerEntity player = context.getSender();

                    if (player != null) {
                        UUID playerUUID = player.getUniqueID();
                        // Whether or send all players' offsets to the sender.
                        boolean informSender = Offsetter.playerOffsetMap.containsKey(playerUUID);

                        Offsetter.setOffsets(playerUUID, offsets);

                        PlayerList playerList = player.getServer().getPlayerList();

                        for (UUID otherPlayerID : Offsetter.playerOffsetMap.keySet()) {
                            ServerPlayerEntity otherPlayer = playerList.getPlayerByUUID(otherPlayerID);

                            if (otherPlayer != null) {
                                // Routes packet to the other players on the server with the mod.
                                if (!otherPlayer.getGameProfile().getName().equals(playerName))
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
