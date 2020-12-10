package com.github.typingtanuki.batt;

import com.github.typingtanuki.batt.battery.*;
import com.github.typingtanuki.batt.db.BatteryDB;
import com.github.typingtanuki.batt.exceptions.PageUnavailableException;
import com.github.typingtanuki.batt.images.ImageDownloader;
import com.github.typingtanuki.batt.output.MarkdownOutput;
import com.github.typingtanuki.batt.scrapper.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.github.typingtanuki.batt.battery.Battery.cleanPartNo;
import static com.github.typingtanuki.batt.scrapper.BatteryDetailReader.extractBatteryDetails;
import static com.github.typingtanuki.batt.scrapper.BatteryLister.listBatteriesForMaker;
import static com.github.typingtanuki.batt.utils.Progress.*;

public class App {
    private static final Map<String, Battery> ID_MATCHER = new HashMap<>();

    public static void main(String[] args) {
        try {
            List<Scrapper> scrappers = new ArrayList<>();
            Set<Maker> makers = new LinkedHashSet<>();

            scrappers.add(new NewLaptopAccessoryScrapper());
            scrappers.add(new LaptopBatteryShopScrapper());
            scrappers.add(new ReplacementLaptopBatteryScrapper());
            scrappers.add(new NotePartsScrapper());

            for (Scrapper scrapper : scrappers) {
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
                MarkdownOutput output = new MarkdownOutput(entry.getValue());
                Files.write(
                        Paths.get("detected_" + entry.getKey() + ".md"),
                        output.generate().getBytes(StandardCharsets.UTF_8));
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

            List<Battery> allBatteries = listBatteriesForMaker(maker);
            for (Battery battery : allBatteries) {
                Battery parsed;
                try {
                    parsed = extractBatteryDetails(battery);
                } catch (RuntimeException e) {
                    throw new IllegalStateException("Failed reading details on " + battery.getCurrentUrl(), e);
                }
                if (parsed == null) {
                    continue;
                }
                battery.consolidate();
                if (battery.getAmp() == null) {
                    continue;
                }

                Battery previous = findSimilar(parsed);
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
            }
        }

        return found;
    }

    private static void handleBatteryPost(Battery battery, boolean download) {
        boolean isValid = battery.isValid();
        if (isValid) {
            progress(BATTERY_MATCH);
        } else {
            progress(BATTERY_NO_MATCH);
        }
        if(isValid && download){
            ImageDownloader.addImagesToDownload(battery);
        }else{
            ImageDownloader.addImagesToDelete(battery);
        }
        BatteryDB.addBattery(battery, isValid);
    }

    private static Battery findSimilar(Battery battery) {
        Set<String> parts = new HashSet<>();
        for (String part : battery.getPartNo()) {
            parts.add(cleanPartNo(part));
        }
        for (String part : parts) {
            Battery matched = ID_MATCHER.get(part);
            if (matched != null) {
                int oAmp = battery.getAmp();
                int nAmp = matched.getAmp();
                if (oAmp > nAmp) {
                    matched.setAmp(oAmp);
                    matched.setWatt(battery.getWatt());
                }
                return matched;
            }
        }
        for (String part : parts) {
            ID_MATCHER.put(part, battery);
        }
        return null;
    }
}
