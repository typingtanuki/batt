package com.github.typingtanuki.batt.battery;

import com.github.typingtanuki.batt.validator.*;

import java.util.*;

public class Battery {
    private static final Condition[] CONDITIONS = new Condition[]{
            new Condition("panel_mate",
                    new VoltageValidator(7.4, 8.2),
                    new AmperageValidator(1_000, 1_000_000),
                    new ConnectorValidator(false, BatteryConnector.PANEL_MATE),
                    new TypeValidator(true, BatteryType.LI_POLYMER),
                    new FormValidator(true, BatteryForm.SQUARE, BatteryForm.RECTANGLE, BatteryForm.FAT)),
            new Condition("pin10",
                    new VoltageValidator(7.4, 8.2),
                    new AmperageValidator(1_000, 1_000_000),
                    new ConnectorValidator(true, BatteryConnector.PIN_10, BatteryConnector.PANEL_MATE),
                    new TypeValidator(true, BatteryType.LI_POLYMER),
                    new FormValidator(true, BatteryForm.SQUARE, BatteryForm.RECTANGLE, BatteryForm.FAT)),
            new Condition("pin4",
                    new VoltageValidator(11, 15),
                    new AmperageValidator(1_000, 1_000_000),
                    new ConnectorValidator(true, BatteryConnector.PIN_4),
                    new TypeValidator(true, BatteryType.LI_POLYMER),
                    new FormValidator(true, BatteryForm.SQUARE, BatteryForm.RECTANGLE, BatteryForm.FAT))
    };

    private final Set<String> matchedConditions = new HashSet<>();
    private final String currentUrl;
    private final Set<Source> sources = new HashSet<>();
    private final Set<String> brands = new HashSet<>();
    private final Set<String> partNo = new HashSet<>();

    private Double volt;
    private Integer amp;
    private Double watt;
    private int cells;
    private BatteryType type;
    private BatteryForm form = BatteryForm.UNKNOWN;
    private BatteryConnector connector = BatteryConnector.UNKNOWN;

    private String model;

    public Battery(Source source) {
        super();

        sources.add(source.compact());
        currentUrl = source.getUrl();
    }

    public static String cleanKey(String part) {
        return part.strip().toUpperCase(Locale.ENGLISH).replaceAll("[/.\\s|\\-+]", "_");
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

    public int getCells() {
        return cells;
    }

    public void setCells(int cells) {
        this.cells = cells;
    }

    public void setBrand(String brand) {
        for (String b : brand.split(" ")) {
            this.brands.add(b.strip());
        }
    }

    public boolean isValid() {
        consolidate();
        matchedConditions.clear();

        for (Condition condition : CONDITIONS) {
            if (condition.isValid(this)) {
                matchedConditions.add(condition.getName());
            }
        }

        return !matchedConditions.isEmpty();
    }

    public void consolidate() {
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
        if (form == BatteryForm.UNKNOWN) {
            return;
        }

        this.form = form;
    }

    public BatteryConnector getConnector() {
        return connector;
    }

    public void setConnector(BatteryConnector connector) {
        if (connector == BatteryConnector.UNKNOWN) {
            return;
        }

        this.connector = connector;
    }

    public Set<String> getMatchedConditions() {
        return matchedConditions;
    }

    public void mergeWith(Battery battery) {
        partNo.addAll(battery.partNo);
        sources.addAll(battery.sources);
        setForm(battery.getForm());
        setConnector(battery.getConnector());

        if (battery.cells == 0) {
            cells = battery.cells;
        }
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public String getModel() {
        if (model != null) {
            return model;
        }

        if (partNo.isEmpty()) {
            throw new IllegalStateException("No model");
        }
        List<String> mods = new ArrayList<>(partNo);
        mods.sort(Comparator.naturalOrder());
        setModel(mods.get(0));
        return model;
    }

    public void setModel(String model) {
        if (this.model != null) {
            throw new IllegalStateException("Model already set");
        }
        if (model.isBlank()) {
            throw new IllegalStateException("Model is blank");
        }
        this.model = cleanKey(model);
    }

    public Set<String> getPartNo() {
        return partNo;
    }

    public void addPartNo(Set<String> partNo) {
        for (String part : partNo) {
            if (!part.isBlank()) {
                this.partNo.add(part.strip().toUpperCase(Locale.ENGLISH));
            }
        }
    }

    public Set<Source> getSources() {
        return sources;
    }

    public Set<String> getBrands() {
        return brands;
    }
}
