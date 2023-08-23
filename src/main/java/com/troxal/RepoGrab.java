package com.troxal;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.io.File;
import java.util.List;

import com.opencsv.CSVWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

/**
 * Class: RepoGrab
 * Description: Gets all repos, and allows for manipulations such as cloning and CSV creation
 */
public class RepoGrab{
    // Initialize variables
    static private String authToken = getConfig.getKey();
    private String variables,language;
    private Integer amountReturned,followers,users,percentLanguage,totalCommit,totalSize;
    private LocalDate beginningDate = LocalDate.parse("2010-01-01"), endingDate = LocalDate.parse("2010-04-01"),currentDate=LocalDate.now();

    private Data JSONResponse = null;
    private List<RepoData> repos = new ArrayList<>();

    // Constructor to get query info, basically just sets all the variable data to their respective global variables
    public RepoGrab(Integer followers,String language,Integer users,Integer percentLanguage,Integer totalCommit,Integer totalSize,String sDate) {
        this.followers=followers;
        this.language=language;
        this.users=users;
        this.percentLanguage=percentLanguage;
        this.totalCommit=totalCommit;
        this.totalSize=totalSize;
        this.beginningDate = LocalDate.parse(sDate);
        this.endingDate = beginningDate.plusMonths(4);
        this.amountReturned=50;

        // Start getting the data (endCursor is null here because the GraphQL API supports that to start at beginning)
        jsonToRepoData(null);
    }

    // Queries the data based on set variables
    private void queryData(String endCursor){
        // Create new API link
        GitHubGraphQL api = new GitHubGraphQL();

        // StringBuilder just allows for easier if/else appendations of the variable string
        StringBuilder sb = new StringBuilder();
        sb.append("{\"queryString\": \"");
        // If there's min followers, set
        if(followers!=null)
            sb.append("followers:>="+followers);
        // If there's a language, set
        if(language!=null) {
            sb.append(" language:"+language);
        }
        // If there's a min total size, set
        if(totalSize!=null) {
            sb.append(" size:>="+totalSize);
        }
        // Set the beginning to ending push dates
        sb.append(" pushed:");
        sb.append(beginningDate+".."+endingDate);
        // Always 50 returned, it's supposed to allow 100, but I couldn't get that to work
        if(amountReturned!=null)
            sb.append("\",\"amountReturned\":"+amountReturned);
        // if endCursor then put, else null
        if(endCursor!=null)
            sb.append(",\"cursorValue\":\""+endCursor+"\"}");
        else
            sb.append(",\"cursorValue\":"+null+"}");
        variables = sb.toString();

        // This is the GraphQL query, could make it easier to update in future
        String query = "query listRepos($queryString: String!,$amountReturned: Int!,$cursorValue: String) { rateLimit { cost remaining resetAt } search(query: $queryString, type: REPOSITORY, first: $amountReturned after:$cursorValue) { repositoryCount pageInfo { endCursor hasNextPage } edges { node { ... on Repository { id name createdAt isArchived isPrivate url assignableUsers { totalCount } languages(first: 3, orderBy: {field: SIZE, direction: DESC}) { edges { size node { name } } totalSize } defaultBranchRef { target { ... on Commit { history { totalCount } } } } } } } } }";
        try{
            // For sake of testing, output the current variables
            System.out.println("Variables: "+variables);
            // Get the response in put in JSONResponse for processing
            JSONResponse = api.getData(authToken,query,variables);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // Gets the data recursively until all data is gotten
    private void jsonToRepoData(String endCursor){
        // Get the data
        queryData(endCursor);

        // For each repo
        for(int i=0;i<JSONResponse.getSearch().getEdges().size();i++){
            // If there's anything in the default branch
            if(JSONResponse.getSearch().getEdges().get(i).getNode().getDefaultBranchRef()!=null){
                // Init don't ignore repo
                Boolean ignoreRepo=false;
                // Set a tempRepo var for less code duplication
                Node tempRepo = JSONResponse.getSearch().getEdges().get(i).getNode();
                // List of all languages
                List<Language> languages = new ArrayList<>();
                // For each language
                for(int j=0;j<tempRepo.getLanguages().getEdges().size();j++){
                    // Set a tempLanguage for less code duplication
                    Languages tempLanguage = tempRepo.getLanguages();
                    // If the name of the language is the chosen language
                    if(tempLanguage.getEdges().get(j).getNode().getName()==language)
                        // And it's percent size over the total size is greater than/equal to the threshold percent
                        if((tempLanguage.getEdges().get(j).getSize()/tempLanguage.getTotalSize())>=percentLanguage){
                            // Add the language to languages
                            languages.add(new Language(tempRepo.getLanguages().getEdges().get(j).getNode().getName(),tempRepo.getLanguages().getEdges().get(j).getSize()));
                        }else{
                            // Else ignore repo
                            ignoreRepo=true;
                        }
                }
                // If the issue assignable users is less than the specified amount, ignore repo
                if(tempRepo.getAssignableUsers().getTotalCount()<users)
                    ignoreRepo=true;

                // If the commit count is less than the specified amount, ignore repo
                if(tempRepo.getDefaultBranchRef().getTarget().getHistory().getTotalCount()<totalCommit)
                    ignoreRepo=true;

                // If not ignored repo, add to the Repos array
                if(!ignoreRepo){
                    repos.add(new RepoData(tempRepo.getId(), tempRepo.getName(), tempRepo.getUrl(), tempRepo.getCreatedAt(), tempRepo.getAssignableUsers().getTotalCount(),tempRepo.getLanguages().getTotalSize(),tempRepo.getDefaultBranchRef().getTarget().getHistory().getTotalCount(),languages));
                    System.out.println("** Added "+tempRepo.getName());
                }else{
                    //System.out.println("*** Repo doesn't meet thresholds.");
                    //TODO: counter for ignored projects
                }
            }
        }
        // While there is a cursor (GraphQL has a next page) AND you still have at least 100 API calls left
        while (JSONResponse.getSearch().getPageInfo().gethasNextPage()&&JSONResponse.getRateLimit().getRemaining()>100){
            // Print status on cursor/remaining API
            System.out.println("Next cursor:"+JSONResponse.getSearch().getPageInfo().getEndCursor()+" :: Remaining API:"+JSONResponse.getRateLimit().getRemaining());
            // Recurse with next cursor
            jsonToRepoData(JSONResponse.getSearch().getPageInfo().getEndCursor());
        }
        // After there's no more pages, go to next date chunk until the ending date is after the current date
        while (JSONResponse.getRateLimit().getRemaining()>100&&endingDate.isBefore(currentDate)) {
            // Update beginningDate to be last endingDate
            beginningDate = endingDate;
            // Add four months to the endingDate
            endingDate = endingDate.plusMonths(4);
            // Print status on date range/remaining API
            System.out.println("Date range:"+beginningDate+" to "+endingDate+" :: Remaining API:"+JSONResponse.getRateLimit().getRemaining());
            // Recurse with no cursor
            jsonToRepoData(null);
        }
    }

    // Just get all data from array
    private List<RepoData> getData(){
        return repos;
    }

    // Get remaining rate calls left
    public Integer getAPICallsLeft(){
        return JSONResponse.getRateLimit().getRemaining();
    }

    // Get total amount of repos
    public Integer getTotalRepoCount(){
        return JSONResponse.getSearch().getRepositoryCount();
    }

    // Get all repos
    public List<RepoData> getRepos(){
        return getData();
    }

    // Get a single repo's data
    public RepoData getRepo(Integer id){
        return getRepos().get(id);
    }

    // Clone a repo based on it's url and name
    private boolean cloneRepo(String url,String name){
        // We need to try/catch git issues
        try{
            // Get clean name for Windows
            String cleanPath = FileNameCleaner.cleanFileName(name);
            // Clone repo with JGit
            Git.cloneRepository()
                    .setNoCheckout(true)
                    .setURI(url+".git")
                    .setDirectory(new File("repos/"+cleanPath))
                    .call();
            return true;
        } catch (InvalidRemoteException e) {
            throw new RuntimeException(e);
        } catch (TransportException e) {
            throw new RuntimeException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    // Clone all repos
    public void cloneRepos(){
        for(int i=0;i<getRepos().size();i++) {
            System.out.println("** Cloning "+getRepo(i).getName());
            if(cloneRepo(getRepo(i).getUrl(),getRepo(i).getName()+"_"+getRepo(i).getId()))
                System.out.println("** Clone successful");
        }
    }

    // Create CSV of all repos
    public void createCSV(){
        File file = new File("Repos.csv");
        try
        {
            CSVWriter writer = new CSVWriter(new FileWriter(file));

            String[] headerTxt = {"Github ID","Repository Name","Github Link","Creation Date","Community","Total Size","Total Commits"};
            writer.writeNext(headerTxt);

            for(int i=0;i<repos.size();i++){
                String tempLine[] = new String[]{
                        getRepo(i).getId(),
                        getRepo(i).getName(),
                        getRepo(i).getUrl(),
                        getRepo(i).getCreationDate(),
                        String.valueOf(getRepo(i).getUsers()),
                        String.valueOf(getRepo(i).getTotalSize()),
                        String.valueOf(getRepo(i).getTotalCommits())
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
