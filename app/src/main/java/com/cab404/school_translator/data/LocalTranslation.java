package com.cab404.school_translator.data;

import com.cab404.school_translator.net.data.Translation;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created at 14:38 on 20/03/17
 *
 * @author cab404
 */
@Entity(
        indexes = {@Index("inFavourites, timestamp ASC")}
)
public class LocalTranslation {
    public boolean inFavourites;
    @NotNull
    public String translatedText;
    @NotNull
    public String sourceText;
    public String lang;

    @Id(autoincrement = true)
    public Long id = null;

    public long timestamp;

    @Override
    public String toString() {
        return "LocalTranslation{" +
                "translatedText='" + translatedText + '\'' +
                ", inFavourites=" + inFavourites +
                ", sourceText='" + sourceText + '\'' +
                ", lang='" + lang + '\'' +
                ", id=" + id +
                ", timestamp=" + timestamp +
                '}';
    }

    public boolean getInFavourites() {
        return this.inFavourites;
    }

    public void setInFavourites(boolean inFavourites) {
        this.inFavourites = inFavourites;
    }

    public String getTranslatedText() {
        return this.translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getSourceText() {
        return this.sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getLang() {
        return this.lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public LocalTranslation(String sourceText, Translation translation) {
        this.sourceText = sourceText;
        this.inFavourites = false;
        this.lang = translation.lang;
        this.timestamp = System.currentTimeMillis();
        this.translatedText = translation.text.get(0);
    }

    @Generated(hash = 248769681)
    public LocalTranslation(boolean inFavourites, @NotNull String translatedText,
            @NotNull String sourceText, String lang, Long id, long timestamp) {
        this.inFavourites = inFavourites;
        this.translatedText = translatedText;
        this.sourceText = sourceText;
        this.lang = lang;
        this.id = id;
        this.timestamp = timestamp;
    }

    @Generated(hash = 969061138)
    public LocalTranslation() {
    }

}
