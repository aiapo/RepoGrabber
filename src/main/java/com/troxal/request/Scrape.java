package com.troxal.request;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;

public class Scrape {
    public static Connection.Response scrapePage(String address){
        try{
            return Jsoup.connect(address).execute();
        } catch (IOException e) {
            System.out.println("Error scrapping: "+e);
            return null;
        }
    }

}
