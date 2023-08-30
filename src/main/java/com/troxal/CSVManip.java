package com.troxal;

import com.opencsv.CSVWriter;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class CSVManip {
public static String fileName;
    // Create CSV of all repos
    public static void createCSV(List<RepoInfo> repos) {

        System.out.println("Enter a name for the file here: ");
        Scanner fileInput = new Scanner(System.in);
        fileName = fileInput.nextLine();
        fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        File file = new File(fileName + ".csv");

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(file));

            String[] headerTxt = {"Github ID", "Repository Name", "Github Link", "Description", "Primary Language", "Creation Date", "Update Date", "Push Date", "Is Archived", "Is Fork", "Mentionable Users", "Issue Users", "Total Size", "Total Commits", "Forks", "Stars", "Watchers", "Languages"};
            writer.writeNext(headerTxt);

            for (int i = 0; i < repos.size(); i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < repos.get(i).getLanguages().size(); j++)
                    sb.append(repos.get(i).getLanguages().get(j).toString());

                String tempLine[] = new String[]{
                        repos.get(i).getId(),
                        repos.get(i).getName(),
                        repos.get(i).getUrl(),
                        repos.get(i).getDescription(),
                        repos.get(i).getPrimaryLanguage(),
                        repos.get(i).getCreationDate(),
                        repos.get(i).getUpdateDate(),
                        repos.get(i).getPushDate(),
                        String.valueOf(repos.get(i).getIsArchived()),
                        String.valueOf(repos.get(i).getIsFork()),
                        String.valueOf(repos.get(i).getTotalMentionableUsers()),
                        String.valueOf(repos.get(i).getTotalIssueUsers()),
                        String.valueOf(repos.get(i).getTotalProjectSize()),
                        String.valueOf(repos.get(i).getTotalCommits()),
                        String.valueOf(repos.get(i).getForkCount()),
                        String.valueOf(repos.get(i).getStarCount()),
                        String.valueOf(repos.get(i).getWatchCount()),
                        sb.toString()
                };
                writer.writeNext(tempLine);
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFileName(){
        return fileName;
    }

}
