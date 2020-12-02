package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.Maker;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.typingtanuki.batt.scrapper.BatteryDetailReader.extractBatteryDetails;
import static com.github.typingtanuki.batt.scrapper.BatteryLister.listBatteriesForMaker;
import static com.github.typingtanuki.batt.scrapper.MakerListReader.extractMakers;
import static com.github.typingtanuki.batt.utils.Progress.progress;
import static com.github.typingtanuki.batt.utils.Progress.progressStart;

public abstract class AbstractScrapper implements Scrapper {
    private final String rootUrl;

    public AbstractScrapper(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    @Override
    public void listBatteries(List<Battery> batteries) throws IOException {
        Map<String, Battery> found = new LinkedHashMap<>();
        for (Battery battery : batteries) {
            found.put(battery.getModel(), battery);
        }

        List<Maker> makers = extractMakers(rootUrl);

        int lastPercent = 0;

        for (int i = 0; i < makers.size(); i++) {
            Maker maker = makers.get(i);
            progressStart(maker.getName());

            int percent = ((i + 1) * 20) / makers.size() * 5;
            if (percent > lastPercent) {
                lastPercent = percent;
                progress(" " + percent + "% ");
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

        batteries.clear();
        batteries.addAll(found.values());
    }
}
