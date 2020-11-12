package com.github.typingtanuki.batt.battery;

import com.github.typingtanuki.batt.validator.*;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class Battery {
    private static final int MAX_LIST_SIZE = 10;

    private static final Validator[] VALIDATORS = new Validator[]{
            new VoltageValidator(7.4, 8.2),
            new AmperageValidator(5_000, 1_000_000),
            new TypeValidator(true, BatteryType.LI_POLYMER),
            new FormValidator(true, BatteryForm.SQUARE, BatteryForm.RECTANGLE, BatteryForm.FAT),
            new ConnectorValidator(true, BatteryConnector.PANELMATE)
    };

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

    public static String tableHeader() {
        return "| Model | Brand | Part No. | Description | Volt | Amp | Watt | Cell | URL | Models | Connector | Form factor |\r\n" +
                "| ----- | ----- | -------- | ----------- | ---- | --- | ---- | ---- | --- | ------ | --------- | ----------- |";
    }

    public String getUrl() {
        return url;
    }

    public double getVolt() {
        return volt;
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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
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
                " | " + formatUrl() +
                " | " + String.join("<br/>", limitSet(models)) +
                " | " + connector +
                " | " + form +
                " |";
    }

    private String formatUrl() {
        return "[NewLaptopAccessory](" + url + "){:target=\"_blank\"}";
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

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setPartNo(Set<String> partNo) {
        this.partNo = partNo;
    }

    public void setModels(Set<String> models) {
        this.models = models;
    }

    public boolean isValid() {
        consolidate();

        for (Validator validator : VALIDATORS) {
            if (!validator.isValid(this)) {
                return false;
            }
        }

        return true;
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

    public BatteryType getType() {
        return type;
    }

    public void setType(BatteryType type) {
        this.type = type;
    }

    public BatteryForm getForm() {
        return form;
    }

    public void setForm(BatteryForm form) {
        this.form = form;
    }

    public BatteryConnector getConnector() {
        return connector;
    }

    public void setConnector(BatteryConnector connector) {
        this.connector = connector;
    }
}
