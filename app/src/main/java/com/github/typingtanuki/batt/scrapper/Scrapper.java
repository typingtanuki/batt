package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Maker;

import java.io.IOException;
import java.util.List;

public interface Scrapper {
    List<Maker> makers() throws IOException;
}
