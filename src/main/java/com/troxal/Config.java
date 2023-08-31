package com.troxal;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class: Config
 * Description: Gets config stuff like API keys so that they don't get exposed through Git and can be easily changed
 */
public class Config {
    // Just gets the API key from config/github-oauth.properties
    public static String getAuthToken() {
        try {
            FileReader filereader = new FileReader("config/github-oauth.properties");
            int i;
            StringBuilder sb = new StringBuilder();
            while ((i = filereader.read()) != -1) {
                sb.append((char)i);
            }
            return sb.toString();
        } catch (FileNotFoundException ex) {
            System.out.println("File not found: "+ex);
            try {
                Files.createFile(Path.of("config/github-oauth.properties"));
            }catch(IOException e){
                System.out.println("Can't create authKey file: "+e);
            }
            return "NOAUTHKEY";
        } catch (IOException e) {
            System.out.println("Error reading file: "+e);
            return "INVALIDAUTHKEY";
        }
    }
}
