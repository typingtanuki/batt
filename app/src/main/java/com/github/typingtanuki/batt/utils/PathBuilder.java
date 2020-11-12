package com.github.typingtanuki.batt.utils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class PathBuilder {
    private Path currentPath;
    private String fileName;
    private String extension;

    public PathBuilder(String root) {
        currentPath = Paths.get(root);
    }

    public PathBuilder withSubFolder(String subFolder) {
        currentPath = currentPath.resolve(subFolder);
        return this;
    }

    public PathBuilder withFileName(String fileName, boolean needsHashing) {
        if (!needsHashing) {
            this.fileName = fileName;
            return this;
        }

        this.fileName = hash(fileName);
        return this;
    }

    public PathBuilder withFileNamePrefix(String fileNamePrefix, boolean needsHashing) {
        if (fileName == null) {
            throw new IllegalStateException("File name is unset");
        }

        String prepared = fileNamePrefix;
        if (needsHashing) {
            prepared = hash(fileNamePrefix);
        }
        this.fileName = prepared + this.fileName;
        return this;
    }


    public PathBuilder withExtension(String extension) {
        this.extension = extension;
        return this;
    }

    public Path build() {
        if (fileName == null) {
            throw new IllegalStateException("File name is unset");
        }
        if (extension == null) {
            throw new IllegalStateException("Extension is unset");
        }

        return currentPath.resolve(fileName + extension);
    }


    private static String hash(String s) {
        String clean = s.toLowerCase(Locale.ENGLISH).replaceAll("[:./\\\\?&]", "_");
        String hashed = clean;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(clean.getBytes(StandardCharsets.UTF_8));
            hashed = hex(hash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Could not hash entry " + clean);
            e.printStackTrace(System.err);
            System.exit(12);
        }
        return hashed;
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02x", aByte));
        }
        return result.toString();
    }
}