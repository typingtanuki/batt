package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Maker;
import com.github.typingtanuki.batt.exceptions.PageUnavailableException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.github.typingtanuki.batt.scrapper.MakerListReader.extractMakers;

public abstract class AbstractScrapper implements Scrapper {
    private final String rootUrl;

    public AbstractScrapper(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    @Override
    public List<Maker> makers() throws IOException, PageUnavailableException {
        return extractMakers(this, rootUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractScrapper that = (AbstractScrapper) o;
        return Objects.equals(rootUrl, that.rootUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootUrl);
    }
}
