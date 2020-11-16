package com.github.typingtanuki.batt.db;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryConnector;
import com.github.typingtanuki.batt.battery.BatteryForm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BatteryDB {
    private static final Pattern CSV_READER = Pattern.compile("^([^\\s]*),\\s*([^\\s]*),\\s*([^\\s]*)$");
    private static boolean initialized = false;

    private static final Map<String, BatteryConnector> DB_CONNECTOR = new HashMap<>();
    private static final Map<String, BatteryForm> DB_FORM = new HashMap<>();
    private static final Set<String> UNKNOWN_BATTERIES = new HashSet<>();

    private BatteryDB() {
        super();
    }

    private static void init() {
        if (initialized) {
            return;
        }
        Path dbPath = Paths.get("battery_db.csv");
        try {
            List<String> lines = Files.readAllLines(dbPath);
            boolean isHeader = true;
            for (String line : lines) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                Matcher matcher = CSV_READER.matcher(line);
                if (matcher.matches()) {
                    String model = matcher.group(1);
                    String connectorStr = matcher.group(2).strip();
                    if (!connectorStr.isBlank()) {
                        BatteryConnector connector = BatteryConnector.valueOf(connectorStr);
                        DB_CONNECTOR.put(model, connector);
                    }
                    String formStr = matcher.group(3).strip();
                    if (!formStr.isBlank()) {
                        BatteryForm form = BatteryForm.valueOf(formStr);
                        DB_FORM.put(model, form);
                    }
                } else {
                    throw new RuntimeException("Entry does not match pattern: " + line);
                }
            }
            initialized = true;
        } catch (IOException | RuntimeException e) {
            System.err.println("Failed loading database " + dbPath);
            e.printStackTrace(System.err);
            System.exit(12);
        }
    }

    public static void resolveConnector(Battery battery) {
        init();
        BatteryConnector connector = DB_CONNECTOR.get(battery.getModel());
        if (connector == null) {
            return;
        }
        battery.setConnector(connector);
    }

    public static void resolveForm(Battery battery) {
        init();
        BatteryForm form = DB_FORM.get(battery.getModel());
        if (form == null) {
            return;
        }
        battery.setForm(form);
    }

    public static void addBattery(Battery battery) {
        String model = battery.getModel();

        if (!DB_CONNECTOR.containsKey(model) || !DB_FORM.containsKey(model)) {
            UNKNOWN_BATTERIES.add(model);
        }

        DB_CONNECTOR.put(battery.getModel(), battery.getConnector());
        DB_FORM.put(battery.getModel(), battery.getForm());
    }

    public static void dump() {
        List<String> keys = new LinkedList<>(UNKNOWN_BATTERIES);
        keys.sort(String::compareTo);
        for (String key : keys) {
            String out = key + ", " +
                    handleMissing(DB_CONNECTOR.get(key)) + ", " +
                    handleMissing(DB_FORM.get(key));
            System.out.println(out);
        }
    }

    private static String handleMissing(Enum<?> enumKey) {
        if (enumKey == null) {
            return "UNKNOWN";
        }
        return enumKey.name();
    }
}
