package com.troxal.manipulation;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.troxal.database.Database;
import com.troxal.database.Manager;
import com.troxal.pojo.LanguageInfo;
import com.troxal.pojo.RepoInfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CSV {
    // Create CSV of all repos

    public static String fileName;
    private static String oldFile;

    public static void create(List<RepoInfo> repos,Boolean headless){
        if(!headless){
            //Allows user to input filename for CSV and fixes any illegal characters
            System.out.println("Enter a name for the file here: ");
            Scanner fileInput = new Scanner(System.in);
            fileName = fileInput.nextLine();
            fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        }else{
            fileName="Repos";
        }
        new File("./results").mkdirs();
        File file = new File("results/"+ fileName + ".csv");
        try
        {
            // Use CSVWriter to create a new CSV
            CSVWriter writer = new CSVWriter(new FileWriter(file));

            // Write header stuff
            String[] headerTxt = {"Github ID","Repository Name","Owner","Github Link","Description","Primary Language",
                    "Creation Date","Update Date","Push Date","Is Archived","Archived At","Is Fork","Is Empty",
                    "Is Locked","Is Disabled","Is Template","Mentionable Users","Issue Users","Committers","Total Size",
                    "Total Commits","Total Issues","Forks","Stars","Watchers","Languages","Branch Name"};
            writer.writeNext(headerTxt);

            // For all repos, import into a row
            for(int i=0;i<repos.size();i++){
                // Language stuff to make each language a single row (ex: 'Java:378030 Shell:2612 C++:2566')
                StringBuilder sb = new StringBuilder();
                for(int j=0;j<repos.get(i).getLanguages().size();j++)
                    sb.append(repos.get(i).getLanguages().get(j).toString());

                RepoInfo repo = repos.get(i);
                // Build row
                String tempLine[] = new String[]{
                        repo.getId(),
                        repo.getName(),
                        repo.getOwner(),
                        repo.getUrl(),
                        repo.getDescription(),
                        repo.getPrimaryLanguage(),
                        repo.getCreationDate(),
                        repo.getUpdateDate(),
                        repo.getPushDate(),
                        String.valueOf(repo.getIsArchived()),
                        repo.getArchivedAt(),
                        String.valueOf(repo.getIsFork()),
                        String.valueOf(repo.getIsEmpty()),
                        String.valueOf(repo.getIsLocked()),
                        String.valueOf(repo.getIsDisabled()),
                        String.valueOf(repo.getIsTemplate()),
                        String.valueOf(repo.getTotalIssueUsers()),
                        String.valueOf(repo.getTotalMentionableUsers()),
                        String.valueOf(repo.getTotalCommitterCount()),
                        String.valueOf(repo.getTotalProjectSize()),
                        String.valueOf(repo.getTotalCommits()),
                        String.valueOf(repo.getIssueCount()),
                        String.valueOf(repo.getForkCount()),
                        String.valueOf(repo.getStarCount()),
                        String.valueOf(repo.getWatchCount()),
                        sb.toString(),
                        repo.getBranchName()
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
    public static List<RepoInfo> read(Boolean headless) {
        List<RepoInfo> repos = new ArrayList<>();
        try {

            // Open 'Repos.csv'
            if(!headless){
            System.out.println("Enter in the name of the file:");
                Scanner fileGrabber = new Scanner(System.in);
                oldFile = fileGrabber.nextLine();
            }else{
                oldFile="Repos";
            }
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
                Boolean ignoreRepo = false;
                // Languages are stored seperated by a space in CSV, so split
                String[] languages = csvData.get(i)[25].split("' ");
                // tempLanguage to read into RepoInfo
                List<LanguageInfo> tempLanguages = new ArrayList<>();
                // Then the name and size are seperated by a colon
                for (int j = 0; j < languages.length; j++) {
                    String[] tempLanguage = languages[j].split(":");
                    try {
                        // Add to tempLanguage the language
                        tempLanguages.add(new LanguageInfo(tempLanguage[0], Integer.valueOf(tempLanguage[1])));
                    }catch(ArrayIndexOutOfBoundsException e){
                        ignoreRepo = true;
                    }
                }

                if (!ignoreRepo){
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
                            csvData.get(i)[8],
                            Boolean.parseBoolean(csvData.get(i)[9]),
                            csvData.get(i)[10],
                            Boolean.parseBoolean(csvData.get(i)[11]),
                            Boolean.parseBoolean(csvData.get(i)[12]),
                            Boolean.parseBoolean(csvData.get(i)[13]),
                            Boolean.parseBoolean(csvData.get(i)[14]),
                            Boolean.parseBoolean(csvData.get(i)[15]),
                            Integer.valueOf(csvData.get(i)[16]),
                            Integer.valueOf(csvData.get(i)[17]),
                            Integer.valueOf(csvData.get(i)[18]),
                            Integer.valueOf(csvData.get(i)[19]),
                            Integer.valueOf(csvData.get(i)[20]),
                            Integer.valueOf(csvData.get(i)[21]),
                            Integer.valueOf(csvData.get(i)[22]),
                            Integer.valueOf(csvData.get(i)[23]),
                            Integer.valueOf(csvData.get(i)[24]),
                            tempLanguages,
                            csvData.get(i)[26]
                    );

                    // Add the repos to the repo list
                    repos.add(tempRepo);
                }
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
    public static void createFromDB(Boolean headless){
        if(!headless){
            //Allows user to input filename for CSV and fixes any illegal characters
            System.out.println("Enter a name for the file here: ");
            Scanner fileInput = new Scanner(System.in);
            fileName = fileInput.nextLine();
            fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        }else{
            fileName="Repos";
        }
        new File("./results").mkdirs();
        File file = new File("results/"+ fileName + ".csv");
        try
        {
            // Use CSVWriter to create a new CSV
            CSVWriter writer = new CSVWriter(new FileWriter(file));

            // Write header stuff
            String[] headerTxt = {"Github ID","Repository Name","Owner","Github Link","Description","Primary Language",
                    "Creation Date","Update Date","Push Date","Is Archived","Archived At","Is Fork","Is Empty",
                    "Is Locked","Is Disabled","Is Template","Mentionable Users","Issue Users","Committers","Total Size",
                    "Total Commits","Total Issues","Forks","Stars","Watchers","Languages","Branch Name","ReadMe",
                    "Domain"};
            writer.writeNext(headerTxt);

            try {
                Database db = new Manager().access();
                ResultSet repos = db.select("Repositories",new String[]{"*"});
                while(repos.next()){
                    String repoId = repos.getString("id");
                    // Language stuff to make each language a single row (ex: 'Java:378030 Shell:2612 C++:2566')
                    StringBuilder sb = new StringBuilder();
                    ResultSet languages = db.select("Languages",new String[]{"*"},"repoid = ?",new Object[]{repoId});
                    while(languages.next()){
                        sb.append(languages.getString("name")+":"+languages.getString("size")+"' ");
                    }
                    languages.close();

                    String readMe = "";
                    if(repos.getString("readme")!=null)
                        readMe = repos.getString("readme")
                                .replaceAll("[^a-zA-Z0-9]", " ");

                    // Build row
                    String tempLine[] = new String[]{
                            repoId,
                            repos.getString("name"),
                            repos.getString("owner"),
                            repos.getString("url"),
                            repos.getString("description"),
                            repos.getString("primarylanguage"),
                            repos.getString("creationdate"),
                            repos.getString("updatedate"),
                            repos.getString("pushdate"),
                            String.valueOf(repos.getBoolean("isarchived")),
                            repos.getString("archivedat"),
                            String.valueOf(repos.getBoolean("isforked")),
                            String.valueOf(repos.getBoolean("isempty")),
                            String.valueOf(repos.getBoolean("islocked")),
                            String.valueOf(repos.getBoolean("isdisabled")),
                            String.valueOf(repos.getBoolean("istemplate")),
                            String.valueOf(repos.getInt("totalissueusers")),
                            String.valueOf(repos.getInt("totalmentionableusers")),
                            String.valueOf(repos.getInt("totalcommittercount")),
                            String.valueOf(repos.getInt("totalprojectsize")),
                            String.valueOf(repos.getInt("totalcommits")),
                            String.valueOf(repos.getInt("issuecount")),
                            String.valueOf(repos.getInt("forkcount")),
                            String.valueOf(repos.getInt("starcount")),
                            String.valueOf(repos.getInt("watchcount")),
                            sb.toString(),
                            repos.getString("branchname"),
                            readMe,
                            repos.getString("domain")
                    };

                    // Write row to CSV
                    writer.writeNext(tempLine);
                }
                repos.close();
                db.close();
                writer.close();

            } catch (SQLException e) {
                System.out.println("[ERROR] SQL Exception: "+e+" (detect [RefMine.java])");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
