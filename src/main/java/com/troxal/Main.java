package com.troxal;

import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Boolean iPub=false,iArch=false,continueA=true;

        System.out.println("-- Welcome to RepoGrabber! --\n");
        Scanner scn = new Scanner(System.in);
        System.out.println("Do you want only public repos? (y/n): ");
        String isPublic = scn.next();
        if(isPublic.toLowerCase()=="y")
            iPub = true;
        System.out.println("Do you want archived repos? (y/n): ");
        String isArchive = scn.next();
        if(isArchive.toLowerCase()=="y")
            iArch = true;
        System.out.println("What from what date do you want there to be at least one commit (ex: >2010-01-01): ");
        String pushDate = scn.next();
        System.out.println("What languages do you want to grab? (comma seperated; ex: java,python)");
        String languages = scn.next();
        System.out.println("How many repos/page do you want to return (ex: 1-100): ");
        Integer amount = Integer.valueOf(scn.next());

        System.out.println("\n** Grabbing repos!");
        RepoGrab rg = new RepoGrab(iArch,iPub,pushDate,null,languages,amount);
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