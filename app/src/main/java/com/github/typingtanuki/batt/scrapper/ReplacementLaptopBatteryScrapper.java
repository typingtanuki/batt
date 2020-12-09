package com.github.typingtanuki.batt.scrapper;

public class ReplacementLaptopBatteryScrapper extends AbstractScrapper{
    public ReplacementLaptopBatteryScrapper() {
        super("https://www.replacement-laptop-battery.com.au/discount.html");
    }

    @Override
    public String name() {
        return "Replacement Laptop Battery";
    }
}
