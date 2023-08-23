package com.troxal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Class: getConfig
 * Description: Gets config stuff like API keys so that they don't get exposed through Git and can be easily changed
 */
public class getConfig {
    // Just gets the API key from keys.config
    public static String getKey() {
        Scanner scanner;
        try {
            scanner = new Scanner(new File("keys.config"));
            return scanner.nextLine();
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

}
