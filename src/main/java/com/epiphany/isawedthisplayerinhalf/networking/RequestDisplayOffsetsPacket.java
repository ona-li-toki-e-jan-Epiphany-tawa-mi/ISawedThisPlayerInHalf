package com.epiphany.isawedthisplayerinhalf.networking;

import com.epiphany.isawedthisplayerinhalf.ISawedThisPlayerInHalf;
import com.epiphany.isawedthisplayerinhalf.Offsetter;
import io.netty.handler.codec.DecoderException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.Level;

import java.util.function.Supplier;

/**
 * Used to query the server and have it return where a player has offsets and what they are when ::offsets get cannot get a result from the client.
 */
public class RequestDisplayOffsetsPacket implements IPacket {
    private String playerName;

    /**
     * Creates a new RequestDisplayOffsetsPacket.
     *
     * @param playerName The name of the player to request the offsets of.
     */
    RequestDisplayOffsetsPacket(String playerName) {
        this.playerName = playerName;
    }

    public RequestDisplayOffsetsPacket(PacketBuffer packetBuffer) {
        try {
            this.playerName = packetBuffer.readString(16);

        } catch (DecoderException decoderException) {
            this.playerName = null;
        }
    }

    @Override
    public void toBytes(PacketBuffer packetBuffer) {
        packetBuffer.writeString(this.playerName, 16);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();
            if (sender == null)
                return;

            // Security.
            if (this.playerName == null) {
                sender.sendMessage(new TranslationTextComponent(
                        "commands.swdthsplyrnhlf.errors.unknown_player")
                        .applyTextStyle(TextFormatting.RED));

                ISawedThisPlayerInHalf.LOGGER.log(Level.WARN, "Player " + sender.getName().getString() + " attempted to request offsets with an invalid player name (" +
                        this.playerName + ")! - will not process!");
                return;
            }


            PlayerList playerList = sender.getServer().getPlayerList();
            boolean success = false;

            for (PlayerEntity player : playerList.getPlayers())
                if (this.playerName.equals(player.getName().getString())) {
                    Vec3d playerOffsets = Offsetter.getOffsetsOrNull(player);

                    if (playerOffsets != null) {
                        sender.sendMessage(new TranslationTextComponent(
                                "commands.swdthsplyrnhlf.offsets.get.of_player", this.playerName, playerOffsets.x, playerOffsets.y, playerOffsets.z));

                    } else
                        sender.sendMessage(new TranslationTextComponent(
                                "commands.swdthsplyrnhlf.offsets.get.no_offsets", this.playerName));

                    success = true;
                }

            if (!success)
                sender.sendMessage(new TranslationTextComponent(
                        "commands.swdthsplyrnhlf.errors.unknown_player")
                        .applyTextStyle(TextFormatting.RED));
        }));

        context.setPacketHandled(true);
    }
}
