package com.epiphany.isawedthisplayerinhalf.config;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

// TODO (MAYBE) Add server config that lets admins control kicking and banning when invalid offsets are sent.
// TODO Internalize and translate server messages.

/**
 * Server configuration file and data.
 */
@OnlyIn(Dist.DEDICATED_SERVER)
public class ServerConfig {
    private static ForgeConfigSpec.EnumValue<Locale> serverLocale;

    public static void enable() {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();

        serverLocale = configBuilder.defineEnum("language", Locale.EN_US);

        ConfigCommon.buildConfigFile("isawedthisplayerinhalf-server.toml", ModConfig.Type.SERVER, configBuilder.build(), false);
    }


    /**
     * Gets the currently set server locale for displaying messages.
     * @return The currently set server locale.
     */
    public static Locale getServerLocale() {
        return serverLocale.get();
    }
}
