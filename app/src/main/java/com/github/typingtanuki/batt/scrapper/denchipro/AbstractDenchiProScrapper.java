package com.github.typingtanuki.batt.scrapper.denchipro;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.Maker;
import com.github.typingtanuki.batt.battery.Source;
import com.github.typingtanuki.batt.exceptions.PageUnavailableException;
import com.github.typingtanuki.batt.scrapper.AbstractScrapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.typingtanuki.batt.db.BatteryDB.*;
import static com.github.typingtanuki.batt.scrapper.BatteryDetailReader.batteryImages;
import static com.github.typingtanuki.batt.scrapper.CommonScrap.*;
import static com.github.typingtanuki.batt.utils.CachedHttp.http;
import static com.github.typingtanuki.batt.utils.Progress.BATTERY_BAD_PAGE;
import static com.github.typingtanuki.batt.utils.Progress.progress;

public abstract class AbstractDenchiProScrapper extends AbstractScrapper {
    public AbstractDenchiProScrapper(String rootUrl) {
        super(rootUrl);
    }

    @Override
    public List<Maker> makers() {
        return Collections.singletonList(new Maker("Denchipro", new Source(getRootUrl(), this)));
    }

    @Override
    protected List<Source> listPages(Source source) throws IOException, PageUnavailableException {
        Document index = http("list", source.getUrl());
        Elements pageButtons = index.select("a.page-numbers:not(.next)");
        Element lastPage = pageButtons.get(pageButtons.size() - 1);
        int totalPages = Integer.parseInt(lastPage.text());

        List<Source> out = new LinkedList<>();
        out.add(source);
        for (int i = 2; i <= totalPages; i++) {
            out.add(new Source(source.compact().getUrl() + "/page/" + i + "/", source.getScrapper()));
        }
        return out;
    }

    @Override
    protected List<Battery> extractBatteriesFromPage(Maker maker, Source source) throws IOException, PageUnavailableException {
        List<Battery> out = new LinkedList<>();
        Document index = http("list", source.getUrl());
        Elements batteries = index.select(".type-product");
        for (Element battery : batteries) {
            Elements link = battery.select("a");

            String target = link.attr("href");
            if (target.isBlank()) {
                continue;
            }
            Battery b = new Battery(
                    maker,
                    new Source(target, source.getScrapper()));
            out.add(b);
        }
        return out;
    }

    @Override
    public Battery extractBatteryDetails(Battery battery) throws IOException {
        Document page;
        try {
            page = http("battery", battery.getCurrentUrl());
        } catch (PageUnavailableException e) {
            progress(BATTERY_BAD_PAGE);
            return null;
        }

        Elements models = page.select(".sku_wrapper");
        String model = models.text().split(":")[1].strip();
        battery.setModel(model);
        battery.addPartNo(Collections.singleton(model));

        Elements descriptions = page.select(".product_title-product-details__short-description");
        String description = descriptions.text();
        if (description.isBlank()) {
            descriptions = page.select(".woocommerce-product-details__short-description");
            description = descriptions.text();
        }

        if (battery.getVolt() == null) {
            try {
                readVolt(description, battery);
            } catch (IllegalStateException e) {
                return null;
            }
        }
        if (battery.getWatt() == null) {
            readWatt(description, battery);
        }
        if (battery.getAmp() == null) {
            readAmp(description, battery);
        }

        Elements ids = page.select(".product_title");
        Pattern ID_EXTRACTOR = Pattern.compile("^.*\\s([^\\s]+)$");
        Matcher matcher = ID_EXTRACTOR.matcher(ids.text());
        if (matcher.matches()) {
            battery.addPartNo(Collections.singleton(matcher.group(1)));
        }

        if (battery.getPartNo().isEmpty()) {
            progress(BATTERY_BAD_PAGE);
            return null;
        }

        resolveModel(battery);
        resolveConnector(battery);
        resolveForm(battery);
        resolveSize(battery);
        batteryImages(page, battery);

        return battery;
    }
}
