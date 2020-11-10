package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import static com.github.typingtanuki.batt.battery.BatteryConnectorResolver.resolveConnector;
import static com.github.typingtanuki.batt.battery.BatteryFormResolver.resolveForm;
import static com.github.typingtanuki.batt.scrapper.CommonScrap.*;
import static com.github.typingtanuki.batt.utils.CachedHttp.http;

public final class BatteryDetailReader {
    private BatteryDetailReader() {
        super();
    }

    public static void extractBatteryDetails(Battery battery) throws IOException {
        Document batt = http(battery.getUrl());
        Element description = batt.getElementById("product_desc_h4");
        Element brand = batt.getElementsByClass("product_desc_brand").first();
        Element partNo = batt.getElementsByClass("product_desc_partno").first();
        Element models = batt.getElementsByClass("product_desc_model").first();
        Element property = batt.getElementById("product_desc_property");
        Element detail = batt.getElementById("productDetailsList");
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

        readDescription(descriptionText, battery);

        if (battery.getWatt() == null) {
            readWatt(property.text(), battery);
        }

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

        String details = detail.select("li").first().html();
        readModel(details, battery);

        resolveConnector(battery);
        resolveForm(battery);
    }

    private static Set<String> filteredSet(String[] strings) {
        Set<String> out = new LinkedHashSet<>();
        for (String s : strings) {
            out.add(s.split("\\(")[0].strip());
        }
        return out;
    }
}
