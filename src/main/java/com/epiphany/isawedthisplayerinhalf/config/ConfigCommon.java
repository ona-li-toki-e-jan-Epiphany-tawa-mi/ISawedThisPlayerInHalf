package com.epiphany.isawedthisplayerinhalf.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfigBuilder;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

/**
 * Common code shared between config files.
 */
public class ConfigCommon {
    /**
     * Builds a new config file from the specification for the specified side.
     *
     * @param fileName The name of the config file.
     * @param type The type/side of the config file.
     * @param configSpecification The specification for the config file.
     * @param autosave Whether changes to the config should be automatically saved to file.
     */
    public static void buildConfigFile(String fileName, ModConfig.Type type, ForgeConfigSpec configSpecification, boolean autosave) {
        ModLoadingContext.get().registerConfig(type, configSpecification);
        loadConfigFile(FMLPaths.CONFIGDIR.get().resolve(fileName).toString(), configSpecification, autosave);
    }

    /**
     * Loads in a config file from the given path.
     *
     * @param path The path to the config file.
     * @param configSpecification The config file specification.
     * @param autosave Whether changes to the config should be automatically saved to file.
     */
    private static void loadConfigFile(String path, ForgeConfigSpec configSpecification, boolean autosave) {
        CommentedFileConfigBuilder commentedFileConfigBuilder = CommentedFileConfig.builder(new File(path));
        if (autosave) commentedFileConfigBuilder.sync().autosave().writingMode(WritingMode.REPLACE);
        CommentedFileConfig configFile = commentedFileConfigBuilder.build();

        configFile.load();
        configSpecification.setConfig(configFile);
    }
}
