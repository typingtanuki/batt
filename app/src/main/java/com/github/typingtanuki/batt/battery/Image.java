package com.github.typingtanuki.batt.battery;

import com.github.typingtanuki.batt.utils.PathBuilder;

import java.nio.file.Path;
import java.util.Objects;

import static com.github.typingtanuki.batt.utils.CachedHttp.CACHE_PATH;

public class Image {
    private final String url;
    private final Path path;

    public Image(Battery battery, String url) {
        this.url = url;
        PathBuilder builder = new PathBuilder(CACHE_PATH)
                .withSubFolder("image");

        if (battery.getConnector() != BatteryConnector.CUSTOM && battery.getConnector() != BatteryConnector.UNKNOWN) {
            builder.withSubFolder(battery.getConnector().name());
        }

        builder.withFileName(url, true)
                .withFileNamePrefix(battery.getModel() + "-", false)
                .withExtension(".jpg");
        path = builder.build();
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
