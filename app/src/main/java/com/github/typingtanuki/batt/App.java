package com.github.typingtanuki.batt;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryComparator;
import com.github.typingtanuki.batt.battery.Maker;
import com.github.typingtanuki.batt.battery.MakerComparator;
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
import java.util.regex.Pattern;

import static com.github.typingtanuki.batt.utils.Progress.*;

public class App {
    private static final Map<String, Battery> VISITED = new HashMap<>();
    private static final Pattern NUMBER_STRING = Pattern.compile("^\\d+$");

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

            int lastPercent = 0;
            int i = 0;
            Map<String, List<Battery>> batteriesPerCondition = new LinkedHashMap<>();
            progressStart("" +
                    "Building DB");
            for (Battery battery : batteries) {
                lastPercent = computePercent(i++, batteries.size(), lastPercent);
                battery.complete();
                boolean valid = battery.isValid();
                BatteryDB.addBattery(battery, valid);
                if (valid) {
                    ImageDownloader.addImagesToDownload(battery);
                } else {
                    ImageDownloader.addImagesToDelete(battery);
                }

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

    private static int computePercent(int count, int total, int lastPercent) {
        int percent = ((count + 1) * 20) / total * 5;
        if (percent > lastPercent) {
            progress(" " + percent + "% ");
        }
        return percent;
    }

    public static List<Battery> listBatteries(List<Maker> makers) throws IOException, PageUnavailableException {
        int lastPercent = 0;
        String lastMaker = "";

        List<Battery> cleaned = new ArrayList<>();
        for (int i = 0; i < makers.size(); i++) {
            Maker maker = makers.get(i);
            if (!lastMaker.equals(maker.getName())) {
                progressStart(maker.getName());
                lastMaker = maker.getName();

                lastPercent = computePercent(i, makers.size(), lastPercent);
            }


            List<Battery> allBatteries = maker.listBatteries();
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

                if (quickMerge(parsed)) {
                    if (parsed.isValid()) {
                        progress(MERGED_MATCH);
                    } else {
                        progress(MERGED_NO_MATCH);
                    }
                    continue;
                }

                if (battery.isValid()) {
                    progress(BATTERY_MATCH);
                } else {
                    progress(BATTERY_NO_MATCH);
                }
                cleaned.add(parsed);
            }
        }

        return cleaned;
    }

    private static boolean quickMerge(Battery parsed) {
        Battery mergedWith = null;
        for (String part : parsed.getPartNo()) {
            if (part.length() < 3) {
                continue;
            }
            if (part.length() < 5 && NUMBER_STRING.matcher(part).matches()) {
                continue;
            }

            Battery original = VISITED.get(part);
            if (original != null) {
                mergedWith = original.rewindMerges();
                mergedWith.mergeWith(parsed);
            }
        }

        if (mergedWith == null) {
            record(parsed);
            return false;
        }

        record(mergedWith);
        return true;
    }

    private static void record(Battery battery) {
        for (String part : battery.getPartNo()) {
            VISITED.put(part, battery);
        }
    }
}
