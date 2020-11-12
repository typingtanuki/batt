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
import static com.github.typingtanuki.batt.utils.Progress.progress;

public class NewLaptopAccessoryScrapper implements Scrapper {
    @Override
    public List<Battery> listBatteries() throws IOException {
        Map<String, Battery> found = new LinkedHashMap<>();

        List<String> makers = extractMakers();
        for (int i = 0; i < makers.size(); i++) {
            progress(" " + (i + 1) + "/" + makers.size() + " ");
            String maker = makers.get(i);
            List<Battery> batteries = listBatteriesForMaker(maker);
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
