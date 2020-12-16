package com.github.typingtanuki.batt.scrapper.denchipro;

public class DenchiProTabletScrapper extends AbstractDenchiProScrapper {
    public DenchiProTabletScrapper() {
        super("http://www.denchipro.com/product-category/%e9%9b%bb%e6%b1%a0%e7%a8%ae%e9%a1%9e/%e3%82%bf%e3%83%96%e3%83%ac%e3%83%83%e3%83%88pc-%e3%83%90%e3%83%83%e3%83%86%e3%83%aa%e3%83%bc/");
    }

    @Override
    public String name() {
        return "Denchipro - Tablet";
    }
}
