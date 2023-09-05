package com.troxal.request;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;

public class Scrape {
    public static Document scrapePage(String address){
        try{
            Connection.Response execute = Jsoup.connect(address).execute();
            return Jsoup.parse(execute.body());
        } catch (IOException e) {
            System.out.println("Error scrapping: "+e);
            return null;
        }
    }

}
