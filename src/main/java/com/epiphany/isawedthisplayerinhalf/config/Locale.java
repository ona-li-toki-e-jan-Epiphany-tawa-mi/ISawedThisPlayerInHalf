package com.epiphany.isawedthisplayerinhalf.config;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Represents a locale, the mapping from keys to translations.
 */
@OnlyIn(Dist.DEDICATED_SERVER)
public enum Locale {
    EN_US("en_us"),
    RU_RU("ru_ru"),
    TOK("tok");

    public final String localeFilename;

    /**
     * Creates a new locale.
     * @param filename The name of the file (without extension) that stores the translations in assets/swdthsplyrnhlf/serverLang/.
     */
    Locale(String filename) {
        this.localeFilename = filename;
    }
}
