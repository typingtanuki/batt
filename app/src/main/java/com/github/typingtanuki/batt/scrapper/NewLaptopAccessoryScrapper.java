package com.github.typingtanuki.batt.scrapper;

public class NewLaptopAccessoryScrapper extends AbstractScrapper {
    public NewLaptopAccessoryScrapper() {
        super("https://www.newlaptopaccessory.com/laptop-batteries-c-1.html");
    }

    @Override
    public String name() {
        return "New Laptop Accessory";
    }
}
