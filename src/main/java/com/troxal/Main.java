package com.troxal;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Integer menuChoice = 0;
        Boolean success = false;
        RepoGrab rg = null;

        System.out.println("-- Welcome to RepoGrabber! --\n");
        Scanner scn = new Scanner(System.in);

        System.out.println("Do you want to import a CSV to skip the GitHub search? (y/n): ");
        String importCSV="";
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
            Integer followers=0;
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
            rg = new RepoGrab(followers,languages,users,percentLanguage,totalCommit,totalSize,sDate);
            System.out.println("\n** Grabbed repos!");
        }else{
            System.out.println("\n** Grabbing repos!");
            rg = new RepoGrab();
            System.out.println("\n** Grabbed repos!");
        }


        while(menuChoice!=5){
            System.out.println("** Menu: **" +
                    "\n 1. Print all repos" +
                    "\n 2. Clone all repos"+
                    "\n 3. Export to CSV"+
                    "\n 4. Run RefMiner"+
                    "\n 5. Exit\n");
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
                    break;
                case 4:
                    RefMine.calculate(rg.getRepos());
                case 5:
                    break;
            }
        }

    }
}