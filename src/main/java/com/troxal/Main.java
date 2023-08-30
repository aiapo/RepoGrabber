package com.troxal;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import com.troxal.manipulation.CSV;
import com.troxal.manipulation.Clone;
import com.troxal.manipulation.RefMine;
import io.github.cdimascio.dotenv.Dotenv;
import static java.lang.System.exit;

public class Main {
    public static void main(String[] args) throws IOException {
        Integer menuChoice = 0;
        Boolean success = false;
        RepoGrab rg = null;
        String importCSV="",languages="",sDate="",menuOrder="";
        Integer followers=0,users=0,percentLanguage=0,totalCommit=0,totalSize=0,i=0;
        boolean newQuery = true;
        while (newQuery) {

        System.out.println("-- Welcome to RepoGrabber! --\n");

        if (args.length > 0&&args[0].equals("headless")) {
                Dotenv dotenv = Dotenv.configure()
                    .directory("/config")
                    .filename(".env")
                    .load();

                importCSV = dotenv.get("IMPORT_CSV");
                languages = dotenv.get("LANGUAGE_WANTED");
                sDate = dotenv.get("START_CREATEDATE");
                followers = Integer.valueOf(dotenv.get("PROJECT_FOLLOWERS"));
                users = Integer.valueOf(dotenv.get("PROJECT_MENTIONABLE"));
                percentLanguage = Integer.valueOf(dotenv.get("PROJECT_PERCENT"));
                totalCommit = Integer.valueOf(dotenv.get("PROJECT_TOTALCOMMIT"));
                totalSize = Integer.valueOf(dotenv.get("PROJECT_TOTALSIZE"));
                menuOrder = dotenv.get("MENU_ORDER");
        }else{
            Scanner scn = new Scanner(System.in);

            System.out.println("Do you want to import a CSV to skip the GitHub search? (y/n): ");
            success=false;
            while(!success){
                importCSV = scn.next();
                if(importCSV.toLowerCase().equals("y")||importCSV.toLowerCase().equals("n"))
                    success = true;
                else
                    System.out.println("Invalid, please enter a 'y' or 'n': ");
            }

            if(importCSV.toLowerCase().equals("n")){
                System.out.println("What is the minimum amount of followers on project wanted? (ex: '50'): ");
                success=false;
                while(!success){
                    try {
                        followers = Integer.valueOf(scn.next());
                        success = true;
                    }catch(NumberFormatException e){
                        System.out.println("Invalid, please enter a number (ex: '50'): ");
                    }
                }
                System.out.println("What language do you want to grab? (ex: 'java'): ");
                success=false;
                while(!success){
                    try {
                        languages = scn.next();
                        success = true;
                    }catch(NumberFormatException e){
                        System.out.println("Invalid, please enter a string (ex: 'java'): ");
                    }
                }
                System.out.println("What is the minimum amount of mentionable users wanted? (ex: '20'): ");
                success=false;
                while(!success){
                    try {
                        users = Integer.valueOf(scn.next());
                        success = true;
                    }catch(NumberFormatException e){
                        System.out.println("Invalid, please enter a number (ex: '20'): ");
                    }
                }
                System.out.println("What is the percentage of the language in the repo wanted? (ex. '51' means at >=51% is language): ");
                success=false;
                while(!success){
                    try {
                        percentLanguage = Integer.valueOf(scn.next());
                        success = true;
                    }catch(NumberFormatException e){
                        System.out.println("Invalid, please enter a number (ex: '51'): ");
                    }
                }
                System.out.println("What is minimum amount of commits wanted? (ex. '300'): ");
                success=false;
                while(!success){
                    try {
                        totalCommit = Integer.valueOf(scn.next());
                        success = true;
                    }catch(NumberFormatException e){
                        System.out.println("Invalid, please enter a number (ex: '300'): ");
                    }
                }
                System.out.println("What is minimum size in bytes of repo wanted? (ex. '5000'): ");
                success=false;
                while(!success){
                    try {
                        totalSize = Integer.valueOf(scn.next());
                        success = true;
                    }catch(NumberFormatException e){
                        System.out.println("Invalid, please enter a number (ex: '5000'): ");
                    }
                }
                System.out.println("What is start date of repos wanted? (ex. '2010-01-01'): ");
                success=false;
                while(!success){
                    try {
                        sDate = scn.next();
                        success = true;
                    }catch(NumberFormatException e){
                        System.out.println("Invalid, please enter a string (ex: '2010-01-01'): ");
                    }
                }
            }
        }

        System.out.println("\n** Grabbing repos!");
        if(importCSV.toLowerCase().equals("n")){
            rg = new RepoGrab(followers,languages,users,percentLanguage,totalCommit,totalSize,sDate);
        }else{
            rg = new RepoGrab();
        }
        System.out.println("\n** Grabbed repos!");

            while (menuChoice != 5 && menuChoice != 6) {
                if(menuOrder=="") {

                    System.out.println("** Menu: **" +
                            "\n 1. Print all repos" +
                            "\n 2. Clone all repos" +
                            "\n 3. Export to CSV" +
                            "\n 4. Run RefMiner" +
                            "\n 5. New Query" +
                            "\n 6. Exit\n");
                    System.out.println("Choice: ");
                    Scanner scn = new Scanner(System.in);
                    menuChoice = Integer.valueOf(scn.next());
                } else {
                    String[] menuOrderS = menuOrder.split(",");
                    if(i<menuOrderS.length){
                        menuChoice = Integer.valueOf(menuOrderS[i]);
                        i++;
                    }else
                        exit(0);
                }
                switch (menuChoice) {
                    case 1:
                        System.out.println(rg.getRepos());
                        break;
                    case 2:
                        Clone.cloneRepos(rg.getRepos());
                        break;
                    case 3:
                        CSVManip.createCSV(rg.getRepos());
                        // Uses given file name to create a txt file for query metadata
                        try {
                            FileWriter paramWrite = new FileWriter(CSVManip.fileName + "_metadata.txt");
                            paramWrite.write("+Min Followers: " + followers + "\n+Language: " + languages +
                                    "\n+Min Mentionable Users: " + users + " \n+Percent of Language: " + percentLanguage +
                                    "\n+Min Commits: " + totalCommit + "\n+Min Size in Bytes: " + totalSize + "\n+Start Date: " + sDate +
                                    "\n\n+Added Repos: " + rg.getAddedRepos() + "\n+Ignored Repos: " + rg.getIgnoredRepos() +
                                    "\n\n" + java.time.LocalDate.now() + " " + java.time.LocalTime.now());
                            paramWrite.close();
                        } catch (IOException e) {
                            System.out.println("Metadata output error");
                        }
                        break;
                    case 4:
                        RefMine.calculate(rg.getRepos());
                    case 5:
                        break;
                    case 6:
                        newQuery = false;
                        break;
                }

        }

    }
}