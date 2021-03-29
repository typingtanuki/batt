package com.github.typingtanuki.batt.utils;

import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UrlUtils {
    private static final Pattern URL_EXTRACTOR = Pattern.compile("^(https?://[^/]+)/.*$");
    private static final Map<String, String> ROOT_CACHE = new HashMap<>();

    private UrlUtils() {
        super();
    }

    public static String absoluteURL(Element link, String rootUrl) {
        String url = link.attr("href");
        if (url.startsWith("http")) {
            return url;
        }
        String extractedRoot = ROOT_CACHE.get(rootUrl);
        if (extractedRoot == null) {
            Matcher matcher = URL_EXTRACTOR.matcher(rootUrl);
            if (!matcher.matches()) {
                throw new IllegalStateException("Could not extract URL from: " + rootUrl);
            }
            extractedRoot = matcher.group(1);
            ROOT_CACHE.put(rootUrl, extractedRoot);
        }
        return extractedRoot + url;
    }
}
