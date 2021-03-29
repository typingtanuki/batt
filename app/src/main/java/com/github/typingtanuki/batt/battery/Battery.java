package com.github.typingtanuki.batt.battery;

import com.github.typingtanuki.batt.db.BatteryDB;
import com.github.typingtanuki.batt.exceptions.NoPartException;
import com.github.typingtanuki.batt.validator.Conditions;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.typingtanuki.batt.db.BatteryDB.formatModelName;

/**
 * A battery with its associated details
 */
public class Battery {
    private static final Pattern MAKERS;

    static {
        StringBuilder regex = new StringBuilder();
        for (MakerName name : MakerName.values()) {
            if (regex.length() > 0) {
                regex.append('|');
            }
            regex.append(name.name());
        }
        MAKERS = Pattern.compile(regex.toString());
    }

    /**
     * The conditions which are matched
     */
    private final Set<String> matchedConditions = new HashSet<>();
    /**
     * The URL of the page currently being parsed
     */
    private final String currentUrl;
    /**
     * The sources where this battery can be found
     */
    private final Set<Source> sources = new HashSet<>();
    /**
     * The brands for this battery
     */
    private final Set<String> brands = new HashSet<>();
    /**
     * The part numbers
     */
    private final Set<String> partNo = new HashSet<>();
    private final Set<Image> images = new HashSet<>();
    private final Maker maker;
    /**
     * The volt output of this battery
     */
    private Double volt;
    /**
     * The amperage of this battery
     */
    private Integer amp;
    /**
     * The watt output of this battery
     */
    private Double watt;
    /**
     * The type of battery power
     */
    private BatteryType type;
    /**
     * The shape of the battery
     */
    private BatteryForm form = BatteryForm.UNKNOWN;
    /**
     * The type of connector
     */
    private BatteryConnector connector = BatteryConnector.UNKNOWN;
    /**
     * A unique model ID for this battery (computed once)
     */
    private String model;
    private Double thickness;
    private Double height;
    private Double width;
    private String dbId;
    private Battery mergeTarget = null;
    private boolean isCompleted = false;

    public Battery(Maker maker, Source source) {
        super();

        this.maker = maker;
        sources.add(source.compact());
        currentUrl = source.getUrl();
    }

    /**
     * Clean a part number to avoid special chars and duplication
     */
    public static String cleanPartNo(String part) throws NoPartException {
        part = part.toUpperCase(Locale.ENGLISH);
        part = MAKERS.matcher(part).replaceAll("").strip();

        while (part.startsWith("_")) {
            part = part.substring(1);
        }
        while (part.contains("__")) {
            part = part.replaceAll("__", "_");
        }

        if (part.isBlank()) {
            throw new NoPartException();
        }
        return part;
    }

    public boolean isValid() {
        consolidate();

        matchedConditions.clear();

        if (isCompleted && images.isEmpty()) {
            return false;
        }

        String model;
        try {
            model = getModel();
        } catch (NoPartException e) {
            return false;
        }

        if (model.contains("円")) {
            return false;
        }
        if (model.length() < 2) {
            return false;
        }

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

    public void mergeWith(Battery toMerge) {
        Battery battery = toMerge.rewindMerges();
        if (this == battery) {
            return;
        }

        partNo.addAll(battery.partNo);
        sources.addAll(battery.sources);
        images.addAll(battery.images);

        String mergedDbId = dbId;
        if (mergedDbId == null) {
            mergedDbId = battery.dbId;
        }

        if (BatteryForm.UNKNOWN == form) {
            setForm(battery.getForm(), mergedDbId);
        } else if (BatteryForm.CUSTOM == battery.getForm()) {
            setForm(BatteryForm.CUSTOM, mergedDbId);
        }
        if (BatteryConnector.UNKNOWN == connector) {
            setConnector(battery.getConnector(), mergedDbId);
        } else if (BatteryConnector.CUSTOM == battery.getConnector()) {
            setConnector(BatteryConnector.CUSTOM, mergedDbId);
        }

        battery.setForm(form, mergedDbId);
        battery.setConnector(connector, mergedDbId);

        battery.mergeTarget = this;
        this.dbId = mergedDbId;
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

    public void setForm(BatteryForm form, String dbId) {
        if (form == BatteryForm.UNKNOWN) {
            return;
        }

        this.form = form;
        this.dbId = dbId;
    }

    public BatteryConnector getConnector() {
        return connector;
    }

    public void setConnector(BatteryConnector connector, String dbId) {
        if (connector == BatteryConnector.UNKNOWN) {
            return;
        }

        this.connector = connector;
        this.dbId = dbId;
    }

    public Set<String> getMatchedConditions() {
        return matchedConditions;
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public String getModel() throws NoPartException {
        if (model != null) {
            return model;
        }

        List<String> mods = new ArrayList<>(partNo.size());
        for(String part:partNo){
            String cleanPart = formatModelName(part);
            if(cleanPart.isBlank()){
                continue;
            }

            if(BatteryDB.hasEntry(cleanPart)){
                model=cleanPart;
                return model;
            }
            mods.add(cleanPart);
        }

        if (mods.isEmpty()) {
            model = "NO_ID";
            return model;
        }

        mods.sort(Comparator.naturalOrder());
        model = formatModelName(mods.get(0));
        return model;
    }

    public Set<String> getPartNo() {
        return partNo;
    }

    public void addPartNo(Collection<String> partNo) {
        String makerName = maker.getName().toUpperCase(Locale.ENGLISH);
        for (String part : partNo) {
            if (part.contains(",")) {
                throw new IllegalStateException("Part can not have a ','");
            }
            try {
                part = Battery.cleanPartNo(part);
                String partStr = part.strip();
                this.partNo.add(partStr);
                if (partStr.contains(makerName)) {
                    this.partNo.add(partStr.replaceAll(makerName, "").strip());
                }
            } catch (NoPartException e) {
                // Nothing to do
            }
        }
    }

    public Set<Source> getSources() {
        return sources;
    }

    public Set<String> getBrands() {
        return brands;
    }

    public String getSize() {
        if (thickness == null || height == null || width == null) {
            return "";
        }
        return width + " x " + height + " x " + thickness;
    }

    public void setSize(String size) {
        if (size.isBlank()) {
            return;
        }
        String[] parts = size.replace("mm", "").split("x");
        if (parts.length != 3) {
            throw new IllegalStateException("Invalid size: " + size);
        }
        double a = Double.parseDouble(parts[0].strip());
        double b = Double.parseDouble(parts[1].strip());
        double c = Double.parseDouble(parts[2].strip());

        List<Double> values = new ArrayList<>(3);
        values.add(a);
        values.add(b);
        values.add(c);
        values.sort(Double::compare);

        thickness = values.get(0);
        height = values.get(1);
        width = values.get(2);

        if (!form.equals(BatteryForm.CUSTOM)) {
            if (width / height > 1.5) {
                setForm(BatteryForm.RECTANGLE, null);
            } else {
                setForm(BatteryForm.SQUARE, null);
            }
            if (thickness > 30) {
                if (!form.equals(BatteryForm.CUSTOM)) {
                    setForm(BatteryForm.FAT, null);
                }
            }
        }
    }

    public void complete() {
        isCompleted = true;
    }

    public double[] getSizes() {
        return new double[]{
                width == null ? 0d : width,
                height == null ? 0d : height,
                thickness == null ? 0d : thickness};
    }

    public Set<Image> getImages() {
        return images;
    }

    public void addImage(String image) throws NoPartException {
        images.add(new Image(this, image));
    }

    public String baseUri() {
        Pattern a = Pattern.compile("^(https?://[^/]+)/.*$");
        Matcher matcher = a.matcher(currentUrl);
        if (!matcher.matches()) {
            throw new IllegalStateException("Could not extract URL from: " + currentUrl);
        }
        return matcher.group(1) + "/";
    }

    public Maker getMaker() {
        return maker;
    }

    public Set<String> allParts() {
        Set<String> out = new HashSet<>();
        out.add(model);
        out.addAll(partNo);
        return out;
    }

    public Battery rewindMerges() {
        Battery out = this;
        if (out.mergeTarget != null && out.mergeTarget != out) {
            out = out.mergeTarget;
        }
        return out;
    }
}
