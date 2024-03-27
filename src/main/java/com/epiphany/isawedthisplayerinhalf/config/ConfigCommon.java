package com.epiphany.isawedthisplayerinhalf.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfigBuilder;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

/*
 * MIT License
 *
 * Copyright (c) 2022 ona-li-toki-e-jan-Epiphany-tawa-mi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/

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
