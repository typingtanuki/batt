package com.github.typingtanuki.batt.scrapper.denchipro;

public class DenchiProOtherScrapper extends AbstractDenchiProScrapper {
    public DenchiProOtherScrapper() {
        super("http://www.denchipro.com/product-category/%e9%9b%bb%e6%b1%a0%e7%a8%ae%e9%a1%9e/%e3%81%9d%e3%81%ae%e4%bb%96/");
    }

    @Override
    public String name() {
        return "Denchipro - Other";
    }
}
