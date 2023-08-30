package com.troxal;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Integer menuChoice = 0;
        Boolean success = false;

        System.out.println("-- Welcome to RepoGrabber! --\n");
        Scanner scn = new Scanner(System.in);
        System.out.println("What is the minimum amount of followers on project wanted? (ex: '50'): ");
        Integer followers=0;
        while(!success){
            try {
                followers = Integer.valueOf(scn.next());
                success = true;
            }catch(NumberFormatException e){
                System.out.println("Invalid, please enter a number (ex: '50'): ");
            }
        }
        System.out.println("What language do you want to grab? (ex: 'java'): ");
        String languages="";
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
        Integer users = 0;
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
        Integer percentLanguage=0;
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
        Integer totalCommit=0;
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
        Integer totalSize=0;
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
        String sDate="";
        success=false;
        while(!success){
            try {
                sDate = scn.next();
                success = true;
            }catch(NumberFormatException e){
                System.out.println("Invalid, please enter a string (ex: '2010-01-01'): ");
            }
        }

        System.out.println("\n** Grabbing repos!");
        RepoGrab rg = new RepoGrab(followers,languages,users,percentLanguage,totalCommit,totalSize,sDate);
        System.out.println("\n** Grabbed repos!");

        while(menuChoice!=4){
            System.out.println("** Menu: **" +
                    "\n 1. Print all repos" +
                    "\n 2. Clone all repos"+
                    "\n 3. Export to CSV"+
                    "\n 4. Exit\n");
            System.out.println("Choice: ");
            menuChoice = Integer.valueOf(scn.next());
            switch(menuChoice){
                case 1:
                    System.out.println(rg.getRepos());
                    break;
                case 2:
                    Clone.cloneRepos(rg.getRepos());
                    break;
                case 3:
                    CSVManip.createCSV(rg.getRepos());
                    try{
                        FileWriter paramWrite = new FileWriter(CSVManip.fileName+ "_parameters.txt");
                        paramWrite.write("Min Followers: " + followers + "\nLanguage: " + languages +
                                "\nMin Mentionable Users: " + users + " \nPercent of Language: " + percentLanguage +
                                "\nMin Commits: " + totalCommit + "\nMin Size in Bytes: " + totalSize + "\nStart Date: " + sDate);
                        paramWrite.close();
                    } catch(IOException e) {
                        System.out.println("Parameter error");
                    }
                    break;
                case 4:
                    break;
            }
        }

    }
}