package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.Maker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.typingtanuki.batt.scrapper.BatteryDetailReader.extractBatteryDetails;
import static com.github.typingtanuki.batt.scrapper.BatteryLister.listBatteriesForMaker;
import static com.github.typingtanuki.batt.scrapper.MakerListReader.extractMakers;
import static com.github.typingtanuki.batt.utils.Progress.progress;
import static com.github.typingtanuki.batt.utils.Progress.progressStart;

public class NewLaptopAccessoryScrapper implements Scrapper {
    @Override
    public List<Battery> listBatteries() throws IOException {
        Map<String, Battery> found = new LinkedHashMap<>();

        List<Maker> makers = extractMakers();

        int lastPercent = 0;

        for (int i = 0; i < makers.size(); i++) {
            Maker maker = makers.get(i);
            progressStart(maker.getName());

            int percent = ((i + 1) * 20) / makers.size() * 5;
            if (percent > lastPercent) {
                lastPercent = percent;
                progress(" " + percent + "% ");
            }
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
