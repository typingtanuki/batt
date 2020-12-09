package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.typingtanuki.batt.db.BatteryDB.*;
import static com.github.typingtanuki.batt.scrapper.CommonScrap.*;
import static com.github.typingtanuki.batt.utils.CachedHttp.http;
import static com.github.typingtanuki.batt.utils.Progress.BATTERY_BAD_PAGE;
import static com.github.typingtanuki.batt.utils.Progress.progress;

public final class BatteryDetailReader {
    private static final Pattern TYPE_EXTRACTOR = Pattern.compile("^Type:\\s*(.*[^\\s])\\s*$");
    private static final Pattern IMAGE_EXTRACTOR = Pattern.compile(".*(images/large/[^\"]+\\.jpg).*");

    private BatteryDetailReader() {
        super();
    }

    public static Battery extractBatteryDetails(Battery battery) throws IOException {
        Document page = http("battery", battery.getCurrentUrl());
        Elements description = page.select("#product_desc_h4, .product_desc_h3, .product-info");
        Elements brand = page.select(".product_desc_brand");
        Elements partNo = page.select(".product_desc_partno, .battery_number strong");
        Elements models = page.select(".product_desc_model");
        Elements property = page.select("#product_desc_property, .product_desc_property, .product_desc_h3, .product-info");

        String descriptionText = description.text();
        battery.setBrand(brand.text());
        if (partNo != null) {
            battery.addPartNo(filteredSet(extractSet(partNo)));
        } else {
            battery.addPartNo(Collections.emptySet());
        }

        Elements parts = page.select(".blkaDescription span[style]");
        if (!parts.isEmpty()) {
            String fullDescription = parts.text();
            for (String part : fullDescription.split(",")) {
                if (!part.isBlank()) {
                    battery.addPartNo(Collections.singletonList(part));
                }
            }
        }
        if (models != null) {
            battery.addPartNo(filteredSet(extractSet(models)));
        } else {
            battery.addPartNo(Collections.emptySet());
        }

        readCell(descriptionText, battery);
        readSize(descriptionText, battery);

        Elements properties = property.select("li");
        for (Element p : properties) {
            String text = p.text();
            Matcher matcher = TYPE_EXTRACTOR.matcher(text);
            if (matcher.matches()) {
                String type = matcher.group(1)
                        .toUpperCase(Locale.ENGLISH)
                        .replaceAll("-", "_")
                        .replaceAll("RECHARGEABLE", "")
                        .replaceAll("BATTERY", "")
                        .replaceAll("REPLACEMENT", "")
                        .replaceAll("ORIGINAL", "")
                        .strip();
                if (type.isBlank()) {
                    battery.setType(BatteryType.UNKNOWN);
                } else {
                    battery.setType(BatteryType.parse(type));
                }
            }
        }

        String allProperties = properties.text();
        if (allProperties.isBlank()) {
            progress(BATTERY_BAD_PAGE);
            return null;
        }

        if (battery.getVolt() == null) {
            readVolt(allProperties, battery);
        }
        if (battery.getWatt() == null) {
            readWatt(allProperties, battery);
        }
        if (battery.getAmp() == null) {
            readAmp(allProperties, battery);
        }

        if (battery.getPartNo().isEmpty()) {
            progress(BATTERY_BAD_PAGE);
            return null;
        }

        resolveModel(battery);
        resolveConnector(battery);
        resolveForm(battery);
        batteryImages(page, battery);

        return battery;
    }

    public static void batteryImages(Document page, Battery battery) {
        Elements scripts = page.select("script");
        for (Element script : scripts) {
            String html = script.html();
            if (html.contains("images/large")) {
                Matcher matcher = IMAGE_EXTRACTOR.matcher(html);
                if (matcher.find()) {
                    String url = page.baseUri() + matcher.group(1);
                    battery.addImage(url);
                }
            }
        }
    }

    private static String[] extractSet(Elements elements) {
        List<String> out = new ArrayList<>();
        for (Element element : elements) {
            out.addAll(Arrays.asList(element.text().split(",")));
        }
        return out.toArray(new String[0]);
    }

    private static Set<String> filteredSet(String[] strings) {
        Set<String> out = new LinkedHashSet<>();
        for (String s : strings) {
            out.add(s.split("\\(")[0].strip());
        }
        return out;
    }
}
