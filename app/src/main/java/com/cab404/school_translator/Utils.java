package com.cab404.school_translator;

import android.content.Context;

import java.util.Locale;

/**
 * Created at 03:05 on 29/03/17
 *
 * @author cab404
 */
public class Utils {

    public static String getUILanguage(Context context) {
        // Recognizing UI language
        final Locale locale = context.getResources().getConfiguration().locale;

        // On fail we default to english
        String localeTag = "en";

        // Later on we can add here more translations
        if (new Locale("ru").getLanguage().equals(locale.getLanguage()))
            localeTag = "ru";

        return localeTag;
    }
}
