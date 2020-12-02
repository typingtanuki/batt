package com.github.typingtanuki.batt;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryComparator;
import com.github.typingtanuki.batt.battery.Maker;
import com.github.typingtanuki.batt.battery.MakerComparator;
import com.github.typingtanuki.batt.db.BatteryDB;
import com.github.typingtanuki.batt.scrapper.LaptopBatteryShopScrapper;
import com.github.typingtanuki.batt.scrapper.NewLaptopAccessoryScrapper;
import com.github.typingtanuki.batt.scrapper.Scrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.github.typingtanuki.batt.scrapper.BatteryDetailReader.extractBatteryDetails;
import static com.github.typingtanuki.batt.scrapper.BatteryLister.listBatteriesForMaker;
import static com.github.typingtanuki.batt.utils.Progress.progress;
import static com.github.typingtanuki.batt.utils.Progress.progressStart;

public class App {
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
                List<Battery> found = entry.getValue();
                StringBuilder output = new StringBuilder();
                output.append("Found: ")
                        .append(found.size())
                        .append("\r\n\r\n")
                        .append(Battery.tableHeader())
                        .append("\r\n");
                for (Battery battery : found) {
                    BatteryDB.addBattery(battery);
                    output.append(battery.asTable()).append("\r\n");
                }
                BatteryDB.dump();
                Files.write(Paths.get("detected_" + entry.getKey() + ".md"), output.toString().getBytes(StandardCharsets.UTF_8));
            }
            System.out.println();
            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(14);
        }
    }

    public static List<Battery> listBatteries(List<Maker> makers) throws IOException {
        Map<String, Battery> found = new LinkedHashMap<>();

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
                extractBatteryDetails(battery);
                if (battery.isValid()) {
                    Battery previous = found.get(battery.getModel());
                    if (previous != null) {
                        previous.mergeWith(battery);
                    } else {
                        found.put(battery.getModel(), battery);
                    }
                }
            }
        }

        return new ArrayList<>(found.values());
    }
}
