package com.epiphany.isawedthisplayerinhalf.networking;

import com.epiphany.isawedthisplayerinhalf.ISawedThisPlayerInHalf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/**
 * Stuff for networking.
 */
public class Networker {
    static SimpleChannel modChannel;
    private static int freeChannelIndex = 0;

    /**
     * Registers packets to the mod's channel.
     */
    public static void registerPackets() {
        modChannel = NetworkRegistry.newSimpleChannel(new ResourceLocation(ISawedThisPlayerInHalf.MOD_ID, "offsets_transfer"), () -> "1.0", s -> true, s -> true);

        modChannel.registerMessage(getNextIndex(), SetOffsetsPacket.class, SetOffsetsPacket::toBytes, SetOffsetsPacket::new, SetOffsetsPacket::handle);
        modChannel.registerMessage(getNextIndex(), RequestOffsetsPacket.class, RequestOffsetsPacket::toBytes, RequestOffsetsPacket::new, RequestOffsetsPacket::handle);
        modChannel.registerMessage(getNextIndex(), RequestDisplayOffsetsPacket.class, RequestDisplayOffsetsPacket::toBytes, RequestDisplayOffsetsPacket::new, RequestDisplayOffsetsPacket::handle);
    }

    /**
     * Grabs the next available index of the channel.
     *
     * @return The next available index of the channel.
     */
    private static int getNextIndex() {
        return freeChannelIndex++;
    }



    /**
     * Sends the server the offsets to set to the player.
     *
     * @param offsets The offsets to set to the player.
     */
    @OnlyIn(Dist.CLIENT)
    public static void sendServerOffsets(Vec3d offsets) {
        sendServerOffsets(offsets.x, offsets.y, offsets.z);
    }

    /**
     * Sends the server the offsets to set to the player.
     *
     * @param xOffset The x-offset to set to the player.
     * @param yOffset The y-offset to set to the player.
     * @param zOffset The z-offset to set to the player.
     */
    @OnlyIn(Dist.CLIENT)
    public static void sendServerOffsets(double xOffset, double yOffset, double zOffset) {
        modChannel.sendToServer(new SetOffsetsPacket(Minecraft.getInstance().player, xOffset, yOffset, zOffset));
    }

    /**
     * Sends a request to the server for the given player's offsets.
     * If that player exists on the server and has offsets then the server will send a SetOffsets
     *
     * @param playerEntityId The entity id of the requested player.
     */
    @OnlyIn(Dist.CLIENT)
    public static void requestOffsets(int playerEntityId) {
        modChannel.sendToServer(new RequestOffsetsPacket(playerEntityId));
    }

    /**
     * Requests the server to message the client with the offsets of the specified player.
     * Name must be 16 characters or shorter.
     *
     * @param playerName The name of the player to request the offsets of.
     *
     * @throws IndexOutOfBoundsException If the player name is longer than 16 characters.
     */
    @OnlyIn(Dist.CLIENT)
    public static void requestDisplayOffsets(String playerName) {
        if (playerName.length() > 16)
            throw new IndexOutOfBoundsException("The player name '" + playerName + "' is too long! Player names must be 16 or shorter.");

        modChannel.sendToServer(new RequestDisplayOffsetsPacket(playerName));
    }
}
