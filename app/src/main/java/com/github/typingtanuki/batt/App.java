package com.github.typingtanuki.batt;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryComparator;
import com.github.typingtanuki.batt.battery.Maker;
import com.github.typingtanuki.batt.battery.MakerComparator;
import com.github.typingtanuki.batt.db.BatteryDB;
import com.github.typingtanuki.batt.images.ImageDownloader;
import com.github.typingtanuki.batt.output.MarkdownOutput;
import com.github.typingtanuki.batt.scrapper.LaptopBatteryShopScrapper;
import com.github.typingtanuki.batt.scrapper.NewLaptopAccessoryScrapper;
import com.github.typingtanuki.batt.scrapper.Scrapper;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.github.typingtanuki.batt.battery.Battery.cleanPartNo;
import static com.github.typingtanuki.batt.scrapper.BatteryDetailReader.extractBatteryDetails;
import static com.github.typingtanuki.batt.scrapper.BatteryLister.listBatteriesForMaker;
import static com.github.typingtanuki.batt.utils.CachedHttp.deleteDownload;
import static com.github.typingtanuki.batt.utils.Progress.*;

public class App {
    private static final Map<String, Battery> ID_MATCHER = new HashMap<>();

    public static void main(String[] args) {
        try {
            List<Scrapper> scrappers = new ArrayList<>();
            Set<Maker> makers = new HashSet<>();

            scrappers.add(new NewLaptopAccessoryScrapper());
            scrappers.add(new LaptopBatteryShopScrapper());

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
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(14);
        }
    }

    public static List<Battery> listBatteries(List<Maker> makers) throws IOException {
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
                Battery parsed = extractBatteryDetails(battery);
                if (parsed == null) {
                    continue;
                }
                boolean isValid = battery.isValid();

                Battery previous = findSimilar(parsed);
                if (previous != null) {
                    previous.mergeWith(parsed);
                    handleBatteryPost(battery.getSourcePage(), parsed, isValid);
                } else {
                    handleBatteryPost(battery.getSourcePage(), battery, isValid);
                    if (isValid) {
                        found.add(parsed);
                    }
                }
            }
        }

        return found;
    }

    private static void handleBatteryPost(Document page, Battery battery, boolean isValid) throws IOException {
        if (isValid) {
            progress(BATTERY_MATCH);
            BatteryDB.addBattery(battery, true);
            downloadBatteryImages(page, battery);
        } else {
            progress(BATTERY_NO_MATCH);
            BatteryDB.addBattery(battery, false);
            deleteBatteryImages(page, battery);
        }
    }

    private static void deleteBatteryImages(Document page, Battery battery) throws IOException {
        for (String image : ImageDownloader.batteryImages(page)) {
            deleteDownload(battery, image);
        }
    }

    private static void downloadBatteryImages(Document page, Battery battery) {
        for (String image : ImageDownloader.batteryImages(page)) {
            ImageDownloader.addImageToDownload(battery, image);
        }
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
                if(oAmp>nAmp){
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
