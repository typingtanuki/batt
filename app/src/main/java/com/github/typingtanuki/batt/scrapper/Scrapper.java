package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.Maker;
import com.github.typingtanuki.batt.exceptions.NoPartException;
import com.github.typingtanuki.batt.exceptions.PageUnavailableException;

import java.io.IOException;
import java.util.List;

public interface Scrapper {
    List<Maker> makers() throws IOException, PageUnavailableException;

    String name();

    List<Battery> listBatteries(Maker maker) throws IOException, PageUnavailableException;

    Battery extractBatteryDetails(Battery battery) throws IOException, NoPartException;
}
