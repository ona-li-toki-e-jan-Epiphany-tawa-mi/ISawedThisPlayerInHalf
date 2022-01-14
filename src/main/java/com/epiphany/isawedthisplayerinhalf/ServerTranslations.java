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
    private static final Pattern NUMERIC_VARIABLE_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
    private static final String SERVER_LANG_DIRECTORY = "/assets/swdthsplyrnhlf/lang/";

    private static final Map<String, String> localeMapping = new HashMap<>();

    public static void enable() {
        Locale serverLocale = ServerConfig.getServerLocale();
        String localeFilePath = SERVER_LANG_DIRECTORY + serverLocale.localeFilename + ".json";

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
}
