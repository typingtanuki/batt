package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Maker;

import java.io.IOException;
import java.util.List;

import static com.github.typingtanuki.batt.scrapper.MakerListReader.extractMakers;

public abstract class AbstractScrapper implements Scrapper {
    private final String rootUrl;

    public AbstractScrapper(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    @Override
    public List<Maker> makers() throws IOException {
        return extractMakers(rootUrl);
    }
}
