package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.config.Locale;
import com.epiphany.isawedthisplayerinhalf.config.ServerConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Handles the loading and localization of server-side translations.
 */
@OnlyIn(Dist.DEDICATED_SERVER)
public class ServerTranslations {
    // Copied from LanguageMap.
    private static final Pattern NUMERIC_VARIABLE_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
    private static final String SERVER_LANG_DIRECTORY = "/assets/swdthsplyrnhlf/serverLang/";

    private static final Map<String, String> localeMapping = new HashMap<>();

    public static void enable() {
        Locale serverLocale = ServerConfig.getServerLocale();

        if (serverLocale != ServerConfig.DEFAULT_LOCALE)
            loadLocaleData(ServerConfig.DEFAULT_LOCALE);
        loadLocaleData(serverLocale);
    }

    /**
     * Loads local localization mappings into the locale map from the sever language directory.
     * @param locale The locale to load.
     */
    private static void loadLocaleData(Locale locale) {
        String localeFilePath = SERVER_LANG_DIRECTORY + locale.localeFilename + ".json";

        // Copied from LanguageMap.
        // Loads data for selected locale from file.
        try {
            InputStream inputStream = ServerTranslations.class.getResourceAsStream(localeFilePath);
            if (inputStream == null) throw new IOException();

            JsonElement jsonElement = (new Gson()).fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonElement.class);
            JsonObject jsonObject = JSONUtils.getJsonObject(jsonElement, "strings");

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String localization = NUMERIC_VARIABLE_PATTERN.matcher(JSONUtils.getString(entry.getValue(), entry.getKey())).replaceAll("%$1s");
                localeMapping.put(entry.getKey(), localization);
            }

        } catch (JsonParseException | IOException exception) {
            ISawedThisPlayerInHalf.LOGGER.error("Couldn't read locale data from " + localeFilePath, exception);
        }
    }



    /**
     * Translates the key into the corresponding translation for the current locale.
     *
     * @param key The key to the translation.
     *
     * @return The translation for the current locale.
     */
    public static String translateKey(String key) {
        String translation = localeMapping.get(key);
        return translation != null ? translation : key;
    }

    /**
     * Translates the key into the corresponding translation for the current locale.
     * Formats the resulting string using {@link String#format}, passing along the given arguments to it.
     *
     * @param key The key to the translation.
     * @param args The arguments for String.format().
     *
     * @return The formatted translation for the current locale.
     */
    public static String translateAndFormatKey(String key, Object... args) {
        return String.format(translateKey(key), args);
    }
}
