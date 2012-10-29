package com.bencvt.minecraft.buildregion.lang;

import java.io.IOException;
import java.util.IllegalFormatException;
import java.util.Properties;

import net.minecraft.src.StringTranslate;

public abstract class LocalizedString {
    private static String lang;
    private static Properties table;

    /**
     * Attempt to translate a string, optionally with String.format arguments.
     * <p>
     * If any of the String.format arguments is an enum value, it is
     * translated as well.
     * 
     * @return a non-null string, translated and formatted if possible.
     */
    public static String translate(String key, Object ... args) {
        String result = lookup(key);
        if (result != null) {
            if (result.startsWith("$MC:")) {
                // The property value is specifying a key from the Minecraft
                // translations. Perform another lookup.
                result = StringTranslate.getInstance().translateKeyFormat(result.substring(4), args);
            } else if (args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null) {
                        args[i] = "null";
                    } else if (args[i] instanceof Enum) {
                        args[i] = translate((Enum) args[i]);
                    }
                }
                try {
                    result = String.format(result, args);
                } catch (IllegalFormatException e) {
                    // leave result as it was
                }
            }
        }
        return result == null ? key : result;
    }

    /**
     * Attempt to translate an enum value.
     * @return a non-null string, translated if possible.
     */
    public static String translate(Enum e) {
        if (e == null) {
            return "null";
        }
        String propName = "enum." + e.getClass().getSimpleName().toLowerCase();
        if (lookup(propName) != null) {
            return translate(propName + "." + e.toString().toLowerCase());
        } else {
            return e.toString().toLowerCase();
        }
    }

    /**
     * Look up a localized string from the lazily-instantiated lang table.
     * @return a string from the lang table, or null if there is no entry in
     *         either the current lang or the default lang.
     */
    private static String lookup(String key) {
        // Get the user's selected language from Minecraft.
        String newLang = StringTranslate.getInstance().getCurrentLanguage();
        if (lang == null || !lang.equals(newLang)) {
            lang = newLang;
            // Ensure the default language is loaded.
            if (table == null) {
                Properties defaultProps = new Properties();
                try {
                    defaultProps.load(LocalizedString.class.getResourceAsStream("en_US.properties"));
                } catch (IOException e) {
                    throw new RuntimeException("unable to load default lang file", e);
                }
                table = new Properties(defaultProps);
            }
            // Load translation of the selected language.
            table.clear();
            try {
                table.load(LocalizedString.class.getResourceAsStream(lang + ".properties"));
            } catch (Exception e) {
                // No valid translation available for the selected language.
                // Table lookups will have to rely on the default properties.
                table.clear();
            }
        }
        return table.getProperty(key);
    }
}
