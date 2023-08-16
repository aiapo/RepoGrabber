package com.troxal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class getConfig {

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
