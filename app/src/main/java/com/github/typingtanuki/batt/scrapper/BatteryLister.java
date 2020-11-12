package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Battery;
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

    public static List<Battery> listBatteriesForMaker(String maker) throws IOException {
        List<Battery> out = new LinkedList<>();
        List<String> pages = listPages(maker);
        for (String page : pages) {
            out.addAll(extractBatteriesFromPage(page));
        }
        return out;
    }

    private static List<String> listPages(String maker) throws IOException {
        Document index = http("list", maker);
        Elements counters = index.select("#productsListingBottomNumber strong");
        if (counters.isEmpty()) {
            return Collections.singletonList(maker);
        }

        Iterator<Element> iter = counters.iterator();
        iter.next(); //Start of page
        double pageLength = Integer.parseInt(iter.next().text());
        double total = Integer.parseInt(iter.next().text());
        int pageCount = (int) Math.ceil(total / pageLength);

        List<String> out = new LinkedList<>();
        out.add(maker);
        for (int i = 2; i <= pageCount; i++) {
            out.add(maker + "?page=" + i + "&sort=20a&language=en");
        }
        return out;
    }

    private static List<Battery> extractBatteriesFromPage(String page) throws IOException {
        List<Battery> out = new LinkedList<>();
        Document index = http("list", page);
        Elements batteries = index.select(".productListing-data");
        if (batteries.isEmpty()) {
            Battery battery = new Battery(page);
            out.add(battery);
        }
        for (Element battery : batteries) {
            Elements descriptions = battery.select(".listingDescription");
            Elements link = battery.select("a");

            Element description = descriptions.first();
            if (description != null) {
                Battery b = new Battery(link.attr("href"));
                String text = description.text() + link.text();

                readVolt(text, b);
                readAmp(text, b);
                readWatt(text, b);

                if (b.isValid()) {
                    out.add(b);
                }
            }
        }
        return out;
    }
}
