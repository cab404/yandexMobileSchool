package com.cab404.school_translator;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.cab404.school_translator.data.DaoMaster;
import com.cab404.school_translator.data.DaoSession;
import com.cab404.school_translator.net.YandexTranslateClient;
import com.cab404.school_translator.net.data.Langs;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseOpenHelper;

import java.util.Locale;

/**
 * Created at 03:18 on 20/03/17
 *
 * @author cab404
 */

public class App extends Application {
    private final static String TAG = "App";
    public static final String META_YANDEX_TRANSLATE_TOKEN = "com.cab404.YandexTranslateToken";

    private static App instance;

    public static App instance() {
        return instance;
    }

    private Langs translations;
    private DaoSession daoSession;

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public Langs getTranslations() {
        return translations;
    }

    public void setTranslations(Langs langs) {
        translations = langs;
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
            // I guess it's okay to die if translator cannot translate
            throw new RuntimeException("Failed to get YaTrToken :(", e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        client = new YandexTranslateClient(getYaTrToken());

        // For some unknown reason, all GreenDAO OpenHelpers from DaoMaster right now (28.03.17) are
        // spontaneously catching fire and/or exploding.
        // So we are making our own.
        daoSession = new DaoMaster(
                new DatabaseOpenHelper(this, "trans-history", DaoMaster.SCHEMA_VERSION) {
                    @Override
                    public void onCreate(Database db) {
                        super.onCreate(db);
                        DaoMaster.createAllTables(db, true);
                    }
                }.getWritableDb()
        ).newSession();
    }

}
