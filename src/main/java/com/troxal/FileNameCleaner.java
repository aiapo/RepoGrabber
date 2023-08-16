package com.troxal;

public class FileNameCleaner {
    public static String cleanFileName(String badFileName) {
        String cleanName = badFileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        return cleanName.toString();
    }
}