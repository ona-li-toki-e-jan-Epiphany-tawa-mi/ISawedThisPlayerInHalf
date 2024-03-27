package com.epiphany.isawedthisplayerinhalf.config;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

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
 * Server configuration file and data.
 */
@OnlyIn(Dist.DEDICATED_SERVER)
public class ServerConfig {
    public static final Locale DEFAULT_LOCALE = Locale.EN_US;

    private static ForgeConfigSpec.EnumValue<Locale> serverLocale;
    private static ForgeConfigSpec.IntValue kickWarningCount;

    public static void enable() {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();

        serverLocale = configBuilder.defineEnum("language", Locale.EN_US);

        configBuilder.comment(
                " (en-US) Automatically kicks players that send invalid offset packets after the given number of occurrences. Set to -1 to disable.",
                " (ru-RU) Автоматически выгоняет игроков, которые отправили неправильные пакеты смещений больше заданного раз. Установите на -1, чтобы выключить.",
                " (tok (eo-UY)) jan li pana e ma ante ike lon tenpo pi nanpa pana la li weka e ona. o pana e -1 tawa ni: ni li weka ala e jan."
        );
        kickWarningCount = configBuilder.defineInRange("kickWarningCount", 2, -1, Integer.MAX_VALUE);

        ConfigCommon.buildConfigFile("isawedthisplayerinhalf-server.toml", ModConfig.Type.SERVER, configBuilder.build(), false);
    }


    /**
     * Gets the currently set server locale for displaying messages.
     * @return The currently set server locale.
     */
    public static Locale getServerLocale() {
        return serverLocale.get();
    }

    /**
     * @return Whether to kick a player if they send invalid offset information more times than permitted.
     */
    public static boolean shouldKickOnInvalid() {
        return kickWarningCount.get() != -1;
    }

    /**
     * @return The number of times a player can send invalid offsets before being kicked.
     */
    public static int getKickWarningCount() {
        return kickWarningCount.get();
    }
}
