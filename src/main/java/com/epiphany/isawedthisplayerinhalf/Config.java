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

// TODO Look into adding a config menu in the mode menu.
// TODO Check out to see if config translation can be displayed depending on client locale.

/**
 * Deals with configuration data for the mod.
 */
@OnlyIn(Dist.CLIENT)
public class Config {
    private static ForgeConfigSpec.DoubleValue offsetX, offsetY, offsetZ;

    /**
     * Sets up config file and loads in data if it already exits.
     */
    public static void enable() {
        final String configFileName = "isawedthisplayerinhalf-client.toml";

        ForgeConfigSpec configSpecification = buildConfig();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, configSpecification);
        loadConfigFile(FMLPaths.CONFIGDIR.get().resolve(configFileName).toString(), configSpecification);
    }

    /**
     * Builds the specification of what the config file should contain.
     *
     * @return The config file specification.
     */
    public static ForgeConfigSpec buildConfig() {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();

        configBuilder.comment(
                " (en-US) The coordinates of the player's upper-half.",
                " (ru-RU) Koordinaty vyerkhnyej chasti tyela igroka.",
                " (tok (eo-UY)) nanpa ni li ma ante pi sijelo sewi sina li pana e sijelo ni lon ona."
        );
        offsetX = configBuilder.defineInRange("offsets.x", 0, -Double.MAX_VALUE, Double.MAX_VALUE);
        offsetY = configBuilder.defineInRange("offsets.y", 0, -Double.MAX_VALUE, Double.MAX_VALUE);
        offsetZ = configBuilder.defineInRange("offsets.z", 0, -Double.MAX_VALUE, Double.MAX_VALUE);

        return configBuilder.build();
    }

    /**
     * Loads in the config file from the given path.
     *
     * @param path The path to the config file.
     * @param configSpecification The config file specification.
     */
    private static void loadConfigFile(String path, ForgeConfigSpec configSpecification) {
        CommentedFileConfig configFile = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
        configFile.load();
        configSpecification.setConfig(configFile);
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
