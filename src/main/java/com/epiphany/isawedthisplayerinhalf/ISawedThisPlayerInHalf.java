package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.networking.Networker;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// TODO Have [::ofs set] command show what the new offset is.

// TODO Possibly have the server store offsets in a database, leaving client storage as it is in memory.
// TODO Make class transformers more compatible.


@Mod("swdthsplyrnhlf")
public class ISawedThisPlayerInHalf {
    public static final String MOD_ID = "swdthsplyrnhlf";

    public ISawedThisPlayerInHalf() {
        FMLJavaModLoadingContext.get().getModEventBus().register(ISawedThisPlayerInHalf.class);
        MinecraftForge.EVENT_BUS.register(Offsetter.class);
        Networker.registerPackets();
    }

    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent fmlClientSetupEvent) {
        MinecraftForge.EVENT_BUS.register(RenderingOffsetter.class);
        Config.onEnable();
    }
}
