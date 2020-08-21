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

/**
 * Deals with configuration data for the mod.
 */
@OnlyIn(Dist.CLIENT)
public class Config {
    private static final ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec clientConfig;

    static final ForgeConfigSpec.DoubleValue offsetX, offsetY, offsetZ;
    private static final ForgeConfigSpec.BooleanValue a;
    private static boolean b;

    // Builds config file.
    static {
        configBuilder.comment(" The following coordinates offset the player's upper-half.");

        offsetX = configBuilder.defineInRange("offset.x", 0, -Double.MAX_VALUE, Double.MAX_VALUE);
        offsetY = configBuilder.defineInRange("offset.y", 0, -Double.MAX_VALUE, Double.MAX_VALUE);
        offsetZ = configBuilder.defineInRange("offset.z", 0, -Double.MAX_VALUE, Double.MAX_VALUE);

        a=configBuilder.define("what.renderGraphics", true);

        clientConfig = configBuilder.build();
    }

    /**
     * Loads in config data.
     */
    public static void onEnable() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientConfig);
        loadConfig(FMLPaths.CONFIGDIR.get().resolve("swdthsplyrnhlf-client.toml").toString());

        b=a.get();
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

    static void a() {boolean c=!a.get();a.set(c);b=c;}public static boolean b() {return b;}
}
