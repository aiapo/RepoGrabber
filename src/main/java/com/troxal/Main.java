package com.troxal;

import com.troxal.manipulation.CSV;
import com.troxal.manipulation.Clone;
import com.troxal.manipulation.RefMine;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.FileWriter;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

import static java.lang.System.exit;

public class Main {
    public static void main(String[] args) throws IOException {
        boolean newQuery = true;
        while (newQuery) {
            Integer menuChoice = 0;
            Boolean success = false,headless=false;
            RepoGrab rg = null;
            String importCSV = "", languages = "", sDate = "", menuOrder = "", endDate = "",
                    errorInt = "Please enter a number.", errorString = "Please enter a valid String";
            Integer followers = 0, users = 0, percentLanguage = 0, totalCommit = 0, totalSize = 0, i = 0;

                System.out.println("-- Welcome to RepoGrabber! --\n");

                if (args.length > 0 && args[0].equals("headless")) {
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
                    headless = true;
                } else {
                    Scanner scn = new Scanner(System.in);

                    System.out.println("Do you want to import a CSV to skip the GitHub search? (y/n): ");
                    success = false;
                    while (!success) {
                        importCSV = scn.next();
                        if (importCSV.toLowerCase().equals("y") || importCSV.toLowerCase().equals("n"))
                            success = true;
                        else
                            System.out.println("Invalid, please enter a 'y' or 'n': ");
                    }

                    if (importCSV.toLowerCase().equals("n")) {
                        String followPrompt = "What is the minimum amount of followers on project wanted? (ex: '50'): ";
                        followers = paramGetterInt(followPrompt,errorInt,scn);

                        String langPrompt = "What language do you want to grab? (ex: 'java'): ";
                        languages = paramGetterString(langPrompt,errorString,scn);

                        String mentionablePrompt = "What is the minimum amount of mentionable users wanted? (ex: '20'): ";
                        users = paramGetterInt(mentionablePrompt,errorInt,scn);

                        String percentPrompt = "What is the percentage of the language in the repo wanted? (ex. '51' means at >=51% is language): ";
                        percentLanguage = paramGetterInt(percentPrompt,errorInt,scn);

                        String commitPrompt = "What is minimum amount of commits wanted? (ex. '300'): ";
                        totalCommit = paramGetterInt(commitPrompt, errorInt,scn);

                        String bytesPrompt = "What is minimum size in bytes of repo wanted? (ex. '5000'): ";
                        totalSize = paramGetterInt(bytesPrompt,errorInt, scn);

                        String startPrompt = "What is start date of repos wanted? (ex. '2010-01-01'): ";
                        sDate = paramGetterString(startPrompt,errorString,scn);

                        String endPrompt = "What is end date of repos wanted? (ex. '2010-01-01'): ";
                        endDate = paramGetterString(endPrompt,errorString,scn);

                    }
                }

                System.out.println("\n** Grabbing repos!");
                if (importCSV.toLowerCase().equals("n")) {
                    rg = new RepoGrab(followers, languages, users, percentLanguage, totalCommit, totalSize, sDate, endDate);
                } else {
                    rg = new RepoGrab(headless);
                }
                System.out.println("\n** Grabbed repos!");

                while (menuChoice != 5 && menuChoice != 6) {
                    if (menuOrder == "") {

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
                        if (i < menuOrderS.length) {
                            menuChoice = Integer.valueOf(menuOrderS[i]);
                            i++;
                        } else
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
                            CSV.create(rg.getRepos(),headless);
                            metaData(followers,languages,users,percentLanguage,totalCommit,totalSize,sDate,rg,endDate);
                            break;
                        case 4:
                            RefMine.calculate(rg.getRepos(),false);
                        case 5:
                            break;
                        case 6:
                            newQuery = false;
                            break;
                    }

                }
            }
    }
    // Uses given file name to create a txt file for query metadata
    public static void metaData(int followers, String languages, int users, int percentLanguage, int totalCommit, int totalSize,
                                String sDate, RepoGrab rg, String endDate){
        try {
            FileWriter paramWrite = new FileWriter("results/" + CSV.fileName + "_metadata.txt");
            paramWrite.write("+Min Followers: " + followers + "\n+Language: " + languages +
                    "\n+Min Mentionable Users: " + users + " \n+Percent of Language: " + percentLanguage +
                    "\n+Min Commits: " + totalCommit + "\n+Min Size in Bytes: " + totalSize + "\n+Start Date: " + sDate +
                    "\nEnd Date: " + endDate + "\n\n+Added Repos: " + rg.getAddedRepos() + "\n+Ignored Repos: " + rg.getIgnoredRepos() +
                    "\n\n" + java.time.LocalDate.now() + " " + java.time.LocalTime.now().truncatedTo(ChronoUnit.MINUTES));
            paramWrite.close();
        } catch (IOException e) {
            System.out.println("Metadata output error");
        }
    }

    public static int paramGetterInt(String prompt, String error, Scanner scn){
        int param = 0;
        System.out.println(prompt);
        boolean success = false;
        while (!success) {
            try {
                param = Integer.parseInt(scn.next());
                success = true;
            } catch (NumberFormatException e) {
                System.out.println(error);
            }
        }
        return param;
    }

    public static String paramGetterString(String prompt, String error, Scanner scn){
        String param = "";
        System.out.println(prompt);
        boolean success = false;
        while (!success) {
            try {
                param = scn.next();
                success = true;
            } catch (NumberFormatException e) {
                System.out.println(error);
            }
        }
        return param;
    }
}