package com.github.typingtanuki.batt;

import com.github.typingtanuki.batt.battery.*;
import com.github.typingtanuki.batt.db.BatteryDB;
import com.github.typingtanuki.batt.exceptions.NoPartException;
import com.github.typingtanuki.batt.exceptions.PageUnavailableException;
import com.github.typingtanuki.batt.images.ImageDownloader;
import com.github.typingtanuki.batt.output.ForumOutput;
import com.github.typingtanuki.batt.output.MarkdownOutput;
import com.github.typingtanuki.batt.scrapper.*;
import com.github.typingtanuki.batt.scrapper.denchipro.DenchiProLaptopScrapper;
import com.github.typingtanuki.batt.scrapper.denchipro.DenchiProOtherScrapper;
import com.github.typingtanuki.batt.scrapper.denchipro.DenchiProTabletScrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.github.typingtanuki.batt.battery.Battery.cleanPartNo;
import static com.github.typingtanuki.batt.utils.Progress.*;

public class App {
    private static final Map<String, Battery> ID_MATCHER = new HashMap<>();

    public static void main(String[] args) {
        try {
            List<Scrapper> scrappers = new ArrayList<>();
            List<Maker> makers = new ArrayList<>();

            scrappers.add(new NewLaptopAccessoryScrapper());
            scrappers.add(new LaptopBatteryShopScrapper());
            scrappers.add(new ReplacementLaptopBatteryScrapper());
            scrappers.add(new NotePartsScrapper());

            scrappers.add(new DenchiProTabletScrapper());
            scrappers.add(new DenchiProLaptopScrapper());
            scrappers.add(new DenchiProOtherScrapper());

            for (Scrapper scrapper : scrappers) {
                progressStart("Listing from " + scrapper.name());
                makers.addAll(scrapper.makers());
            }
            List<Maker> makerList = new ArrayList<>(makers);
            makerList.sort(new MakerComparator());

            List<Battery> batteries = listBatteries(makerList);
            batteries.sort(new BatteryComparator());

            Map<String, List<Battery>> batteriesPerCondition = new LinkedHashMap<>();
            for (Battery battery : batteries) {
                for (String matchedCondition : battery.getMatchedConditions()) {
                    List<Battery> l = batteriesPerCondition.computeIfAbsent(matchedCondition, k -> new ArrayList<>());
                    l.add(battery);
                }
            }

            for (Map.Entry<String, List<Battery>> entry : batteriesPerCondition.entrySet()) {
                MarkdownOutput markdown = new MarkdownOutput(entry.getValue());
                Files.write(
                        Paths.get("detected_" + entry.getKey() + ".md"),
                        markdown.generate().getBytes(StandardCharsets.UTF_8));
                ForumOutput forum = new ForumOutput(entry.getValue());
                Files.write(
                        Paths.get("detected_" + entry.getKey() + ".frm"),
                        forum.generate().getBytes(StandardCharsets.UTF_8));
            }
            BatteryDB.dump();
            ImageDownloader.downloadImages();
            System.out.println();
            System.out.println("Done");
        } catch (IOException | PageUnavailableException e) {
            e.printStackTrace();
            System.exit(14);
        }
    }

    public static List<Battery> listBatteries(List<Maker> makers) throws IOException, PageUnavailableException {
        List<Battery> found = new ArrayList<>();

        int lastPercent = 0;
        String lastMaker = "";

        for (int i = 0; i < makers.size(); i++) {
            Maker maker = makers.get(i);
            if (!lastMaker.equals(maker.getName())) {
                progressStart(maker.getName());
                lastMaker = maker.getName();

                int percent = ((i + 1) * 20) / makers.size() * 5;
                if (percent > lastPercent) {
                    lastPercent = percent;
                    progress(" " + percent + "% ");
                }
            }


            List<Battery> allBatteries = maker.listBatteries();
            progress(" (" + allBatteries.size() + ") ");
            for (Battery battery : allBatteries) {
                Battery parsed;
                try {
                    parsed = maker.extractBatteryDetails(battery);
                } catch (RuntimeException e) {
                    throw new IllegalStateException("Failed reading details on " + battery.getCurrentUrl(), e);
                } catch (NoPartException e) {
                    // Not a battery
                    continue;
                }
                if (parsed == null) {
                    continue;
                }
                parsed.consolidate();
                if (parsed.getAmp() == null) {
                    continue;
                }
                battery.consolidate();
                if (battery.getAmp() == null) {
                    continue;
                }


                Battery previous;
                try {
                    previous = findSimilar(parsed);
                } catch (NoPartException e) {
                    // Broken battery
                    continue;
                }
                try {
                    if (previous != null) {
                        progress(MERGED);
                        previous.mergeWith(parsed);
                        handleBatteryPost(parsed, false);
                        handleBatteryPost(previous, true);
                    } else {
                        handleBatteryPost(battery, true);
                        if (battery.isValid()) {
                            found.add(parsed);
                        }
                    }
                } catch (NoPartException e) {
                    throw new IOException("Trying to merge broken batteries", e);
                }
            }
        }

        return found;
    }

    private static void handleBatteryPost(Battery battery, boolean download) throws NoPartException {
        boolean isValid = battery.isValid();
        if (isValid) {
            progress(BATTERY_MATCH);
        } else {
            progress(BATTERY_NO_MATCH);
        }
        if (isValid && download) {
            ImageDownloader.addImagesToDownload(battery);
        } else {
            ImageDownloader.addImagesToDelete(battery);
        }
        BatteryDB.addBattery(battery, isValid);
    }

    private static Battery findSimilar(Battery battery) throws NoPartException {
        Set<String> parts = new HashSet<>();
        for (String part : battery.getPartNo()) {
            parts.add(cleanPartNo(part, true));
        }
        for (String part : parts) {
            Battery matched = ID_MATCHER.get(part);
            if (matched != null) {
                if (!similarValue(battery.getVolt(), matched.getVolt())) {
                    continue;
                }
                if (!similarValue(battery.getAmp(), matched.getAmp())) {
                    continue;
                }
                if (!similarValue(battery.getType(), matched.getType())) {
                    continue;
                }
                return matched;
            }
        }
        for (String part : parts) {
            ID_MATCHER.put(part, battery);
        }
        return null;
    }

    private static boolean similarValue(Number a, Number b) {
        if (a == null) {
            return true;
        }
        if (b == null) {
            return true;
        }
        return Math.abs((a.doubleValue() - b.doubleValue()) / a.doubleValue()) < 0.1;
    }

    private static boolean similarValue(BatteryType a, BatteryType b) {
        if (a == null) {
            return true;
        }
        if (b == null) {
            return true;
        }
        return a == b;
    }
}
