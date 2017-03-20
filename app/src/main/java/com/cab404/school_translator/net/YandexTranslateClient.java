package com.cab404.school_translator.net;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created at 11:12 on 20/03/17
 *
 * @author cab404
 */
public class YandexTranslateClient {
    private final static String TAG = "YandexTranslateClient";
    public static final String YANDEX_TRANSLATE_BACKEND = "https://translate.yandex.net";

    private YandexTranslateApi api;

    public YandexTranslateApi getApi() {
        return api;
    }

    public YandexTranslateClient(final String apiKey) {
        final OkHttpClient client = new OkHttpClient.Builder()
                // Interceptor for adding api token
                .addInterceptor(chain -> chain.proceed(
                        chain.request()
                                .newBuilder()
                                .url(
                                        chain.request().url()
                                                .newBuilder()
                                                .addQueryParameter("key", apiKey)
                                                .build()
                                ).build()
                        )
                )
                .addInterceptor(new HttpLoggingInterceptor())
                .build();
        final Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(YANDEX_TRANSLATE_BACKEND)
                .client(client)
                .build();
        api = retrofit.create(YandexTranslateApi.class);
    }
}
