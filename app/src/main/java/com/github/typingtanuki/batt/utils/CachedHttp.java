package com.github.typingtanuki.batt.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.github.typingtanuki.batt.utils.Progress.progress;

public final class CachedHttp {
    private static final String CACHE_PATH = "url_cache";

    public CachedHttp() {
        super();
    }

    public static Document http(String type, String url) throws IOException {
        Path path = pathFor(type, url, ".html");

        if (Files.exists(path)) {
            return Jsoup.parse(String.join("\r\n", Files.readAllLines(path)));
        }

        progress("+");
        Document document = Jsoup.connect(url).get();
        String html = document.html();
        Files.createDirectories(path.getParent());
        Files.write(path, Collections.singletonList(html));
        return document;
    }

    public static void download(String model, String url) throws IOException {
        Path path = pathFor("image_" + model, url, ".jpg");
        if (Files.exists(path)) {
            return;
        }

        progress("+");
        Connection.Response document = Jsoup.connect(url).ignoreContentType(true).execute();
        Files.createDirectories(path.getParent());
        Files.write(path, document.bodyAsBytes());
    }

    public static void deleteDownload(String model, String url) throws IOException {
        Path path = pathFor("image_" + model, url, ".jpg");
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

    private static Path pathFor(String type, String url, String extension) {
        String file = url.toLowerCase(Locale.ENGLISH).replaceAll("[:./\\\\?&]", "_");
        String hashed = file;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes(StandardCharsets.UTF_8));
            hashed = hex(hash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Could not hash entry " + file);
            e.printStackTrace(System.err);
            System.exit(12);
        }

        return Paths.get(CACHE_PATH).resolve(type).resolve(hashed + extension);
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02x", aByte));
        }
        return result.toString();
    }
}
