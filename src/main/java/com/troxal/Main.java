package com.troxal;

import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Boolean continueA=true;

        System.out.println("-- Welcome to RepoGrabber! --\n");
        Scanner scn = new Scanner(System.in);
        System.out.println("At least how many followers on project: ");
        Integer followers = Integer.valueOf(scn.next());
        System.out.println("What language do you want to grab? (ex: java)");
        String languages = scn.next();

        System.out.println("\n** Grabbing repos!");
        RepoGrab rg = new RepoGrab(followers,languages);
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