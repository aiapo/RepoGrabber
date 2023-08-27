package com.troxal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Class: Config
 * Description: Gets config stuff like API keys so that they don't get exposed through Git and can be easily changed
 */
public class Config {
    // Just gets the API key from github-oauth.properties
    public static String getAuthToken() {
        Scanner scanner;
        try {
            scanner = new Scanner(new File("github-oauth.properties"));
            return scanner.nextLine();
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
}
