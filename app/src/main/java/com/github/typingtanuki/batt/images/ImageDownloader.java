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
    private static final Set<Image> IMAGES = new HashSet<>();
    private static final Set<Image> TO_DELETE = new HashSet<>();

    private ImageDownloader() {
        super();
    }

    public static void addImagesToDownload(Battery battery) {
        IMAGES.addAll(battery.getImages());
    }

    public static void addImagesToDelete(Battery battery) {
        TO_DELETE.addAll(battery.getImages());
    }

    public static void downloadImages() throws IOException {
        progressStart("Image download");

        for(Image image:TO_DELETE){
            if(!IMAGES.contains(image)){
                deleteDownload(image);
            }
        }

        int total = IMAGES.size();
        int current = 0;
        int lastPercent = -1;
        for (Image image : IMAGES) {
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
