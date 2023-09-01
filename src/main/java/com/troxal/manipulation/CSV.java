package com.troxal.manipulation;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.troxal.pojo.LanguageInfo;
import com.troxal.pojo.RepoInfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CSV {
    // Create CSV of all repos

    public static String fileName;
    private static String oldFile;

    public static void create(List<RepoInfo> repos){
        //Allows user to input filename for CSV and fixes any illegal characters
        System.out.println("Enter a name for the file here: ");
        Scanner fileInput = new Scanner(System.in);
        fileName = fileInput.nextLine();
        fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        File file = new File("results/"+ fileName + ".csv");

        try
        {
            // Use CSVWriter to create a new CSV
            CSVWriter writer = new CSVWriter(new FileWriter(file));

            // Write header stuff
            String[] headerTxt = {"Github ID","Repository Name","Github Link","Description","Primary Language","Creation Date","Update Date","Push Date","Is Archived","Is Fork","Mentionable Users","Issue Users","Total Size","Total Commits","Forks","Stars","Watchers","Languages"};
            writer.writeNext(headerTxt);

            // For all repos, import into a row
            for(int i=0;i<repos.size();i++){
                // Language stuff to make each language a single row (ex: 'Java:378030 Shell:2612 C++:2566')
                StringBuilder sb = new StringBuilder();
                for(int j=0;j<repos.get(i).getLanguages().size();j++)
                    sb.append(repos.get(i).getLanguages().get(j).toString());

                // Build row
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

                // Write row to CSV
                writer.writeNext(tempLine);
            }

            // Close writer
            writer.close();
        }

        // Print error
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Read CSV, returns list of repos back
    public static List<RepoInfo> read() {
        List<RepoInfo> repos = new ArrayList<>();
        try {

            // Open 'Repos.csv'
            System.out.println("Enter in the name of the file:");
            Scanner fileGrabber = new Scanner(System.in);
            oldFile = fileGrabber.nextLine();
            FileReader filereader = new FileReader("results/"+oldFile+".csv");

            // Skip header
            CSVReader reader = new CSVReaderBuilder(filereader)
                    .withSkipLines(1)
                    .build();

            // Use csvReader to read in CSV
            List<String[]> csvData = reader.readAll();

            System.out.println("Imported "+csvData.size()+" repos!");

            // Read all rows of the CSV
            for (int i=0;i<csvData.size();i++) {
                // Languages are stored seperated by a space in CSV, so split
                String[] languages = csvData.get(i)[17].split("' ");
                // tempLanguage to read into RepoInfo
                List<LanguageInfo> tempLanguages = new ArrayList<>();
                // Then the name and size are seperated by a colon
                for (int j = 0; j < languages.length; j++) {
                    String[] tempLanguage = languages[j].split(":");
                    // Add to tempLanguage the language
                    tempLanguages.add(new LanguageInfo(tempLanguage[0],Integer.valueOf(tempLanguage[1])));
                }

                // Temp RepoInfo, all rows (except tempLanguages) correspond to their value
                RepoInfo tempRepo = new RepoInfo(
                        csvData.get(i)[0],
                        csvData.get(i)[1],
                        csvData.get(i)[2],
                        csvData.get(i)[3],
                        csvData.get(i)[4],
                        csvData.get(i)[5],
                        csvData.get(i)[6],
                        csvData.get(i)[7],
                        Boolean.parseBoolean(csvData.get(i)[8]),
                        Boolean.parseBoolean(csvData.get(i)[9]),
                        Integer.valueOf(csvData.get(i)[10]),
                        Integer.valueOf(csvData.get(i)[11]),
                        Integer.valueOf(csvData.get(i)[12]),
                        Integer.valueOf(csvData.get(i)[13]),
                        Integer.valueOf(csvData.get(i)[14]),
                        Integer.valueOf(csvData.get(i)[15]),
                        Integer.valueOf(csvData.get(i)[16]),
                        tempLanguages
                );

                // Add the repos to the repo list
                repos.add(tempRepo);
            }

            // Close reader
            reader.close();

            // Return the new repo list
            return repos;

            // Or error & return repos empty
        } catch (Exception e) {
            e.printStackTrace();
            return repos;
        }
    }
}
