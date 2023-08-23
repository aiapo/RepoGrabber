package com.troxal;

/**
 * Class: FileNameCleaner
 * Description: Cleans a file name so that it doesn't have invalid characters that Windows doesn't like
 */
public class FileNameCleaner {
    // Cleans the filename using regex to replace any invalid characters with an underscore
    public static String cleanFileName(String badFileName) {
        return badFileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }
}