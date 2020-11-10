package com.github.typingtanuki.batt.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Locale;

import static com.github.typingtanuki.batt.utils.Progress.progress;

public final class CachedHttp {
    private static final String CACHE_PATH = "url_cache";

    public CachedHttp() {
        super();
    }

    public static Document http(String url) throws IOException {
        Path path = pathFor(url);

        if (Files.exists(path)) {
            return Jsoup.parse(String.join("\r\n", Files.readAllLines(path)));
        }

        progress(".");
        Document document = Jsoup.connect(url).get();
        String html = document.html();
        Files.createDirectories(path.getParent());
        Files.write(path, Collections.singletonList(html));
        return document;
    }


    private static Path pathFor(String url) {
        String file = url.toLowerCase(Locale.ENGLISH).replaceAll("[:./\\\\?&]", "_");
        return Paths.get(CACHE_PATH).resolve(file + ".html");
    }
}
