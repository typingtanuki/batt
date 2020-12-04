package com.github.typingtanuki.batt.scrapper;

public class LaptopBatteryShopScrapper extends AbstractScrapper {
    public LaptopBatteryShopScrapper() {
        super("https://www.laptop-battery-shop.com/laptop-batteries-c-1.html");
    }

    @Override
    public String name() {
        return "Laptop Battery Shop";
    }
}
