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

            List<Battery> batteries = new ArrayList<>();
            scrappers.get(0).listBatteries(batteries, makerList);
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
}
