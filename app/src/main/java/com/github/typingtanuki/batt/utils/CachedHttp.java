package com.github.typingtanuki.batt.utils;

import com.github.typingtanuki.batt.battery.Battery;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.typingtanuki.batt.utils.Progress.*;

public final class CachedHttp {
    private static final String CACHE_PATH = "url_cache";
    private static final Map<String, Long> TIMEOUT = new HashMap<>();
    private static final long RETRY_INTERVAL = 1000;
    private static final int MAX_RETRIES = 10;

    static {
        TIMEOUT.put("list", TimeUnit.DAYS.toMillis(7));
        TIMEOUT.put("maker", TimeUnit.DAYS.toMillis(14));
    }

    private CachedHttp() {
        super();
    }

    public static Document http(String type, String url) throws IOException {
        Path path = new PathBuilder(CACHE_PATH)
                .withSubFolder(type)
                .withFileName(url, true)
                .withExtension(".html")
                .build();

        if (Files.exists(path) && isUpToDate(path, type)) {
            progress(PAGE_CACHED);
            return Jsoup.parse(String.join("\r\n", Files.readAllLines(path)));
        }

        progress(PAGE_DOWNLOAD);

        Document document = null;
        int retries = MAX_RETRIES;
        IOException lastIo = null;
        while (retries > 0 && document == null) {
            try {
                document = Jsoup.connect(url).get();
            } catch (IOException e) {
                lastIo = e;
                sleep();
            }catch(IllegalArgumentException e){
                throw e;
            }
            retries--;
        }

        if (document == null && lastIo != null) {
            throw lastIo;
        }

        String html = document.html();
        Files.createDirectories(path.getParent());
        Files.write(path, Collections.singletonList(html));
        return document;
    }

    private static void sleep() {
        try {
            Thread.sleep(RETRY_INTERVAL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean isUpToDate(Path path, String type) throws IOException {
        long modified = Files.getLastModifiedTime(path).toMillis();
        long now = System.currentTimeMillis();

        Long timeout = TIMEOUT.get(type);
        if (timeout == null) {
            return true;
        }
        if (now - modified > timeout) {
            progress(CACHE_TIMEOUT);
            return false;
        }
        return true;
    }

    public static void download(Battery battery, String url) throws IOException {
        Path path = imagePath(battery, url);
        if (Files.exists(path)) {
            progress(PAGE_CACHED);
            return;
        }

        progress(PAGE_DOWNLOAD);

        Connection.Response document = null;
        int retries = MAX_RETRIES;
        IOException lastIo = null;
        while (retries > 0 && document == null) {
            try {
                document = Jsoup.connect(url).ignoreContentType(true).execute();
                Files.createDirectories(path.getParent());
                Files.write(path, document.bodyAsBytes());
            } catch (IOException e) {
                lastIo = e;
                sleep();
            }
            retries--;
        }

        if (document == null) {
            throw lastIo;
        }
    }

    public static void deleteDownload(Battery battery, String url) throws IOException {
        Path path = imagePath(battery, url);
        Files.deleteIfExists(path);
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
