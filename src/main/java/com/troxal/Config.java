package com.troxal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class: Config
 * Description: Gets config stuff like API keys so that they don't get exposed through Git and can be easily changed
 */
public class Config {
    // Just gets an environment variable
    public static String get(String environmentVariable) {
        Map<String,String> configEnv = byBufferedReader("config/.env");
        return configEnv.get(environmentVariable);
    }

    private static Map<String, String> byBufferedReader(String filePath) {
        HashMap<String, String> map = new HashMap<>();
        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while ((line = reader.readLine()) != null) {
                if(!line.startsWith("##")){
                    String[] keyValuePair = line.split(" = ", 2);
                    if (keyValuePair.length > 1) {
                        String key = keyValuePair[0];
                        String value = keyValuePair[1].replaceAll("\"", "");

                        map.put(key, value);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}