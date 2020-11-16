package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Battery;

import java.io.IOException;
import java.util.List;

public interface Scrapper {
    List<Battery> listBatteries() throws IOException;
}