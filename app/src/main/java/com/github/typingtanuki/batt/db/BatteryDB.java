package com.github.typingtanuki.batt.db;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryConnector;
import com.github.typingtanuki.batt.battery.BatteryForm;
import com.github.typingtanuki.batt.exceptions.NoPartException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public final class BatteryDB {
    private static final String BATTERY_FILE = "battery_db.csv";
    private static final Map<String, BatteryConnector> DB_CONNECTOR = new HashMap<>();
    private static final Map<String, Boolean> DB_MATCH = new HashMap<>();
    private static final Map<String, BatteryForm> DB_FORM = new HashMap<>();
    private static final Map<String, String> DB_SIZE = new HashMap<>();
    private static final Map<String, Boolean> DB_SCANNED = new HashMap<>();
    private static final Map<String, String> DB_MAKER = new HashMap<>();
    private static boolean initialized = false;

    private static final Pattern SPECIAL_CHARS = Pattern.compile("[/.\\s|\\-+#]");

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
                String model;
                try {
                    model = Battery.cleanPartNo(parts[0].strip());
                } catch (NoPartException e) {
                    continue;
                }
                String connectorStr = parts[1].strip();

                String size = "";

                if (parts.length >= 6) {
                    size = parts[5].trim();
                }
                if (!connectorStr.isBlank()) {
                    BatteryConnector connector = BatteryConnector.valueOf(connectorStr);
                    BatteryConnector inDb = DB_CONNECTOR.get(model);
                    if (inDb == null || !BatteryConnector.UNKNOWN.equals(connector)) {
                        DB_CONNECTOR.put(model, connector);
                    }
                }
                String formStr = parts[2].strip();
                try {
                    if (!formStr.isBlank()) {
                        BatteryForm form = BatteryForm.valueOf(formStr);
                        BatteryForm inDb = DB_FORM.get(model);
                        if (inDb == null || !BatteryForm.UNKNOWN.equals(form)) {
                            DB_FORM.put(model, form);
                        }
                    }
                    if (!size.isBlank()) {
                        DB_SIZE.put(model, size);
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException("Could not load line: " + line, e);
                }
            }
            initialized = true;
        } catch (IOException | RuntimeException e) {
            System.err.println("Failed loading database " + dbPath);
            e.printStackTrace(System.err);
            System.exit(12);
        }
    }

    public static void resolveConnector(Battery battery) throws NoPartException {
        init();
        BatteryConnector connector = DB_CONNECTOR.get(battery.getModel());
        if (connector == null) {
            return;
        }
        battery.setConnector(connector, battery.getModel());
    }

    public static void resolveForm(Battery battery) throws NoPartException {
        init();
        BatteryForm form = DB_FORM.get(battery.getModel());
        if (form == null) {
            return;
        }
        battery.setForm(form, battery.getModel());
    }

    public static void resolveSize(Battery battery) throws NoPartException {
        if (!battery.getSize().isBlank()) {
            return;
        }
        init();
        String size = DB_SIZE.get(battery.getModel());
        if (size == null) {
            return;
        }
        battery.setSize(size);
    }

    public static String formatModelName(String rawName) {
        return SPECIAL_CHARS
                .matcher(rawName.strip())
                .replaceAll("_")
                .replaceAll("_+","_")
                .replaceAll("^_*","")
                .replaceAll("^\\(*","")
                .replaceAll("^\\[*","")
                .replaceAll("_*$","")
                .replaceAll("]*$","")
                .replaceAll("\\)*$","");
    }

    public static void addBattery(Battery battery, boolean isMatch) {
        for (String rawId : battery.getPartNo()) {
            rawId = formatModelName(rawId);

            DB_MATCH.put(rawId, isMatch && DB_MATCH.getOrDefault(rawId, true));
            DB_SCANNED.put(rawId, true);

            DB_CONNECTOR.put(rawId, bester(battery.getConnector(), DB_CONNECTOR.get(rawId)));
            DB_FORM.put(rawId, bester(battery.getForm(), DB_FORM.get(rawId)));
            DB_SIZE.put(rawId, bester(battery.getSize(), DB_SIZE.get(rawId)));
            DB_MAKER.put(rawId, battery.getMaker().getName());
        }
    }

    private static BatteryConnector bester(BatteryConnector a, BatteryConnector b) {
        if (b == null) {
            return a;
        }
        if (a == null) {
            return b;
        }
        if (a.equals(BatteryConnector.UNKNOWN)) {
            return b;
        }
        return a;
    }

    private static BatteryForm bester(BatteryForm a, BatteryForm b) {
        if (b == null) {
            return a;
        }
        if (a == null) {
            return b;
        }
        if (a.equals(BatteryForm.UNKNOWN)) {
            return b;
        }
        return a;
    }

    private static String bester(String a, String b) {
        if (b == null || b.isBlank()) {
            return a;
        }
        if (a == null || a.isBlank()) {
            return b;
        }
        return a;
    }

    public static void dump() throws IOException {
        StringBuilder out = new StringBuilder("MODEL, CONNECTOR, FORM, MATCH, SCANNED, SIZE, MAKER");

        Set<String> keySet = new HashSet<>();
        keySet.addAll(DB_CONNECTOR.keySet());
        keySet.addAll(DB_FORM.keySet());
        keySet.addAll(DB_SIZE.keySet());
        keySet.addAll(DB_MATCH.keySet());
        keySet.addAll(DB_MAKER.keySet());

        List<String> keys = new ArrayList<>(keySet);
        keys.sort(String::compareTo);

        dump(out, keys, true);
        dump(out, keys, false);

        Files.write(Paths.get(BATTERY_FILE), out.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void dump(StringBuilder out, List<String> keys, boolean matching) {
        for (String key : keys) {
            boolean matched = DB_MATCH.getOrDefault(key, false);
            if (matched == matching) {
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
                out.append(", ");
                out.append(DB_SIZE.getOrDefault(key, ""));
                out.append(", ");
                out.append(DB_MAKER.getOrDefault(key, ""));
            }
        }
    }

    private static String handleMissing(Enum<?> enumKey) {
        if (enumKey == null) {
            return "UNKNOWN";
        }
        return enumKey.name();
    }
}
