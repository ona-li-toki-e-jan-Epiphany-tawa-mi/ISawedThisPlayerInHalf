package com.epiphany.isawedthisplayerinhalf.networking;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Interface for enforcing some methods that are needed to register the packet so that it can be used.
 * Classes implementing this will also need a constructor that accepts a PacketBuffer.
 */
public interface IPacket {
    /**
     * Converts the data of the packet into bytes and loads it into a packet buffer.
     *
     * @param packetBuffer The packet buffer to load data into.
     */
    void toBytes(PacketBuffer packetBuffer);

    /**
     * Handles the packet once it arrives from the other side of the network. (Server -> client or client -> server.)
     */
    void handle(Supplier<NetworkEvent.Context> contextSupplier);
}
