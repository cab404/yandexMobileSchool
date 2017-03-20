package com.cab404.school_translator;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.cab404.school_translator.net.YandexTranslateClient;
import com.cab404.school_translator.net.data.Langs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created at 03:18 on 20/03/17
 *
 * @author cab404
 */
public class App extends Application {
    private final static String TAG = "App";
    public static final String META_YANDEX_TRANSLATE_TOKEN = "com.cab404.YandexTranslateToken";

    private Langs translations;


    public Langs getTranslations() {
        return translations;
    }

    public void setTranslations(Langs langs) {
        translations = langs;
    }

    public String getUILanguage() {
        // Recognizing UI language
        final Locale locale = getResources().getConfiguration().locale;

        // On fail we default to english
        String localeTag = "en";

        // Later on we can add here more translations
        if (new Locale("ru").getLanguage().equals(locale.getLanguage()))
            localeTag = "ru";

        return localeTag;
    }

    public final static Handler handler = new Handler(Looper.getMainLooper());
    private YandexTranslateClient client;

    public YandexTranslateClient getClient() {
        return client;
    }

    private String getYaTrToken() {
        try {
            final Bundle metaData = getPackageManager()
                    .getApplicationInfo(
                            getPackageName(),
                            PackageManager.GET_META_DATA
                    )
                    .metaData;
            return metaData.getString(META_YANDEX_TRANSLATE_TOKEN);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Failed to get YaTrToken :(", e);
        }
    }

    public static App get(Context context) {
        return ((App) context.getApplicationContext());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        client = new YandexTranslateClient(getYaTrToken());
    }

}
