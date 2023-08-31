package com.troxal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
            System.out.println("File not found: "+ex);
            try {
                Files.createFile(Path.of("github-oauth.properties"));
            }catch(IOException e){
                System.out.println("Can't create authKey file: "+e);
            }
            return "NOAUTHKEY";
        }
    }
}
