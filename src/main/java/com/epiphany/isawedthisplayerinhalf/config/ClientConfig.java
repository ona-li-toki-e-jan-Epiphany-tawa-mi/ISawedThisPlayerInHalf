package com.epiphany.isawedthisplayerinhalf.config;

import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

// TODO Look into adding a config menu in the mode menu.
// TODO Check out to see if config translation can be displayed depending on client locale.

/**
 * Client configuration file and data.
 */
@OnlyIn(Dist.CLIENT)
public class ClientConfig {
    private static ForgeConfigSpec.DoubleValue offsetX, offsetY, offsetZ;

    public static void enable() {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();

        configBuilder.comment(
                " (en-US) The coordinates of the player's upper-half.",
                " (ru-RU) Koordinaty vyerkhnyej chasti tyela igroka.",
                " (tok (eo-UY)) nanpa ni li ma ante pi sijelo sewi sina li pana e sijelo ni lon ona."
        );
        offsetX = configBuilder.defineInRange("offsets.x", 0, -Double.MAX_VALUE, Double.MAX_VALUE);
        offsetY = configBuilder.defineInRange("offsets.y", 0, -Double.MAX_VALUE, Double.MAX_VALUE);
        offsetZ = configBuilder.defineInRange("offsets.z", 0, -Double.MAX_VALUE, Double.MAX_VALUE);

        ConfigCommon.buildConfigFile("isawedthisplayerinhalf-client.toml", ModConfig.Type.CLIENT,
                configBuilder.build(), true);
    }


    /**
     * Gets the offsets from the config file.
     *
     * @return The offsets.
     */
    public static Vec3d getOffsets() {
        return new Vec3d(offsetX.get(), offsetY.get(), offsetZ.get());
    }

    /**
     * Sets the offset to be stored in the config file.
     *
     * @param x The x offset.
     * @param y The y offset.
     * @param z The z offset.
     */
    public static void setOffsets(double x, double y, double z) {
        offsetX.set(x);
        offsetY.set(y);
        offsetZ.set(z);
    }



    private static boolean b = true;public static void a(){b=!b;}@SuppressWarnings("unused")public static boolean b(){return b;}
}
