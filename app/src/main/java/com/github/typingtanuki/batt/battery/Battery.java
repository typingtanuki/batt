package com.github.typingtanuki.batt.battery;

import com.github.typingtanuki.batt.validator.*;

import java.util.*;

import static com.github.typingtanuki.batt.utils.Progress.progress;

public class Battery {
    private static final int MAX_LIST_SIZE = 10;

    private static final Validator[] VALIDATORS = new Validator[]{
            new VoltageValidator(7.4, 8.2),
            new AmperageValidator(5_000, 1_000_000),
            new TypeValidator(true, BatteryType.LI_POLYMER),
            new FormValidator(true, BatteryForm.SQUARE, BatteryForm.RECTANGLE, BatteryForm.FAT),
            new ConnectorValidator(true, BatteryConnector.PIN_10)
    };

    private final String url;
    private Double volt;
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
        return "| Brand | Power | Cell | Connector | Form factor | Part No. | Models | URL |\r\n" +
                "| ----- | ----- | ---- | --------- | ----------- | -------- | ------ | --- |";
    }

    public String getUrl() {
        return url;
    }

    public Double getVolt() {
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
        return "| " + brand +
                " | " + makeList(Arrays.asList(format1(volt) + "V", amp + "mAh", format2(watt) + "W")) +
                " | " + cells +
                " | " + connector +
                " | " + form +
                " | " + makeList(partNo) +
                " | " + makeList(models) +
                " | " + formatUrl() +
                " |";
    }

    private String makeList(Collection<String> list) {
        StringBuilder out = new StringBuilder();
        out.append("<ul>");
        for (String s : list) {
            out.append("<li>").append(s).append("</li>");
        }
        out.append("</ul>");
        return out.toString();
    }

    private String formatUrl() {
        return "<a href=\"" + url + "\" target=\"_blank\">NewLaptopAccessory</a>";
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
        this.partNo = filterSet(partNo);
    }

    public void setModels(Set<String> models) {
        this.models = filterSet(models);
    }

    private Set<String> filterSet(Set<String> set) {
        Set<String> out = new LinkedHashSet<>();
        for (String s : set) {
            if (s.isBlank()) {
                continue;
            }
            boolean isShort = true;
            for (String s2 : set) {
                if (s2.isBlank()) {
                    continue;
                }
                if (s.startsWith(s2) && !s.equals(s2)) {
                    isShort = false;
                    break;
                }
            }
            if (isShort) {
                out.add(s);
            }
        }
        return out;
    }

    public boolean isValid() {
        consolidate();

        for (Validator validator : VALIDATORS) {
            if (!validator.isValid(this)) {
                progress("_");
                return false;
            }
        }

        progress("#");
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
