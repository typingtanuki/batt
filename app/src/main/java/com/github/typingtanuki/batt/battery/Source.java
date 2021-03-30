package com.github.typingtanuki.batt.battery;

import com.github.typingtanuki.batt.scrapper.Scrapper;

import java.util.Objects;

public class Source {
    private final String url;
    private final Scrapper scrapper;

    public Source(String url, Scrapper scrapper) {
        super();

        this.url = url;
        this.scrapper = scrapper;
    }

    public String getUrl() {
        return url;
    }

    public Scrapper getScrapper() {
        return scrapper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Source)) return false;
        Source source = (Source) o;
        return Objects.equals(url, source.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    public String name() {
        return scrapper.name();
    }

    public Source compact() {
        if (!url.contains("?")) {
            return this;
        }
        return new Source(url.split("\\?", -1)[0], scrapper);
    }
}
