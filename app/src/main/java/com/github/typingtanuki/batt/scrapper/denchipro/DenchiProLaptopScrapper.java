package com.github.typingtanuki.batt.scrapper.denchipro;

public class DenchiProLaptopScrapper extends AbstractDenchiProScrapper {
    public DenchiProLaptopScrapper() {
        super("http://www.denchipro.com/product-category/%e9%9b%bb%e6%b1%a0%e7%a8%ae%e9%a1%9e/%e3%83%8e%e3%83%bc%e3%83%88%e3%83%91%e3%82%bd%e3%82%b3%e3%83%b3-%e3%83%90%e3%83%83%e3%83%86%e3%83%aa%e3%83%bc/");
    }

    @Override
    public String name() {
        return "Denchipro - Laptop";
    }
}
