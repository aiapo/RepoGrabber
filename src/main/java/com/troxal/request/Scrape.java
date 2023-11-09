package com.troxal.request;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Scrape {
    private static Integer tryCount = 0;
    public static Connection.Response scrapePage(String address){
        try{
            Connection.Response s = Jsoup.connect(address).execute();
            return s;
        } catch (IOException e) {
            System.out.println("Error scrapping: "+e);
            if(e.getMessage().contains("Status=429")) {
                try {
                    // Wait determined by 'retry-after' seconds to comply with API limits
                    TimeUnit.SECONDS.sleep(20);
                } catch (InterruptedException ez) {
                    System.out.println("[ERROR] Error trying to wait: \n" + ez);
                }
                if (tryCount++ == 5) {
                    return scrapePage(address);
                }
            }
            return null;
        }
    }

}
