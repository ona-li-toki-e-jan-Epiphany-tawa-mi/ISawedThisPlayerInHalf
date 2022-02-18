package com.epiphany.isawedthisplayerinhalf.networking;

import com.epiphany.isawedthisplayerinhalf.ISawedThisPlayerInHalf;
import com.epiphany.isawedthisplayerinhalf.Offsetter;
import com.epiphany.isawedthisplayerinhalf.ServerTranslations;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * A packet used for sending a player's offsets.
 */
public class SetOffsetsPacket implements IPacket {
    private static final Map<UUID, Integer> warningCounter = new HashMap<>();

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
                    // Security.
                    if (!Double.isFinite(this.xOffset) || !Double.isFinite(this.yOffset) || !Double.isFinite(this.zOffset)) {
                        ISawedThisPlayerInHalf.LOGGER.log(Level.WARN,
                                I18n.format("network.error.set_offsets.invalid_offsets", this.playerID));
                        return MAGIC_BOOLEAN;
                    }


                    Minecraft minecraftInstance = Minecraft.getInstance();
                    Entity possiblePlayer = minecraftInstance.world.getEntityByID(this.playerID);

                    if (possiblePlayer instanceof PlayerEntity && !possiblePlayer.equals(minecraftInstance.player))
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


                        // TODO Make setting offsets completely serverside (including returning messages.)
                        // Kicks players if they send to many invalid packets.
                        UUID senderUUID = sender.getUniqueID();
                        int warnings = warningCounter.getOrDefault(senderUUID, 1);
                        warningCounter.put(senderUUID, warnings + 1);

                        if (ServerConfig.shouldKickOnInvalid() && warnings > ServerConfig.getKickWarningCount()) {
                            sender.connection.disconnect(new TranslationTextComponent("network.disconnect.invalid_offsets"));
                            warningCounter.remove(senderUUID);

                            ISawedThisPlayerInHalf.LOGGER.log(Level.WARN, ServerTranslations.translateAndFormatKey(
                                    "network.disconnected_player.invalid_offsets", sender.getName().getString()));
                        }


                        return MAGIC_BOOLEAN;
                    }

                    this.playerID = sender.getEntityId();


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
