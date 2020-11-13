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

import static com.github.typingtanuki.batt.db.BatteryDB.resolveConnector;
import static com.github.typingtanuki.batt.db.BatteryDB.resolveForm;
import static com.github.typingtanuki.batt.scrapper.CommonScrap.*;
import static com.github.typingtanuki.batt.utils.CachedHttp.*;
import static com.github.typingtanuki.batt.utils.Progress.progress;

public final class BatteryDetailReader {
    private BatteryDetailReader() {
        super();
    }

    public static void extractBatteryDetails(Battery battery) throws IOException {
        Document page = http("battery", battery.getUrl());
        Element description = page.getElementById("product_desc_h4");
        Element brand = page.getElementsByClass("product_desc_brand").first();
        Element partNo = page.getElementsByClass("product_desc_partno").first();
        Element models = page.getElementsByClass("product_desc_model").first();
        Element property = page.getElementById("product_desc_property");
        Element detail = page.getElementById("productDetailsList");
        String descriptionText = description.text();

        battery.setBrand(brand.text());
        if (partNo != null) {
            battery.setPartNo(filteredSet(partNo.text().split(", ")));
        } else {
            battery.setPartNo(Collections.emptySet());
        }
        if (models != null) {
            battery.setModels(filteredSet(models.text().split(", ")));
        } else {
            battery.setModels(Collections.emptySet());
        }

        readCell(descriptionText, battery);

        Elements properties = property.select("li");
        for (Element p : properties) {
            String text = p.text();
            if (text.contains("Type:")) {
                String type = text.split(":")[1]
                        .strip()
                        .toUpperCase(Locale.ENGLISH)
                        .replaceAll("-", "_");
                battery.setType(BatteryType.valueOf(type));
            }
        }

        String allProperties = properties.text();
        if (battery.getVolt() == null) {
            readVolt(allProperties, battery);
        }
        if (battery.getWatt() == null) {
            readWatt(allProperties, battery);
        }
        if (battery.getAmp() == null) {
            readAmp(allProperties, battery);
        }

        String details = detail.select("li").first().html();
        readModel(details, battery);

        resolveConnector(battery);
        resolveForm(battery);

        if (battery.isValid()) {
            progress("|");
            downloadBatteryImages(page, battery);
        } else {
            progress(".");
            deleteBatteryImages(page, battery);
        }
    }

    private static void deleteBatteryImages(Document page, Battery battery) throws IOException {
        for (String image : batteryImages(page)) {
            deleteDownload(battery.getModel(), image);
        }
    }

    private static void downloadBatteryImages(Document page, Battery battery) throws IOException {
        for (String image : batteryImages(page)) {
            download(battery.getModel(), image);
        }
    }

    private static Set<String> batteryImages(Document page) {
        Set<String> out = new HashSet<>();
        Elements scripts = page.select("script");
        for (Element script : scripts) {
            String html = script.html();
            if (html.contains("images/large")) {
                Matcher matcher = REGEX.matcher(html);
                if (matcher.find()) {
                    String url = page.baseUri() + matcher.group(1);
                    out.add(url);
                }
            }
        }
        return out;
    }

    private static final Pattern REGEX = Pattern.compile(".*(images/large/[^\"]+\\.jpg).*");

    private static Set<String> filteredSet(String[] strings) {
        Set<String> out = new LinkedHashSet<>();
        for (String s : strings) {
            out.add(s.split("\\(")[0].strip());
        }
        return out;
    }
}
