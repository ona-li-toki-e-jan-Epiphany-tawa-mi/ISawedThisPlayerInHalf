package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.networking.Networker;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// TODO Add proper error messages to the command.
// TODO Possibly move resetting player and biped models into the setting function.
// TODO Proofread JavaScript god file.

// TODO Possibly have offsets stored in a database.
// TODO Make class transformers more compatible.
// TODO Mod may be incompatible with other mods using player render event; investigate.

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
