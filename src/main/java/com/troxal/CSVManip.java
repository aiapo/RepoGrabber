package com.troxal;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVManip {
    // Create CSV of all repos
    public static void createCSV(List<RepoInfo> repos){
        File file = new File("Repos.csv");
        try
        {
            CSVWriter writer = new CSVWriter(new FileWriter(file));

            String[] headerTxt = {"Github ID","Repository Name","Github Link","Creation Date","Mentionable Users","Total Size","Total Commits"};
            writer.writeNext(headerTxt);

            for(int i=0;i<repos.size();i++){
                String tempLine[] = new String[]{
                        repos.get(i).getId(),
                        repos.get(i).getName(),
                        repos.get(i).getUrl(),
                        repos.get(i).getCreationDate(),
                        String.valueOf(repos.get(i).getTotalMentionableUsers()),
                        String.valueOf(repos.get(i).getTotalProjectSize()),
                        String.valueOf(repos.get(i).getTotalCommits())
                };
                writer.writeNext(tempLine);
            }

            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
