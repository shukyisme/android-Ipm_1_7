package me.kwik.data;

/**
 * Created by Farid Abu Salih on 13/03/16.
 * farid@kwik.me
 */
public class KwikLocale {

    public enum Language {English,Hebrew};

    private Language language;
    private String id;

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
