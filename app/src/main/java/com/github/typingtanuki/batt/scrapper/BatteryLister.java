package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.Maker;
import com.github.typingtanuki.batt.battery.Source;
import com.github.typingtanuki.batt.exceptions.PageUnavailableException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.typingtanuki.batt.scrapper.CommonScrap.*;
import static com.github.typingtanuki.batt.utils.CachedHttp.http;

public final class BatteryLister {
    private BatteryLister() {
        super();
    }

    public static List<Battery> listBatteriesForMaker(Maker maker) throws IOException, PageUnavailableException {
        List<Battery> out = new LinkedList<>();
        List<Source> pages = listPages(maker.getSource());
        for (Source page : pages) {
            out.addAll(extractBatteriesFromPage(page));
        }
        return out;
    }

    private static List<Source> listPages(Source maker) throws IOException, PageUnavailableException {
        Document index = http("list", maker.getUrl());
        Elements counters = index.select("#productsListingBottomNumber strong");
        if (counters.isEmpty()) {
            return tryButtonList(index, maker);
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

    private static List<Source> tryButtonList(Document index, Source maker) {
        Elements links = index.select(".rightContents .r_resultInfo_center .M_pager li:not(.next) a");
        if (links.isEmpty()) {
            return Collections.singletonList(maker);
        }
        List<Source> out = new LinkedList<>();
        String rootUrl = maker.getUrl();
        out.add(maker);
        for (Element link : links) {
            String url = link.attr("href");
            if (!url.startsWith("http")) {
                Pattern a = Pattern.compile("^(https?://[^/]+)/.*$");
                Matcher matcher = a.matcher(rootUrl);
                if (!matcher.matches()) {
                    throw new IllegalStateException("Could not extract URL from: " + rootUrl);
                }
                url = matcher.group(1) + url;
            }
            out.add(new Source(url, maker.getScrapper()));
        }
        return out;
    }

    private static List<Battery> extractBatteriesFromPage(Source source) throws IOException, PageUnavailableException {
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
            batteries = index.select(".itemList__unit");
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
            if (target.isBlank()) {
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
