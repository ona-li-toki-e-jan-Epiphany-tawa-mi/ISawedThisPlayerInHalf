package com.epiphany.isawedthisplayerinhalf.networking;

import com.epiphany.isawedthisplayerinhalf.ISawedThisPlayerInHalf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/**
 * Stuff for networking.
 */
public class Networker {
    public static SimpleChannel modChannel;
    private static int IDCounter = 0;

    /**
     * Registers packets to the mod's channel.
     */
    public static void registerPackets() {
        modChannel = NetworkRegistry.newSimpleChannel(new ResourceLocation(ISawedThisPlayerInHalf.MOD_ID, "offsettransfer"), () -> "1.0", s -> true, s -> true);

        modChannel.registerMessage(
                IDCounter++,
                SetOffsetPacket.class,
                SetOffsetPacket::toBytes,
                SetOffsetPacket::new,
                SetOffsetPacket::handle
        );
    }
}
