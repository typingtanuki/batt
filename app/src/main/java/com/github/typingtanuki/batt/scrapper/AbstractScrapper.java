package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.Maker;
import com.github.typingtanuki.batt.battery.Source;
import com.github.typingtanuki.batt.exceptions.NoPartException;
import com.github.typingtanuki.batt.exceptions.PageUnavailableException;
import com.github.typingtanuki.batt.utils.PageType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

import static com.github.typingtanuki.batt.scrapper.CommonScrap.*;
import static com.github.typingtanuki.batt.scrapper.MakerListReader.extractMakers;
import static com.github.typingtanuki.batt.utils.CachedHttp.http;
import static com.github.typingtanuki.batt.utils.UrlUtils.absoluteURL;

public abstract class AbstractScrapper implements Scrapper {
    private final String rootUrl;

    public AbstractScrapper(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    private static List<Source> tryButtonList(Document index, Source source) {
        Elements links = index.select(".rightContents .r_resultInfo_center .M_pager li:not(.next) a");
        if (links.isEmpty()) {
            return Collections.singletonList(source);
        }
        List<Source> out = new LinkedList<>();
        String rootUrl = source.getUrl();
        out.add(source);
        for (Element link : links) {
            String url = absoluteURL(link, rootUrl);
            out.add(new Source(url, source.getScrapper()));
        }
        return out;
    }

    @Override
    public List<Maker> makers() throws IOException, PageUnavailableException {
        return extractMakers(this, rootUrl);
    }

    public String getRootUrl() {
        return rootUrl;
    }

    @Override
    public List<Battery> listBatteries(Maker maker) throws IOException, PageUnavailableException {
        List<Battery> out = new LinkedList<>();
        List<Source> pages = listPages(maker.getSources());
        for (Source page : pages) {
            out.addAll(extractBatteriesFromPage(maker, page));
        }
        return out;
    }

    protected List<Source> listPages(List<Source> sources) throws IOException {
        Set<Source> out = new HashSet<>();
        for (Source source : sources) {
            out.addAll(listPages(source));
        }
        return new ArrayList<>(out);
    }

    protected List<Source> listPages(Source source) throws IOException {
        Document index;
        try {
            index = http(PageType.LIST, source.getUrl());
        } catch (PageUnavailableException e) {
            return Collections.emptyList();
        }

        Elements counters = index.select("#productsListingBottomNumber strong");
        if (counters.isEmpty()) {
            return tryButtonList(index, source);
        }

        Iterator<Element> iter = counters.iterator();
        iter.next(); //Start of page
        double pageLength = Integer.parseInt(iter.next().text());
        double total = Integer.parseInt(iter.next().text());
        int pageCount = (int) Math.min(Math.ceil(total / pageLength), 50);

        List<Source> out = new LinkedList<>();
        out.add(source);
        for (int i = 2; i <= pageCount; i++) {
            out.add(new Source(
                    source.compact().getUrl() + "?page=" + i + "&sort=20a&language=en",
                    source.getScrapper()));
        }
        return out;
    }

    protected List<Battery> extractBatteriesFromPage(Maker maker, Source source) throws IOException, PageUnavailableException {
        List<Battery> out = new LinkedList<>();
        Document index = http(PageType.LIST, source.getUrl());
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
            Battery battery = new Battery(maker, source);
            out.add(battery);
        }

        Set<String> visited = new HashSet<>();
        for (Element battery : batteries) {
            Elements descriptions = battery.select(".listingDescription");
            Elements link = battery.select("a");

            Element description = descriptions.first();
            String target = link.attr("href");
            if (target.isBlank() || visited.contains(target)) {
                continue;
            }
            visited.add(target);
            Battery b = new Battery(
                    maker,
                    new Source(
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

    @Override
    public Battery extractBatteryDetails(Battery battery) throws IOException, NoPartException {
        return BatteryDetailReader.extractBatteryDetails(battery);
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
