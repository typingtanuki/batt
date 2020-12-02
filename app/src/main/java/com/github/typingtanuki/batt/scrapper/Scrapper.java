package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.Maker;

import java.io.IOException;
import java.util.List;

public interface Scrapper {
    void listBatteries(List<Battery> batteries,
                       List<Maker> makers) throws IOException;

    List<Maker> makers() throws IOException;
}
