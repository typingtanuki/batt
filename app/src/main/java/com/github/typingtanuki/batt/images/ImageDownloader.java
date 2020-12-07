package com.github.typingtanuki.batt.images;

import com.github.typingtanuki.batt.battery.Battery;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.github.typingtanuki.batt.utils.CachedHttp.download;
import static com.github.typingtanuki.batt.utils.Progress.*;

public final class ImageDownloader {
    private static final Map<String, Set<String>> URLS = new HashMap<>();
    private static final Map<String, Battery> BATTERIES = new HashMap<>();

    private ImageDownloader() {
        super();
    }

    public static void addImageToDownload(Battery battery, String url) {
        BATTERIES.put(battery.getModel(), battery);
        Set<String> urls = URLS.getOrDefault(battery.getModel(), new HashSet<>());
        urls.add(url);
        URLS.put(battery.getModel(), urls);
    }

    public static void downloadImages() throws IOException {
        progressStart("Image download");
        for (Map.Entry<String, Battery> entry : BATTERIES.entrySet()) {
            progress(BATTERY_MATCH);
            String model = entry.getKey();
            Battery battery = entry.getValue();
            Set<String> urls = URLS.get(model);
            for (String url : urls) {
                download(battery, url);
            }
        }
    }
}
