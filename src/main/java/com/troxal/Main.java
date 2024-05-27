package com.troxal;

import com.troxal.database.Manager;
import com.troxal.jobs.RefMiner;
import com.troxal.manipulation.CSV;
import com.troxal.manipulation.Clone;
import com.troxal.manipulation.GitChanges;
import com.troxal.manipulation.RGDS;

import java.io.FileWriter;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

import static java.lang.System.exit;

public class Main {
    public static void main(String[] args) {
        new Manager(true).close();

        boolean newQuery = true;
        while (newQuery) {
            Integer menuChoice = 0;
            Boolean headless=false;
            RepoGrab rg;
            RGDS rgds = new RGDS();
            String importCSV = "",importDB = "", languages = "", sDate = "", menuOrder = "", endDate = "",
                    errorInt = "Please enter a number.", errorString = "Please enter a valid String", errorYN =
                    "Please enter 'y' or 'n'";
            Integer followers = 0, users = 0, percentLanguage = 0, totalCommit = 0, minTotalSize = 0, maxTotalSize =
                    0, i = 0;

                System.out.println("-- Welcome to RepoGrabber! --\n");

                // TODO: Run select
                // DB: Select all runs in DB
                // if(runs available)
                // -- OUT: List all runs
                // -- OUT: "Which run do you want to use? Or do you want to start from scratch?"
                // -- if(scratch)
                // -- -- Start new repo grab
                // -- else
                // -- -- Import run's repos into local repograb
                // -- -- Go to main menu

                if (args.length > 0 && args[0].equals("headless")) {
                    importCSV = Config.get("IMPORT_CSV");
                    importDB = Config.get("IMPORT_DB");
                    languages = Config.get("LANGUAGE_WANTED");
                    sDate = Config.get("START_CREATEDATE");
                    endDate = Config.get("END_CREATEDATE");
                    followers = Integer.valueOf(Config.get("PROJECT_FOLLOWERS"));
                    users = Integer.valueOf(Config.get("PROJECT_COMMITTERS"));
                    percentLanguage = Integer.valueOf(Config.get("PROJECT_PERCENT"));
                    totalCommit = Integer.valueOf(Config.get("PROJECT_TOTALCOMMIT"));
                    minTotalSize = Integer.valueOf(Config.get("PROJECT_MINTOTALSIZE"));
                    maxTotalSize = Integer.valueOf(Config.get("PROJECT_MAXTOTALSIZE"));
                    menuOrder = Config.get("MENU_ORDER");
                    headless = true;
                } else {
                   Scanner scn = new Scanner(System.in);

                   String dbImportPrompt = "Do you want to import from the DB to skip the GitHub search? (y/n): ";
                   importDB = paramGetterYN(dbImportPrompt,errorYN,scn);

                   if(importDB.equalsIgnoreCase("n")){
                       String importPrompt = "Do you want to import a CSV to skip the GitHub search? (y/n): ";
                       importCSV = paramGetterYN(importPrompt,errorYN,scn);

                       if (importCSV.equalsIgnoreCase("n")) {
                           String followPrompt = "What is the minimum amount of followers on project wanted? (ex: '50'): ";
                           followers = paramGetterInt(followPrompt,errorInt,scn);

                           String langPrompt = "What language do you want to grab? (ex: 'java'): ";
                           languages = paramGetterString(langPrompt,errorString,scn);

                           String committerPrompt = "What is the minimum amount of commit users wanted? (ex: '2'): ";
                           users = paramGetterInt(committerPrompt,errorInt,scn);

                           String percentPrompt = "What is the percentage of the language in the repo wanted? (ex. '51' means at >=51% is language): ";
                           percentLanguage = paramGetterInt(percentPrompt,errorInt,scn);

                           String commitPrompt = "What is minimum amount of commits wanted? (ex. '300'): ";
                           totalCommit = paramGetterInt(commitPrompt, errorInt,scn);

                           String minSizePrompt = "What is minimum size in kilobytes of repo wanted? (ex. '5000'): ";
                           minTotalSize = paramGetterInt(minSizePrompt,errorInt,scn);

                           String maxSizePrompt = "What is the maximum size in kilobytes of repo wanted? (ex. 5000000): ";
                           maxTotalSize = paramGetterInt(maxSizePrompt,errorInt,scn);

                           String startPrompt = "What is start date of repos wanted? (ex. '2010-01-01'): ";
                           sDate = paramGetterString(startPrompt,errorString,scn);

                           String endPrompt = "What is end date of repos wanted? (ex. '2010-01-01'): ";
                           endDate = paramGetterString(endPrompt,errorString,scn);

                       }
                   }
                }

                System.out.println("\n** Grabbing repos!");
                if (importCSV.toLowerCase().equals("n")) {
                    rg = new RepoGrab(followers, languages, users, percentLanguage, totalCommit, minTotalSize,
                            maxTotalSize, sDate, endDate);
                } else {
                    Boolean importFromDB = false;
                    if(importDB.equalsIgnoreCase("y"))
                        importFromDB = true;

                    rg = new RepoGrab(headless,importFromDB);
                }
                System.out.println("\n** Grabbed repos!");

                while (menuChoice != 5 && menuChoice != 8) {
                    if (menuOrder.equals("")) {

                        System.out.println("** Menu: **" +
                                "\n 1. Print all repos" +
                                "\n 2. Clone all repos" +
                                "\n 3. Export to CSV" +
                                "\n 4. Run RefMiner" +
                                "\n 5. New Query" +
                                "\n 6. Move Field Additional Analysis" +
                                "\n 7. Export to RGDS (RepoGrabberDataSet)" +
                                "\n 8. Exit\n");
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
                            metaData(followers,languages,users,percentLanguage,totalCommit,minTotalSize,sDate,rg,endDate);
                            break;
                        case 4:
                            RefMiner.runJobs(rg);
                            break;
                        case 5:
                            break;
                        case 6:
                            new GitChanges(rg);
                            break;
                        case 7:
                            String fileName = "dataset";
                            String title = fileName;
                            String description = "A refactoring dataset from the tool RepoGrabber";
                            if(!headless){
                                System.out.println("Enter a name for the dataset here: ");
                                Scanner fileInput = new Scanner(System.in);
                                fileName = fileInput.nextLine();
                                fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");

                                System.out.println("Enter a title for the dataset here: ");
                                Scanner titleInput = new Scanner(System.in);
                                title = titleInput.nextLine();
                            }

                            if(rgds.write(true,fileName,title,description))
                                System.out.println("Successfully created the RGDS \""+fileName+"\"!");
                            else
                                System.out.println("There was an issue creating the RGDS \""+fileName+"\"! Try again.");
                            break;
                        case 8:
                            newQuery = false;
                            break;
                        case 9:
                            String readFileName = "dataset";
                            if(!headless){
                                System.out.println("Enter a name for the file here: ");
                                Scanner fileInput = new Scanner(System.in);
                                readFileName = fileInput.nextLine();
                                readFileName = readFileName.replaceAll("[\\\\/:*?\"<>|]", "_");
                            }
                            System.out.println(rgds.read(true,readFileName).get(1));
                        default:
                            System.out.println("Invalid choice!");
                            break;
                    }
                }
                rgds.close();
            }
    }
    // Uses given file name to create a txt file for query metadata
    public static void metaData(int followers, String languages, int users, int percentLanguage, int totalCommit, int totalSize,
                                String sDate, RepoGrab rg, String endDate){
        try {
            FileWriter paramWrite = new FileWriter("results/" + CSV.fileName + "_metadata.txt");
            paramWrite.write("+Min Followers: " + followers + "\n+Language: " + languages +
                    "\n+Min Committers: " + users + " \n+Percent of Language: " + percentLanguage +
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

    public static String paramGetterYN(String prompt, String error, Scanner scn){
        String param = "";
        System.out.println(prompt);
        boolean success = false;
        while (!success) {
            param = scn.next();
            if (param.equalsIgnoreCase("y") || param.equalsIgnoreCase("n")){
                success = true;
            }else{
                System.out.println(error);
            }
        }
        return param;
    }
}