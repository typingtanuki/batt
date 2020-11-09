package com.github.typingtanuki.batt;

public class Battery {
    private String url;
    private double volt;
    private int amp;
    private Double watt;
    private String description;
    private String model;
    private int cells;

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
        this.description = description.replaceAll("Brand new", "");
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

    public int getCells() {
        return cells;
    }

    public void setCells(int cells) {
        this.cells = cells;
    }

    public String asTable() {
        return "| " + model +
                " | " + description +
                " | " + volt + "V" +
                " | " + amp + "mAh" +
                " | " + watt + "W" +
                " | " + cells +
                " | " + url +
                " | | |";
    }

    public static String tableHeader() {
        return "| Model | Description | Volt | Amp | Watt | Cell | URL | Connector | Form factor |\r\n" +
                "| ----- | ----------- | ---- | --- | ---- | ---- | --- | --------- | ----------- |";
    }
}
