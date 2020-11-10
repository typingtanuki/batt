package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Battery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.typingtanuki.batt.scrapper.BatteryDetailReader.extractBatteryDetails;
import static com.github.typingtanuki.batt.scrapper.BatteryLister.listBatteriesForMaker;
import static com.github.typingtanuki.batt.scrapper.MakerListReader.extractMakers;

public class NewLaptopAccessoryScrapper implements Scrapper {
    @Override
    public List<Battery> listBatteries() throws IOException {
        Map<String, Battery> found = new LinkedHashMap<>();

        List<String> makers = extractMakers();
        for (String maker : makers) {
            List<Battery> batteries = listBatteriesForMaker(maker);
            if (batteries.isEmpty()) {
                continue;
            }

            for (Battery battery : batteries) {
                extractBatteryDetails(battery);
                if (battery.isValid()) {
                    found.put(battery.getModel(), battery);
                }
            }
        }

        return new ArrayList<>(found.values());
    }
}
