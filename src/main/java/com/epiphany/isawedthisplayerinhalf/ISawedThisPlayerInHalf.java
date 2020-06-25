package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.rendering.PlayerRendererWrapper;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO Fix problem with not being able to break far away blocks.
// TODO Get players to drop items at the offset position.

// TODO Reset both hashmaps when the client leaves a server.

// TODO Add option to turn graphics off.

@Mod("iswdthsplrinhlf")
public class ISawedThisPlayerInHalf {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "iswdthsplrinhlf";

    public ISawedThisPlayerInHalf() {
        FMLJavaModLoadingContext.get().getModEventBus().register(ISawedThisPlayerInHalf.class);
        MinecraftForge.EVENT_BUS.register(Offsetter.class);
    }

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent event) {
        RenderingOffsetter.doClientStuff();
        PlayerRendererWrapper.doClientStuff();

        MinecraftForge.EVENT_BUS.register(RenderingOffsetter.class);
    }

    @SubscribeEvent
    public static void onServerStarting(FMLServerStartingEvent event) {

    }


    /*@SubscribeEvent
    public static void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }*/

    /*@SubscribeEvent
    public static void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }*/



    /*@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {

        }
    }*/
}
