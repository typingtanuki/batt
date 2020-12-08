package com.github.typingtanuki.batt.battery;

import com.github.typingtanuki.batt.validator.*;
import org.jsoup.nodes.Document;

import java.util.*;

/**
 * A battery with its associated details
 */
public class Battery {
    /** The conditions which are matched */
    private final Set<String> matchedConditions = new HashSet<>();
    /** The URL of the page currently being parsed */
    private final String currentUrl;
    /** The sources where this battery can be found */
    private final Set<Source> sources = new HashSet<>();
    /** The brands for this battery */
    private final Set<String> brands = new HashSet<>();
    /** The part numbers */
    private final Set<String> partNo = new HashSet<>();

    /** The volt output of this battery */
    private Double volt;
    /** The amperage of this battery */
    private Integer amp;
    /** The watt output of this battery*/
    private Double watt;
    /** The number of cells in this battery */
    private int cells;
    /** The type of battery power */
    private BatteryType type;
    /** The shape of the battery */
    private BatteryForm form = BatteryForm.UNKNOWN;
    /** The type of connector */
    private BatteryConnector connector = BatteryConnector.UNKNOWN;
    /** The parsed HTML for this battery */
    private Document sourcePage;

    /** A unique model ID for this battery (computed once) */
    private String model;

    /** Clean a part number to avoid special chars and duplication */
    public static String cleanPartNo(String part) {
        return part
                .strip()
                .toUpperCase(Locale.ENGLISH)
                .replaceAll("[/.\\s|\\-+]", "_");
    }

    public Battery(Source source) {
        super();

        sources.add(source.compact());
        currentUrl = source.getUrl();
    }

    public boolean isValid() {
        consolidate();
        matchedConditions.clear();

        Conditions.validate(this, matchedConditions);

        return !matchedConditions.isEmpty();
    }

    /**
     * Convert watt to amp or amp to watt
     */
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

    public void mergeWith(Battery battery) {
        partNo.addAll(battery.partNo);
        sources.addAll(battery.sources);
        setForm(battery.getForm());
        setConnector(battery.getConnector());

        if (battery.cells == 0) {
            cells = battery.cells;
        }
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
        this.model = cleanPartNo(model);
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

    public Document getSourcePage() {
        return sourcePage;
    }

    public void setSourcePage(Document sourcePage) {
        this.sourcePage = sourcePage;
    }
}
