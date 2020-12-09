package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.Maker;
import com.github.typingtanuki.batt.battery.Source;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.github.typingtanuki.batt.scrapper.CommonScrap.*;
import static com.github.typingtanuki.batt.utils.CachedHttp.http;

public final class BatteryLister {
    private BatteryLister() {
        super();
    }

    public static List<Battery> listBatteriesForMaker(Maker maker) throws IOException {
        List<Battery> out = new LinkedList<>();
        List<Source> pages = listPages(maker.getSource());
        for (Source page : pages) {
            out.addAll(extractBatteriesFromPage(page));
        }
        return out;
    }

    private static List<Source> listPages(Source maker) throws IOException {
        Document index = http("list", maker.getUrl());
        Elements counters = index.select("#productsListingBottomNumber strong");
        if (counters.isEmpty()) {
            return Collections.singletonList(maker);
        }

        Iterator<Element> iter = counters.iterator();
        iter.next(); //Start of page
        double pageLength = Integer.parseInt(iter.next().text());
        double total = Integer.parseInt(iter.next().text());
        int pageCount = (int) Math.ceil(total / pageLength);

        List<Source> out = new LinkedList<>();
        out.add(maker);
        for (int i = 2; i <= pageCount; i++) {
            out.add(new Source(
                    maker.compact().getUrl() + "?page=" + i + "&sort=20a&language=en",
                    maker.getScrapper()));
        }
        return out;
    }

    private static List<Battery> extractBatteriesFromPage(Source source) throws IOException {
        List<Battery> out = new LinkedList<>();
        Document index = http("list", source.getUrl());
        Elements batteries = index.select(".productListing-data");
        if (batteries.isEmpty()) {
            batteries = index.select(".laptop-brand-list");
        }
        if (batteries.isEmpty()) {
            batteries = index.select(".battery-list");
        }
        if (batteries.isEmpty()) {
            Battery battery = new Battery(source);
            out.add(battery);
        }
        for (Element battery : batteries) {
            Elements descriptions = battery.select(".listingDescription");
            Elements link = battery.select("a");

            Element description = descriptions.first();
            String target = link.attr("href");
            if(target.isBlank()){
                continue;
            }
            Battery b = new Battery(new Source(
                    target,
                    source.getScrapper()));

            if (description != null) {
                String text = description.text() + link.text();

                readVolt(text, b);
                readAmp(text, b);
                readWatt(text, b);

                if (b.isValid()) {
                    out.add(b);
                }
            } else {
                out.add(b);
            }
        }
        return out;
    }
}
