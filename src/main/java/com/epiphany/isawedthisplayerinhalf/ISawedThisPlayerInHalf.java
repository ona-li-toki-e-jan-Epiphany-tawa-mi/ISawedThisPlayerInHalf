package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.rendering.PlayerRendererWrapper;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("iswdthsplrinhlf")
public class ISawedThisPlayerInHalf {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "iswdthsplrinhlf";

    public ISawedThisPlayerInHalf() {
        // Registering on-load event listeners.
        IEventBus loadingEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        loadingEventBus.addListener(this::setup);
        loadingEventBus.addListener(this::doClientStuff);
        loadingEventBus.addListener(this::onServerStarting);
        //modEventBus.addListener(this::enqueueIMC);
        //modEventBus.addListener(this::processIMC);

        MinecraftForge.EVENT_BUS.register(Offsetter.class);
    }

    private void setup(final FMLCommonSetupEvent event) {

    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        RenderingOffsetter.doClientStuff();
        PlayerRendererWrapper.doClientStuff();

        MinecraftForge.EVENT_BUS.register(RenderingOffsetter.class);
    }

    public void onServerStarting(FMLServerStartingEvent event) {

    }



    /*private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }*/

    /*private void processIMC(final InterModProcessEvent event) {
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
