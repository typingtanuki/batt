package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryType;
import com.github.typingtanuki.batt.exceptions.PageUnavailableException;
import com.github.typingtanuki.batt.utils.PageType;
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
    private static final Pattern TYPE_EXTRACTOR = Pattern.compile("^(?:内蔵セル|Type:)\\s*(.*[^\\s])\\s*$");
    private static final Pattern MODELS_EXTRACTOR = Pattern.compile("^(?:互換性:)\\s*(.*[^\\s])\\s*$");
    private static final Pattern IMAGE_EXTRACTOR = Pattern.compile(".*(images/large/[^\"]+\\.jpg).*");

    private BatteryDetailReader() {
        super();
    }

    public static Battery extractBatteryDetails(Battery battery) throws IOException {
        Document page;
        try {
            page = http(PageType.BATTERY, battery.getCurrentUrl());
        } catch (PageUnavailableException e) {
            progress(BATTERY_BAD_PAGE);
            return null;
        }

        Elements description = page.select("#product_desc_h4, .product_desc_h3, .product-info, #contents_spec");
        Elements brand = page.select(".product_desc_brand");
        Elements partNo = page.select(".product_desc_partno, .battery_number strong, #detailInfo .infoTable td");
        Elements models = page.select(".product_desc_model, table[summary='ページ掲載商品の対応機種表'] td");
        Elements property = page.select("#product_desc_property, .product_desc_property, .product_desc_h3, .product-info, #contents_spec");

        String descriptionText = description.text();
        battery.setBrand(brand.text());
        if (partNo != null) {
            battery.addPartNo(filteredSet(extractSet(partNo)));
        } else {
            battery.addPartNo(Collections.emptySet());
        }

        Elements parts = page.select(".blkaDescription span[style]");
        for(Element part:parts){
            String fullPart = part.text();
            for (String partName : fullPart.split(",")) {
                if (!partName.isBlank()) {
                    battery.addPartNo(Collections.singletonList(partName));
                }
            }
        }
        if (models != null) {
            battery.addPartNo(filteredSet(extractSet(models)));
        } else {
            battery.addPartNo(Collections.emptySet());
        }

        readSize(descriptionText, battery);

        Elements properties = property.select("li, tr");
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
                continue;
            }
            matcher = MODELS_EXTRACTOR.matcher(text);
            if (matcher.matches()) {
                String modelString = matcher.group(1).strip();
                if (!modelString.isBlank()) {
                    battery.addPartNo(Collections.singletonList(modelString));
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
        resolveSize(battery);
        batteryImages(page, battery);

        return battery;
    }

    public static void batteryImages(Document page, Battery battery) {
        if (battery.baseUri().contains("noteparts")) {
            return;
        }
        Elements scripts = page.select("script");
        for (Element script : scripts) {
            String html = script.html();
            if (html.contains("images/large")) {
                Matcher matcher = IMAGE_EXTRACTOR.matcher(html);
                if (matcher.find()) {
                    String url = battery.baseUri() + matcher.group(1);
                    battery.addImage(url);
                }
            }
        }
        Elements details = page.select("img.detail-images");
        for (Element detail : details) {
            String url = detail.attr("src");
            if (!url.startsWith("http")) {
                url = battery.baseUri() + url;
                battery.addImage(url);
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
