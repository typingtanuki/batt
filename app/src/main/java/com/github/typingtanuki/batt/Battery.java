package com.github.typingtanuki.batt;

public class Battery {
    private String url;
    private double volt;
    private int amp;
    private Double watt;
    private String description;
    private String model;

    public Battery(String url, double volt, int amp, Double watt) {
        super();

        this.url = url;
        this.volt = volt;
        this.amp = amp;
        this.watt = watt;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getVolt() {
        return volt;
    }

    public void setVolt(double volt) {
        this.volt = volt;
    }

    public int getAmp() {
        return amp;
    }

    public void setAmp(int amp) {
        this.amp = amp;
    }

    public Double getWatt() {
        return watt;
    }

    public void setWatt(Double watt) {
        this.watt = watt;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    @Override
    public String toString() {
        return "| " + model +
                " | " + description +
                " | " + volt + "V" +
                " | " + amp + "mAh" +
                " | " + watt + "W" +
                " | " + url +
                " |";
    }
}
