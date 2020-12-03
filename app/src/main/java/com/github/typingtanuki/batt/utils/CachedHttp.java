package com.github.typingtanuki.batt.utils;

import com.github.typingtanuki.batt.battery.Battery;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.typingtanuki.batt.utils.Progress.*;

public final class CachedHttp {
    private static final String CACHE_PATH = "url_cache";

    private CachedHttp() {
        super();
    }

    public static Document http(String type, String url) throws IOException {
        Path path = new PathBuilder(CACHE_PATH)
                .withSubFolder(type)
                .withFileName(url, true)
                .withExtension(".html")
                .build();

        if (Files.exists(path)) {
            progress(PAGE_CACHED);
            return Jsoup.parse(String.join("\r\n", Files.readAllLines(path)));
        }

        progress(PAGE_DOWNLOAD);
        Document document = Jsoup.connect(url).get();
        String html = document.html();
        Files.createDirectories(path.getParent());
        Files.write(path, Collections.singletonList(html));
        return document;
    }

    public static void download(Battery battery, String url) throws IOException {
        Path path = imagePath(battery, url);
        if (Files.exists(path)) {
            progress(PAGE_CACHED);
            return;
        }

        progress(PAGE_DOWNLOAD);
        Connection.Response document = Jsoup.connect(url).ignoreContentType(true).execute();
        Files.createDirectories(path.getParent());
        Files.write(path, document.bodyAsBytes());
    }

    public static void deleteDownload(Battery battery, String url) throws IOException {
        Path path = imagePath(battery, url);
        Files.deleteIfExists(path);
        Path parent = path.getParent();
        if (!Files.exists(parent)) {
            return;
        }
        List<Path> content = Files.list(parent).collect(Collectors.toList());
        if (content.isEmpty()) {
            Files.deleteIfExists(parent);
        }
    }

    private static Path imagePath(Battery battery, String url) {
        return new PathBuilder(CACHE_PATH)
                .withSubFolder("image")
                .withFileName(url, true)
                .withFileNamePrefix(battery.getModel() + "-", false)
                .withExtension(".jpg")
                .build();
    }
}
