package com.github.typingtanuki.batt;

public class Battery {
    private String url;
    private double volt;
    private int amp;
    private Double watt;
    private String description;
    private String model;
    private int cells;
    private String brand;
    private String[] partNo;
    private String[] models;

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
                " | " + brand +
                " | " + String.join("<br/>", partNo) +
                " | " + description +
                " | " + volt + "V" +
                " | " + amp + "mAh" +
                " | " + watt + "W" +
                " | " + cells +
                " | " + url +
                " | " + String.join("<br/>", models) +
                " | | |";
    }

    public static String tableHeader() {
        return "| Model | Brand | Part No. | Description | Volt | Amp | Watt | Cell | URL | Models | Connector | Form factor |\r\n" +
                "| ----- | ----- | -------- | ----------- | ---- | --- | ---- | ---- | --- | ------ | --------- | ----------- |";
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBrand() {
        return brand;
    }

    public void setPartNo(String[] partNo) {
        this.partNo = partNo;
    }

    public String[] getPartNo() {
        return partNo;
    }

    public void setModels(String[] models) {
        this.models = models;
    }

    public String[] getModels() {
        return models;
    }
}
