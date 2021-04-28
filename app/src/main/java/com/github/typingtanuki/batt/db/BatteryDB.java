package com.github.typingtanuki.batt.db;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryConnector;
import com.github.typingtanuki.batt.battery.BatteryForm;
import com.github.typingtanuki.batt.exceptions.NoPartException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class BatteryDB {
    private static final String BATTERY_FILE = "battery_db.csv";
    private static final String BATTERY_ZIP_FILE = "battery_db.zip";
    private static final Map<String, BatteryConnector> DB_CONNECTOR = new HashMap<>();
    private static final Map<String, Boolean> DB_MATCH = new HashMap<>();
    private static final Map<String, BatteryForm> DB_FORM = new HashMap<>();
    private static final Map<String, String> DB_SIZE = new HashMap<>();
    private static final Map<String, Boolean> DB_SCANNED = new HashMap<>();
    private static final Map<String, String> DB_MAKER = new HashMap<>();
    private static final Map<String, String> DB_PARTS = new HashMap<>();
    private static final Map<String, String> ALIAS = new HashMap<>();
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[/.\\s|\\-+#]");
    private static final int MIN_MODEL_LENGTH = 5;
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
            Files.deleteIfExists(dbPath);
            unzip();

            List<String> lines = Files.readAllLines(dbPath);
            boolean isHeader = true;
            int lineIdx = 1;
            for (String line : lines) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                try {
                    parseLine(line);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Failure reading DB at line " + lineIdx + "\r\n" + line,
                            e);
                }
                lineIdx++;
            }
            initialized = true;
        } catch (IOException | RuntimeException e) {
            System.err.println("Failed loading database " + dbPath);
            e.printStackTrace(System.err);
            System.exit(12);
        }
    }

    private static void zip() throws IOException {
        byte[] buffer = new byte[1024];

        File fileToZip = new File(BATTERY_FILE);
        try (FileOutputStream fos = new FileOutputStream(BATTERY_ZIP_FILE);
             ZipOutputStream zipOut = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(fileToZip)) {
            zipOut.setLevel(Deflater.BEST_COMPRESSION);

            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zipOut.write(buffer, 0, length);
            }
        }
    }

    private static void unzip() throws IOException {
        byte[] buffer = new byte[1024];

        File fileToExtract = new File(BATTERY_FILE);
        try (FileInputStream fis = new FileInputStream(BATTERY_ZIP_FILE);
             ZipInputStream zis = new ZipInputStream(fis);
             FileOutputStream fos = new FileOutputStream(fileToExtract)) {
            ZipEntry zipEntry = zis.getNextEntry();
            if (zipEntry == null) {
                return;
            }

            if (!zipEntry.getName().equals(BATTERY_FILE)) {
                throw new IOException("Bad zip, found :" + zipEntry.getName());
            }

            int length;
            while ((length = zis.read(buffer)) >= 0) {
                fos.write(buffer, 0, length);
            }
            zis.closeEntry();
        }
    }

    @SuppressWarnings("StringSplitter")
    private static void parseLine(String line) {
        String[] parts = line.split(",", -1);
        String model;
        try {
            model = Battery.cleanPartNo(parts[0].strip());
        } catch (NoPartException e) {
            return;
        }
        String connectorStr = parts[1].strip();

        String size = "";

        if (!connectorStr.isBlank()) {
            BatteryConnector connector = BatteryConnector.valueOf(connectorStr);
            BatteryConnector inDb = DB_CONNECTOR.get(model);
            if (inDb == null || !BatteryConnector.UNKNOWN.equals(connector)) {
                DB_CONNECTOR.put(model, connector);
            }
        }
        String formStr = parts[2].strip();
        if (!formStr.isBlank()) {
            BatteryForm form = BatteryForm.valueOf(formStr);
            BatteryForm inDb = DB_FORM.get(model);
            if (inDb == null || !BatteryForm.UNKNOWN.equals(form)) {
                DB_FORM.put(model, form);
            }
        }

        // 3/4 are DB state tags

        if (parts.length >= 6) {
            size = parts[5].trim();
        }
        if (!size.isBlank()) {
            DB_SIZE.put(model, size);
        }

        // 6 is marker

        String aliases = "";
        if (parts.length >= 8) {
            aliases = parts[7].trim();
        }
        if (!aliases.isBlank()) {
            aliases = aliases.substring(1, aliases.length() - 1);
            String[] extracted = aliases.split(",", -1);
            for (String extract : extracted) {
                ALIAS.put(extract.strip(), model);
            }
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
                .replaceAll("_+", "_")
                .replaceAll("^_*", "")
                .replaceAll("^\\(*", "")
                .replaceAll("^\\[*", "")
                .replaceAll("_*$", "")
                .replaceAll("]*$", "")
                .replaceAll("\\)*$", "");
    }

    public static void addBattery(Battery battery, boolean isMatch) {
        String rawId = "NO_ID";
        try {
            rawId = battery.getModel();
        } catch (NoPartException e) {
            // Nothing to do
        }

        DB_MATCH.put(rawId, isMatch && DB_MATCH.getOrDefault(rawId, true));
        DB_SCANNED.put(rawId, true);

        DB_CONNECTOR.put(rawId, bester(battery.getConnector(), DB_CONNECTOR.get(rawId)));
        DB_FORM.put(rawId, bester(battery.getForm(), DB_FORM.get(rawId)));
        DB_SIZE.put(rawId, bester(battery.getSize(), DB_SIZE.get(rawId)));
        DB_MAKER.put(rawId, battery.getMaker().getName());
        DB_PARTS.put(rawId, String.join(", ", battery.getPartNo()));
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

        Files.deleteIfExists(Paths.get(BATTERY_ZIP_FILE));
        zip();
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
                out.append(", [");
                out.append(DB_PARTS.getOrDefault(key, ""));
                out.append("]");
            }
        }
    }

    private static String handleMissing(Enum<?> enumKey) {
        if (enumKey == null) {
            return "UNKNOWN";
        }
        return enumKey.name();
    }

    public static String matchEntry(String part) {
        String aliased = ALIAS.get(part);
        if (aliased != null) {
            return aliased;
        }

        String v = null;
        int len = -1;
        for (String value : DB_CONNECTOR.keySet()) {
            if (part.startsWith(value)) {
                int l = value.length();
                if (l < MIN_MODEL_LENGTH) {
                    continue;
                }
                if (len == -1 || len > l) {
                    len = l;
                    v = value;
                }
            }
        }

        return v;
    }
}
