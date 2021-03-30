package com.github.typingtanuki.batt.scrapper.denchipro;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.Maker;
import com.github.typingtanuki.batt.battery.MakerName;
import com.github.typingtanuki.batt.battery.Source;
import com.github.typingtanuki.batt.exceptions.NoPartException;
import com.github.typingtanuki.batt.exceptions.PageUnavailableException;
import com.github.typingtanuki.batt.scrapper.AbstractScrapper;
import com.github.typingtanuki.batt.utils.PageType;
import com.github.typingtanuki.batt.utils.Progress;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

import static com.github.typingtanuki.batt.db.BatteryDB.*;
import static com.github.typingtanuki.batt.scrapper.BatteryDetailReader.batteryImages;
import static com.github.typingtanuki.batt.scrapper.CommonScrap.*;
import static com.github.typingtanuki.batt.utils.CachedHttp.http;
import static com.github.typingtanuki.batt.utils.Progress.BATTERY_BAD_PAGE;
import static com.github.typingtanuki.batt.utils.Progress.progress;

public abstract class AbstractDenchiProScrapper extends AbstractScrapper {
    private static final Set<String> VISITED = new HashSet<>();

    public AbstractDenchiProScrapper(String rootUrl) {
        super(rootUrl);
    }

    private static MakerName makerFor(String label) {
        if (!label.contains("対応")) {
            throw new IllegalStateException("Unparseable entry " + label);
        }
        String maker = label.split("対応", 2)[1].strip()
                .split("\\s", -1)[0]
                .split("]", -1)[0]
                .replaceAll("\\[", "").strip();

        MakerName found;
        try {
            found = MakerName.parse(maker);
        } catch (IllegalArgumentException e) {
            found = MakerName.OTHER;
        }
        return found;
    }

    @Override
    public List<Maker> makers() throws IOException, PageUnavailableException {
        Map<MakerName, Set<Source>> detected = new EnumMap<>(MakerName.class);

        List<Source> sources = listAllPages(new Source(getRootUrl(), this));
        for (Source source : sources) {
            progress(Progress.BATTERY_MATCH);
            Document index = http(PageType.LIST, source.getUrl());
            for (Element entry : index.select(".woocommerce-loop-product__title")) {
                MakerName found = makerFor(entry.text());
                Set<Source> soFar = detected.getOrDefault(found, new HashSet<>());
                soFar.add(source);
                detected.put(found, soFar);
            }
        }

        List<Maker> out = new ArrayList<>(detected.size());
        for (Map.Entry<MakerName, Set<Source>> entry : detected.entrySet()) {
            out.add(new Maker(entry.getKey().name(), new ArrayList<>(entry.getValue())));
        }
        return out;
    }

    @Override
    protected List<Source> listPages(Source source) {
        // We are already listing the pages to detect the makers
        return Collections.singletonList(source);
    }

    private List<Source> listAllPages(Source source) throws IOException, PageUnavailableException {
        Document index = http(PageType.LIST, source.getUrl());
        Elements pageButtons = index.select("a.page-numbers:not(.next)");
        Element lastPage = pageButtons.get(pageButtons.size() - 1);
        int totalPages = Integer.parseInt(lastPage.text());

        List<Source> out = new ArrayList<>();
        out.add(source);
        for (int i = 2; i <= totalPages; i++) {
            out.add(new Source(source.compact().getUrl() + "/page/" + i + "/", source.getScrapper()));
        }
        return out;
    }

    @Override
    protected List<Battery> extractBatteriesFromPage(Maker maker, Source source) throws IOException, PageUnavailableException {
        List<Battery> out = new ArrayList<>();
        Document index;
        try {
            index = http(PageType.LIST, source.getUrl());
        } catch (PageUnavailableException e) {
            progress(BATTERY_BAD_PAGE);
            return out;
        }
        Elements batteries = index.select(".type-product");
        for (Element battery : batteries) {
            MakerName found = makerFor(battery.text());
            if (found != maker.getMakerName()) {
                continue;
            }

            Elements link = battery.select("a");

            String target = link.attr("href");
            if (target.isBlank()) {
                continue;
            }
            if (VISITED.add(target)) {
                Battery b = new Battery(
                        maker,
                        new Source(target, source.getScrapper()));
                out.add(b);
            }
        }
        return out;
    }

    @Override
    public Battery extractBatteryDetails(Battery battery) throws IOException, NoPartException {
        Document page;
        try {
            page = http(PageType.BATTERY, battery.getCurrentUrl());
        } catch (PageUnavailableException e) {
            progress(BATTERY_BAD_PAGE);
            return null;
        }

        String model = page.title();
        if (!model.contains("対応")) {
            throw new IllegalStateException("Can not extract battery detail from " + model);
        }
        model = model.split("対応", -1)[1].split(" - denchipro", -1)[0].strip();
        if (model.contains("]")) {
            model = model.split("]", -1)[1].strip().split("\\s", 2)[1].strip();
        } else if (model.contains(" ")) {
            model = model.split("\\s", 2)[1].strip();
        }
        String[] parts = model.split(",", -1);
        battery.addPartNo(Arrays.asList(parts));

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
                battery.setVolt(-1d);
            }
        }
        if (battery.getWatt() == null) {
            readWatt(description, battery);
        }
        if (battery.getAmp() == null) {
            readAmp(description, battery);
        }

        if (battery.getPartNo().isEmpty()) {
            progress(BATTERY_BAD_PAGE);
            return null;
        }

        resolveConnector(battery);
        resolveForm(battery);
        resolveSize(battery);
        batteryImages(page, battery);

        return battery;
    }
}
