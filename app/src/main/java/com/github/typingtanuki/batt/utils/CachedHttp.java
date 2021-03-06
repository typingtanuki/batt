package com.github.typingtanuki.batt.utils;

import com.github.typingtanuki.batt.battery.Image;
import com.github.typingtanuki.batt.exceptions.PageUnavailableException;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.typingtanuki.batt.utils.Progress.*;

public final class CachedHttp {
    public static final String CACHE_PATH = "url_cache";
    public static final String OLD_CACHE_PATH = "url_cache/old";
    private static final Map<PageType, Long> TIMEOUT = new HashMap<>();
    private static final long RETRY_INTERVAL = 10_000;
    private static final int MAX_RETRIES = 10;

    static {
        TIMEOUT.put(PageType.LIST, TimeUnit.DAYS.toMillis(14));
        TIMEOUT.put(PageType.MAKER, TimeUnit.DAYS.toMillis(30));
    }

    private CachedHttp() {
        super();
    }

    public static Document http(PageType type, String url) throws IOException, PageUnavailableException {
        Path path = new PathBuilder(CACHE_PATH)
                .withSubFolder(type)
                .withFileName(url, true)
                .withExtension(".html")
                .build();

        if (Files.exists(path) && isUpToDate(path, type)) {
            progress(PAGE_CACHED);
            return Jsoup.parse(String.join("\r\n", Files.readAllLines(path)));
        }

        Path oldPath = Paths.get(path.toString().replaceFirst(CACHE_PATH, OLD_CACHE_PATH));
        if (Files.exists(oldPath) && isUpToDate(oldPath, type)) {
            Files.createDirectories(parentOf(path));
            Files.move(oldPath, path);
            progress(PAGE_OLD_CACHE);
            return Jsoup.parse(String.join("\r\n", Files.readAllLines(path)));
        }

        progress(PAGE_DOWNLOAD);

        Document document = null;
        int retries = MAX_RETRIES;
        IOException lastIo = null;
        while (retries > 0 && document == null) {
            lastIo = null;
            try {
                document = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get();
            } catch (HttpStatusException e) {
                if (e.getStatusCode() == 404) {
                    throw new PageUnavailableException(url, e);
                }
                lastIo = e;
                sleep();
            } catch (IOException e) {
                lastIo = e;
                sleep();
            } catch (UncheckedIOException e) {
                lastIo = new IOException("Unchecked IO in JSoup", e);
                sleep();
            }
            retries--;
        }

        if (document == null && lastIo != null) {
            throw lastIo;
        }

        if (document == null) {
            throw new IOException("No document, but no failure");
        }

        String html = document.html();
        Files.createDirectories(parentOf(path));
        Files.write(path, Collections.singletonList(html));
        return document;
    }

    private static Path parentOf(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent == null) {
            throw new IOException("Cache directory is at root level");
        }
        return parent;
    }

    private static void sleep() {
        try {
            progress(CONNECTION_RETRY);
            Thread.sleep(RETRY_INTERVAL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean isUpToDate(Path path, PageType type) throws IOException {
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

    public static void download(Image image) throws IOException {
        Path path = image.getPath();
        if (Files.exists(path)) {
            progress(PAGE_CACHED);
            return;
        }

        Path oldPath = Paths.get(path.toString().replaceFirst(CACHE_PATH, OLD_CACHE_PATH));
        if (Files.exists(oldPath)) {
            Files.createDirectories(parentOf(path));
            Files.move(oldPath, path);
            progress(PAGE_OLD_CACHE);
            return;
        }

        System.out.println(image.getUrl());
        System.out.println(image.getPath());
        progress(PAGE_DOWNLOAD);

        Connection.Response document = null;
        int retries = MAX_RETRIES;
        IOException lastIo = null;
        while (retries > 0 && document == null) {
            lastIo = null;
            try {
                document = Jsoup.connect(image.getUrl()).ignoreContentType(true).timeout(5000).execute();
                Files.createDirectories(parentOf(path));
                Files.write(path, document.bodyAsBytes());
            } catch (IOException e) {
                lastIo = e;
                sleep();
            } catch (UncheckedIOException e) {
                lastIo = new IOException("Unchecked failure in JSoup", e);
                sleep();
            }
            retries--;
        }

        if (lastIo != null) {
            throw lastIo;
        }
    }

    public static void deleteDownload(Image image) throws IOException {
        Files.deleteIfExists(image.getPath());
    }
}
