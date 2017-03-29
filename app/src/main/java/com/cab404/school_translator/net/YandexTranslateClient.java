package com.cab404.school_translator.net;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
                .addInterceptor(chain -> {
                            Request request = chain.request();
                            try {
                                return chain.proceed(
                                        request.newBuilder().url(
                                                request.url()
                                                        .newBuilder()
                                                        .addQueryParameter("key", apiKey)
                                                        .build()
                                        ).build()
                                );
                            } catch (Exception e) {
                                throw new IOException(e);
                            }
                        }
                )
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
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
