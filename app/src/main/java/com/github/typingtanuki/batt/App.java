package com.github.typingtanuki.batt;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryComparator;
import com.github.typingtanuki.batt.db.BatteryDB;
import com.github.typingtanuki.batt.scrapper.NewLaptopAccessoryScrapper;
import com.github.typingtanuki.batt.scrapper.Scrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        try {
            Scrapper scrapper = new NewLaptopAccessoryScrapper();
            List<Battery> batteries = new LinkedList<>(scrapper.listBatteries());
            batteries.sort(new BatteryComparator());

            Path out = Paths.get("detected.md");

            StringBuilder output = new StringBuilder();
            output.append("Found: ")
                    .append(batteries.size())
                    .append("\r\n\r\n")
                    .append(Battery.tableHeader())
                    .append("\r\n");
            for (Battery battery : batteries) {
                BatteryDB.addBattery(battery);
                output.append(battery.asTable()).append("\r\n");
            }
            BatteryDB.dump();
            Files.write(out, output.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println();
            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
