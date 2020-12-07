package com.github.typingtanuki.batt.db;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryConnector;
import com.github.typingtanuki.batt.battery.BatteryForm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.github.typingtanuki.batt.utils.Progress.*;

public final class BatteryDB {
    private static final String BATTERY_FILE = "battery_db.csv";
    private static final Map<String, BatteryConnector> DB_CONNECTOR = new HashMap<>();
    private static final Map<String, BatteryForm> DB_FORM = new HashMap<>();
    private static final Map<String, Boolean> DB_MATCH = new HashMap<>();
    private static final Map<String, Boolean> DB_SCANNED = new HashMap<>();
    private static final Set<String> ALL_KEYS = new HashSet<>();
    private static boolean initialized = false;

    private BatteryDB() {
        super();
    }

    private static void init() {
        if (initialized) {
            return;
        }
        Path dbPath = Paths.get(BATTERY_FILE);

        try {
            List<String> lines = Files.readAllLines(dbPath);
            boolean isHeader = true;
            for (String line : lines) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] parts = line.split(",");
                String model = parts[0].strip();
                ALL_KEYS.add(model);
                String connectorStr = parts[1].strip();
                boolean scanned = true;
                if (parts.length >= 5) {
                    scanned = Boolean.parseBoolean(parts[4].trim());
                }
                if (scanned) {
                    if (!connectorStr.isBlank()) {
                        BatteryConnector connector = BatteryConnector.valueOf(connectorStr);
                        DB_CONNECTOR.put(model, connector);
                    }
                    String formStr = parts[2].strip();
                    if (!formStr.isBlank()) {
                        BatteryForm form = BatteryForm.valueOf(formStr);
                        DB_FORM.put(model, form);
                    }
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

    public static void resolveModel(Battery battery) {
        init();
        for (String part : battery.getPartNo()) {
            String cleanedKey = Battery.cleanKey(part);
            if (ALL_KEYS.contains(cleanedKey)) {
                battery.setModel(cleanedKey);
                progress(IN_DB);
                return;
            }
        }
        progress(NOT_IN_DB);
    }

    public static void resolveForm(Battery battery) {
        init();
        BatteryForm form = DB_FORM.get(battery.getModel());
        if (form == null) {
            return;
        }
        battery.setForm(form);
    }

    public static void addBattery(Battery battery, boolean isMatch) {
        DB_CONNECTOR.put(battery.getModel(), battery.getConnector());
        DB_FORM.put(battery.getModel(), battery.getForm());
        DB_MATCH.put(battery.getModel(), isMatch);
        DB_SCANNED.put(battery.getModel(), true);
    }

    public static void dump() throws IOException {
        StringBuilder out = new StringBuilder("MODEL, CONNECTOR, FORM, MATCH, SCANNED");

        Set<String> keySet = new HashSet<>();
        keySet.addAll(DB_CONNECTOR.keySet());
        keySet.addAll(DB_FORM.keySet());

        List<String> keys = new ArrayList<>(keySet);
        keys.sort(String::compareTo);
        for (String key : keys) {
            out.append(System.lineSeparator());
            out.append(key);
            out.append(", ");
            out.append(handleMissing(DB_CONNECTOR.get(key)));
            out.append(", ");
            out.append(handleMissing(DB_FORM.get(key)));
            out.append(", ");
            out.append(DB_MATCH.getOrDefault(key, false));
            out.append(", ");
            out.append(DB_SCANNED.getOrDefault(key, false));
        }

        Files.write(Paths.get(BATTERY_FILE), out.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String handleMissing(Enum<?> enumKey) {
        if (enumKey == null) {
            return "UNKNOWN";
        }
        return enumKey.name();
    }
}
