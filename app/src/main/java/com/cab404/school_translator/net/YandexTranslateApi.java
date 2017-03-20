package com.cab404.school_translator.net;

import com.cab404.school_translator.net.data.Langs;
import com.cab404.school_translator.net.data.Translation;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created at 10:36 on 20/03/17
 *
 * @author cab404
 */
public interface YandexTranslateApi {

    @GET("/api/v1.5/tr.json/getLangs")
    Observable<Langs> getLanguages(@Query("ui") String lang);

    @FormUrlEncoded
    @POST("/api/v1.5/tr.json/translate")
    Observable<Translation> translate(
            @Field("text") String text,
            @Field("lang") String lang
    );

    @FormUrlEncoded
    @POST("/api/v1.5/tr.json/detect")
    Observable<Translation> detect(
            @Field("text") String text
    );

}
