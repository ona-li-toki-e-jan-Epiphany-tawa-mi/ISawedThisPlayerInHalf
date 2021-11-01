package com.epiphany.isawedthisplayerinhalf;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

// TODO Move static initialization into onEnable.

/**
 * Deals with configuration data for the mod.
 */
@OnlyIn(Dist.CLIENT)
public class Config {
    private static final ForgeConfigSpec clientConfig;
    private static final ForgeConfigSpec.DoubleValue offsetX, offsetY, offsetZ;

    // Builds config file.
    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();

        configBuilder.comment(
                " (en-US) The coordinates of the player's upper-half.",
                " (ru-RU) Koordinaty vyerkhnyej chasti tyela igroka.",
                " (tok (eo-UY)) nanpa ni li ma ante pi sijelo sewi sina li pana e sijelo ni lon ona."
        );
        offsetX = configBuilder.defineInRange("offsets.x", 0, -Double.MAX_VALUE, Double.MAX_VALUE);
        offsetY = configBuilder.defineInRange("offsets.y", 0, -Double.MAX_VALUE, Double.MAX_VALUE);
        offsetZ = configBuilder.defineInRange("offsets.z", 0, -Double.MAX_VALUE, Double.MAX_VALUE);

        clientConfig = configBuilder.build();
    }

    /**
     * Loads in config data.
     */
    public static void enable() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientConfig);
        loadConfig(FMLPaths.CONFIGDIR.get().resolve("swdthsplyrnhlf-client.toml").toString());
    }

    /**
     * Loads in the config file from the given path.
     *
     * @param path The path to the config file.
     */
    private static void loadConfig(String path) {
        CommentedFileConfig configFile = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
        configFile.load();
        clientConfig.setConfig(configFile);
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



    private static boolean b = true;static void a(){b=!b;}@SuppressWarnings("unused")public static boolean b(){return b;}
}
