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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BatteryDB {
    private static final String BATTERY_FILE = "battery_db.csv";
    private static final Pattern CSV_READER = Pattern.compile("^([^\\s]*),\\s*([^\\s]*),\\s*([^\\s]*)$");
    private static boolean initialized = false;

    private static final Map<String, BatteryConnector> DB_CONNECTOR = new HashMap<>();
    private static final Map<String, BatteryForm> DB_FORM = new HashMap<>();

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
        DB_CONNECTOR.put(battery.getModel(), battery.getConnector());
        DB_FORM.put(battery.getModel(), battery.getForm());
    }

    public static void dump() throws IOException {
        StringBuilder out = new StringBuilder("MODEL, CONNECTOR, FORM");

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
