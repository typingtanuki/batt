package com.github.typingtanuki.batt.battery;

import com.github.typingtanuki.batt.exceptions.NoPartException;
import com.github.typingtanuki.batt.utils.PathBuilder;

import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.typingtanuki.batt.utils.CachedHttp.CACHE_PATH;

public class Image {
    private static final Pattern URL_WITH_SIZE = Pattern.compile("^(.*)-\\d+x\\d+\\.jpg$");

    private final String url;
    private final Path path;

    public Image(Battery battery, String url) throws NoPartException {
        this.url = clean(url);
        PathBuilder builder = new PathBuilder(CACHE_PATH)
                .withSubFolder("image");

        if (battery.getConnector() != BatteryConnector.CUSTOM && battery.getConnector() != BatteryConnector.UNKNOWN) {
            builder.withSubFolder(battery.getConnector().name());
        }

        builder.withFileName(this.url, true)
                .withFileNamePrefix(battery.getModel() + "-", false)
                .withExtension(".jpg");
        path = builder.build();
    }

    private String clean(String url) {
        Matcher matcher = URL_WITH_SIZE.matcher(url);
        if (matcher.matches()) {
            return matcher.group(1) + ".jpg";
        }
        return url;
    }

    public String getUrl() {
        return url;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return Objects.equals(url, image.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
