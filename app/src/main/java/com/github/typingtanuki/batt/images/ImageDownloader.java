package com.github.typingtanuki.batt.images;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.Image;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.github.typingtanuki.batt.utils.CachedHttp.deleteDownload;
import static com.github.typingtanuki.batt.utils.CachedHttp.download;
import static com.github.typingtanuki.batt.utils.Progress.progress;
import static com.github.typingtanuki.batt.utils.Progress.progressStart;

public final class ImageDownloader {
    private static final Set<Battery> TO_DOWNLOAD = new HashSet<>();
    private static final Set<Battery> TO_DELETE = new HashSet<>();

    private ImageDownloader() {
        super();
    }

    public static void addImagesToDownload(Battery battery) {
        TO_DOWNLOAD.add(battery);
    }

    public static void addImagesToDelete(Battery battery) {
        TO_DELETE.add(battery);
    }

    public static void downloadImages() throws IOException {
        progressStart("Image download");

        Set<Image> imagesToDownload = new HashSet<>();
        for (Battery battery : TO_DOWNLOAD) {
            if (!battery.isValid()) {
                TO_DELETE.add(battery);
                continue;
            }
            imagesToDownload.addAll(battery.getImages());
        }

        for (Battery battery : TO_DELETE) {
            for (Image image : battery.getImages()) {
                if (!imagesToDownload.contains(image)) {
                    deleteDownload(image);
                }
            }
        }

        int total = imagesToDownload.size();
        int current = 0;
        int lastPercent = -1;
        for (Image image : imagesToDownload) {
            download(image);
            int percent = ((current + 1) * 20) / total * 5;
            if (percent > lastPercent) {
                lastPercent = percent;
                progress(" " + percent + "% ");
            }
            current++;
        }
    }
}
