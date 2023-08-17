package com.troxal;

import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Boolean continueA=true;

        System.out.println("-- Welcome to RepoGrabber! --\n");
        Scanner scn = new Scanner(System.in);
        System.out.println("What is the minimum amount of followers on project wanted? (ex: '50'): ");
        Integer followers = Integer.valueOf(scn.next());
        System.out.println("What language do you want to grab? (ex: 'java'): ");
        String languages = scn.next();
        System.out.println("What is the minimum amount of assignable users wanted? (ex: '5'): ");
        Integer users = Integer.valueOf(scn.next());
        System.out.println("What is the percentage of the language in the repo wanted? (ex. '50' means at >=50% is language): ");
        Integer percentLanguage = Integer.valueOf(scn.next());
        System.out.println("What is minimum amount of commits wanted? (ex. '300'): ");
        Integer totalCommit = Integer.valueOf(scn.next());
        System.out.println("What is minimum size in bytes of repo wanted? (ex. '5000'): ");
        Integer totalSize = Integer.valueOf(scn.next());
        System.out.println("What is start date of repos wanted? (ex. '2010-01-01'): ");
        String sDate = scn.next();

        System.out.println("\n** Grabbing repos!");
        RepoGrab rg = new RepoGrab(followers,languages,users,percentLanguage,totalCommit,totalSize,sDate);
        System.out.println("\n** Grabbed repos!");

        while(continueA==true){
            Integer menuChoice = 0;
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
                    rg.cloneRepos();
                    break;
                case 3:
                    rg.createCSV();
                    break;
                case 4:
                    continueA=false;
                    break;
            }
        }

    }
}