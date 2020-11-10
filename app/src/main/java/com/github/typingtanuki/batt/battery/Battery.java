package com.github.typingtanuki.batt.battery;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class Battery {
    private static final int MAX_LIST_SIZE = 10;

    private final String url;
    private double volt;
    private Integer amp;
    private Double watt;
    private String description;
    private String model;
    private int cells;
    private String brand;
    private Set<String> partNo;
    private Set<String> models;
    private BatteryType type;
    private BatteryForm form = BatteryForm.UNKNOWN;
    private BatteryConnector connector = BatteryConnector.UNKNOWN;

    public Battery(String url) {
        super();

        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setVolt(double volt) {
        this.volt = volt;
    }

    public Integer getAmp() {
        return amp;
    }

    public void setAmp(int amp) {
        this.amp = amp;
    }

    public Double getWatt() {
        return watt;
    }

    public void setWatt(double watt) {
        this.watt = watt;
    }

    public void setDescription(String description) {
        this.description = description.replaceAll("Brand new", "");
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
        consolidate();
        return "| " + model +
                " | " + brand +
                " | " + String.join("<br/>", limitSet(partNo)) +
                " | " + description +
                " | " + format1(volt) + "V" +
                " | " + amp + "mAh" +
                " | " + format2(watt) + "W" +
                " | " + cells +
                " | " + url +
                " | " + String.join("<br/>", limitSet(models)) +
                " | " + connector +
                " | " + form +
                " |";
    }

    private String format1(Double value) {
        return String.format("%.1f", value);
    }

    private String format2(Double value) {
        return format1(value) + "0";
    }

    private Set<String> limitSet(Set<String> set) {
        if (set.size() < MAX_LIST_SIZE) {
            return set;
        }
        Set<String> out = new LinkedHashSet<>();
        Iterator<String> iter = set.iterator();
        while (out.size() < MAX_LIST_SIZE) {
            out.add(iter.next());
        }
        out.add("…");
        return out;
    }

    public static String tableHeader() {
        return "| Model | Brand | Part No. | Description | Volt | Amp | Watt | Cell | URL | Models | Connector | Form factor |\r\n" +
                "| ----- | ----- | -------- | ----------- | ---- | --- | ---- | ---- | --- | ------ | --------- | ----------- |";
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setPartNo(Set<String> partNo) {
        this.partNo = partNo;
    }

    public void setModels(Set<String> models) {
        this.models = models;
    }

    public void setForm(BatteryForm form) {
        this.form = form;
    }

    public void setConnector(BatteryConnector connector) {
        this.connector = connector;
    }

    public boolean isValid() {
        consolidate();

        if (volt < 7.4 || volt > 8.2) {
            return false;
        }

        if (amp < 5000) {
            return false;
        }

        if (type == null) {
            return true;
        }

        return BatteryType.LI_POLYMER.equals(type);
    }

    private void consolidate() {
        if (watt == null && amp != null) {
            double comp = amp * volt;
            setWatt(comp / 1000);
        }
        if (amp == null && watt != null) {
            double comp = watt * 100;
            comp = comp / volt;
            setAmp(((int) comp) * 10);
        }
    }

    public void setType(BatteryType type) {
        this.type = type;
    }
}
