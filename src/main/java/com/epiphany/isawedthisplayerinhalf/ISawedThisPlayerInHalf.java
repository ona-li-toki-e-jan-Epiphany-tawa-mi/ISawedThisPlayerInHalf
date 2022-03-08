package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.config.ClientConfig;
import com.epiphany.isawedthisplayerinhalf.config.ServerConfig;
import com.epiphany.isawedthisplayerinhalf.networking.Networker;
import com.epiphany.isawedthisplayerinhalf.networking.SetOffsetsPacket;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO MAYBE knockback in LivingEntity, make sure to remove attackEntityFrom transformer.

// trySleep() next in PlayerEntity.
// TODO updateCape ()V in PlayerEntity.
// TODO Offset position rotates with camera, fix. Possibly in PlayerEntity#updateRidden()

// TODO Add more debug information, more logging.

// TODO (MAYBE MAYBE NOT) Expose an API for other mods to interact with this one.

/**
 * This mod splits apart the player, freeing the torso from the legs.
 *
 * @see <a href="https://github.com/ona-li-toki-e-jan-Epiphany-tawa-mi/ISawedThisPlayerInHalf">Github repo.</a>
 * @see <a href="https://www.curseforge.com/minecraft/mc-mods/i-sawed-this-player-in-half">Curseforge page.</a>
 */
@Mod("swdthsplyrnhlf")
public class ISawedThisPlayerInHalf {
    public static final String MOD_ID = "swdthsplyrnhlf";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ISawedThisPlayerInHalf() {
        FMLJavaModLoadingContext.get().getModEventBus().register(ISawedThisPlayerInHalf.class);
        MinecraftForge.EVENT_BUS.register(Offsetter.class);

        Networker.registerPackets();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent fmlClientSetupEvent) {
        MinecraftForge.EVENT_BUS.register(OffsetsCommand.class);

        ClientConfig.enable();
        RenderingOffsetter.replacePlayerRenderers();
    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    @SubscribeEvent
    public static void onServerSetup(FMLDedicatedServerSetupEvent fmlDedicatedServerSetupEvent) {
        MinecraftForge.EVENT_BUS.register(SetOffsetsPacket.class);

        ServerConfig.enable();
        ServerTranslations.enable();
    }
}
