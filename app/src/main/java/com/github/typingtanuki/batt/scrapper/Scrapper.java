package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Maker;
import com.github.typingtanuki.batt.exceptions.PageUnavailableException;

import java.io.IOException;
import java.util.List;

public interface Scrapper {
    List<Maker> makers() throws IOException, PageUnavailableException;

    String name();
}
