package com.troxal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RepoGrab {
    // Initialize variables
    static private String authToken = Config.getAuthToken();
    private String language;
    private Integer followers,users,percentLanguage,totalCommit,totalSize,ignoredRepos=0, addedRepos = 0;
    // 20 day chunks, 30 repos/page
    private Integer addedTime=20;
    private Integer amountReturned=45;
    private Boolean ranAtLeastOnce=false;
    private LocalDate beginningDate = LocalDate.parse("2010-01-01"), endingDate = LocalDate.parse("2010-01-15"),currentDate=LocalDate.now();
    private Set<RepoInfo> repoCollection = new HashSet<>();

    // Constructor to get query info, basically just sets all the variable data to their respective global variables
    public RepoGrab(Integer followers,String language,Integer users,Integer percentLanguage,Integer totalCommit,Integer totalSize,String sDate) {
        this.followers=followers;
        this.language=language;
        this.users=users;
        this.percentLanguage=percentLanguage;
        this.totalCommit=totalCommit;
        this.totalSize=totalSize;
        this.beginningDate = LocalDate.parse(sDate);
        this.endingDate = currentDate;
        this.addedTime= Math.toIntExact(ChronoUnit.DAYS.between(beginningDate, endingDate));

        getRepos(null);
    }

    private String generateVariables(String endCursor){
        // StringBuilder just allows for easier if/else of the variable string
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
        // Set the beginning to ending created dates
        sb.append(" created:");
        sb.append(beginningDate+".."+endingDate);
        // This is set manually because otherwise the response may be unstable past like 50 (even then it's unstable)
        sb.append("\",\"amountReturned\":"+amountReturned);
        // if endCursor then put, else null
        if(endCursor!=null)
            sb.append(",\"cursorValue\":\""+endCursor+"\"}");
        else
            sb.append(",\"cursorValue\":"+null+"}");
        return sb.toString();
    }

    // Optimizes the chunk period so that it will return a chunk that has between 700-1000 repos
    private void dayOptimizer(Integer days){
        try {
            // Wait 2 seconds to comply with API limits
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            System.out.println("[ERROR] Error trying to wait: \n"+e);
        }
        // Just get total repos in this chunk
        String query = "query listRepos($queryString: String!, $amountReturned: Int!, $cursorValue: String) { " +
                "rateLimit { cost remaining resetAt } " +
                    "search(query: $queryString, type: REPOSITORY, first: $amountReturned after: $cursorValue) { " +
                        "repositoryCount " +
                    "} " +
                "}";

        // Print out testing period
        System.out.println("[INFO] Optimizer trying period from "+beginningDate+" to "+endingDate);

        // Get the data
        Data repoData = queryData(query,null);

        // If no error from API
        if(repoData!=null){
            // If ending date is or is after current date, accept any <1000
            if((endingDate.isEqual(currentDate)||endingDate.isAfter(currentDate))&&repoData.getSearch().getRepositoryCount()<=1000){
                addedTime = days;
                System.out.println("[INFO] Landed on "+addedTime+" days for the optimal period.");
            }else if(repoData.getSearch().getRepositoryCount()>=700&&repoData.getSearch().getRepositoryCount()<=1000) {
                addedTime = days;
                System.out.println("[INFO] Landed on "+addedTime+" days for the optimal period.");
            }else if(repoData.getSearch().getRepositoryCount()>1000){
                addedTime=days/2;
                endingDate=beginningDate.plusDays(addedTime);
                dayOptimizer(addedTime);
            }else{
                addedTime=days+50;
                endingDate=beginningDate.plusDays(addedTime);
                dayOptimizer(addedTime);
            }
        }else
            System.out.println("error with repoData");
    }

    // Converts the JSON response to POJO
    private Data jsonToRepo(String responseData){
        // Create a ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Return the JSON data from the POST query and map each JSON Object to their respective Java Object
            GitHubJSON d = objectMapper.readValue(responseData, GitHubJSON.class);
            if(d.getErrors()==null)
                return d.getData();
            else{
                System.out.println("[ERROR] Encountered an error on from GitHub: \n - "+d.getErrors().get(0).getMessage());
                return null;
            }
        } catch (JsonMappingException e) {
            System.out.println("[ERROR] Encountered an error on when JSON mapping: \n"+e);
            return null;
        } catch (JsonProcessingException e) {
            System.out.println("[ERROR] Encountered an error on when JSON processing: \n"+e);
            return null;
        }
    }

    // Gets the data from the API and error handles API stuff
    private Data queryData(String query,String endCursor){
        String queryVariables = generateVariables(endCursor);

        // Actually make the query
        Requests data = GitHub.GraphQL(authToken, query, queryVariables);

        // Use HTTP Status responses to determine if we have an error or not
        // 200 = OK
        if(data.getStatus()==200){
            // Convert JSON response to Object
            Data repos = jsonToRepo(data.getBody());
            // If no error mapping, return the mapped repos
            if(repos!=null)
                return repos;
            else
                return null;
        // 403 = Unauthorized / 502 = Bad Gateway
        }else if(data.getStatus()==403||data.getStatus()==502){
          System.out.println("[ERROR] A rate limit or unauthorized request encountered... waiting "+15+" seconds...\n - Headers:\n "+data.getHeader().toString()+" \n - Body:\n"+data.getBody());
          try {
              // Wait 15 seconds to comply with API limits
              TimeUnit.SECONDS.sleep(15);
          } catch (InterruptedException e) {
              System.out.println("[ERROR] Error trying to wait: \n"+e);
          }
           return null;
        // Just error if any other status
        }else{
            System.out.println("[ERROR] Encountered an error from GitHub: \n - Status:"+data.getStatus()+"\n - Body: "+data.getBody());
            return null;
        }
    }

    // Get all repos from API
    public void getRepos(String endCursor){
        String query = "query listRepos($queryString: String!, $amountReturned: Int!, $cursorValue: String) { " +
                "rateLimit { cost remaining resetAt } " +
                "search(query: $queryString, type: REPOSITORY, first: $amountReturned after: $cursorValue) { " +
                    "repositoryCount " +
                    "pageInfo { endCursor hasNextPage } " +
                    "repositories: edges { " +
                        "repo: node { " +
                            "... on Repository { " +
                                "id " +
                                "name " +
                                "url " +
                                "description " +
                                "createdAt " +
                                "updatedAt " +
                                "pushedAt " +
                                "isArchived " +
                                "isPrivate " +
                                "isFork " +
                                "isEmpty " +
                                "primaryLanguage { name } " +
                                "forkCount " +
                                "stargazerCount " +
                                "watchers { totalCount } " +
                                "issueUsers: assignableUsers { totalCount } " +
                                "mentionableUsers { totalCount } " +
                                "languages(first: 3, orderBy: {field: SIZE, direction: DESC}) { " +
                                    "edges { size node { name } } " +
                                "totalSize " +
                            "} " +
                            "mainBranch: defaultBranchRef { " +
                                "target { " +
                                    "... on Commit { " +
                                        "history { totalCount } " +
                                    "} " +
                                "} " +
                            "} " +
                        "} " +
                    "} " +
                "} " + "} " + "}";

        // For sake of testing, output the current variables
        System.out.println("[DEBUG] Variables: "+generateVariables(endCursor));

        // Get the data
        Data repoData = queryData(query,endCursor);

        if(repoData!=null){
            if(((repoData.getSearch().getRepositoryCount()<700||repoData.getSearch().getRepositoryCount()>1000)&&endingDate.isBefore(currentDate))||!ranAtLeastOnce) {
                System.out.println("[INFO] Unoptimized creation period, running optimization...");
                ranAtLeastOnce=true;
                dayOptimizer(addedTime);
                getRepos(null);
            } else {
                // For each repo
                for (int i = 0; i < repoData.getSearch().getRepositories().size(); i++) {
                    // If there's anything in the default/main branch
                    if (repoData.getSearch().getRepositories().get(i).getRepo().getMainBranch() != null) {

                        // Init don't ignore repo
                        Boolean ignoreRepo = false;
                        // Set a tempRepo var for less code duplication
                        Repo tempRepo = repoData.getSearch().getRepositories().get(i).getRepo();

                        // List of all languages
                        List<LanguageInfo> languageList = new ArrayList<>();
                        // For top 3 languages
                        for (int j = 0; j < tempRepo.getLanguages().getEdges().size(); j++) {
                            // Add languages to language list
                            languageList.add(
                                    new LanguageInfo(
                                            tempRepo.getLanguages().getEdges().get(j).getNode().getName(),
                                            tempRepo.getLanguages().getEdges().get(j).getSize()
                                    )
                            );
                            // If specified language over specified threshold, don't add repo
                            if (tempRepo.getLanguages().getEdges().get(j).getNode().getName() == language)
                                if ((tempRepo.getLanguages().getEdges().get(j).getSize() / tempRepo.getLanguages().getTotalSize() < percentLanguage))
                                    ignoreRepo = true;
                        }

                        // If the mentionable users is less than the specified amount, ignore repo
                        if (tempRepo.getMentionableUsers().getTotalCount() < users)
                            ignoreRepo = true;

                        // If the commit count is less than the specified amount, ignore repo
                        if (tempRepo.getMainBranch().getTarget().getHistory().getTotalCount() < totalCommit)
                            ignoreRepo = true;

                        //If not ignored repo, add to repoCollection array
                        if (!ignoreRepo) {
                            repoCollection.add(
                                    new RepoInfo(
                                            tempRepo.getId(),
                                            tempRepo.getName(),
                                            tempRepo.getUrl(),
                                            tempRepo.getDescription(),
                                            tempRepo.getPrimaryLanguage().getName(),
                                            tempRepo.getCreatedAt(),
                                            tempRepo.getUpdatedAt(),
                                            tempRepo.getPushedAt(),
                                            tempRepo.getIsArchived(),
                                            tempRepo.getIsFork(),
                                            tempRepo.getIssueUsers().getTotalCount(),
                                            tempRepo.getMentionableUsers().getTotalCount(),
                                            tempRepo.getLanguages().getTotalSize(),
                                            tempRepo.getMainBranch().getTarget().getHistory().getTotalCount(),
                                            tempRepo.getForkCount(),
                                            tempRepo.getStargazerCount(),
                                            tempRepo.getWatchers().getTotalCount(),
                                            languageList
                                    )
                            );
                            System.out.println("** Added " + tempRepo.getName() + " (" + tempRepo.getUrl() + ")");
                            addedRepos++;
                        } else
                            ignoredRepos++;
                    }
                }

                try {
                    // Wait 4 seconds each query to reduce API limits
                    System.out.println("[INFO] Wait 4 seconds...");
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException e) {
                    System.out.println("[ERROR] Error trying to wait: \n"+e);
                }

                // While there is a next page AND you still have at least 100 API calls left
                if(repoData.getSearch().getPageInfo().getHasNextPage()&&repoData.getRateLimit().getRemaining()>100){
                    // Print status on cursor/remaining API
                    System.out.println("[INFO] Next cursor: "+repoData.getSearch().getPageInfo().getEndCursor()+" :: Next Page: "+repoData.getSearch().getPageInfo().getHasNextPage()+" :: Remaining API: "+repoData.getRateLimit().getRemaining());
                    // Recurse with next cursor
                    getRepos(repoData.getSearch().getPageInfo().getEndCursor());
                }
                // After there's no more pages, go to the next chunk until the ending date is after the current date
                if(endingDate.isBefore(currentDate)&&repoData.getRateLimit().getRemaining()>100) {
                    // Update beginningDate to be last endingDate
                    beginningDate = endingDate;

                    endingDate = endingDate.plusDays(addedTime);
                    if(endingDate.isAfter(currentDate))
                        // Set to current date
                        endingDate=currentDate;

                    // Print status on date range/remaining API
                    System.out.println("[INFO] Date range:"+beginningDate+" to "+endingDate+" :: Remaining API:"+repoData.getRateLimit().getRemaining());
                    // Recurse with no cursor
                    getRepos(null);
                }
            }
        }
    }

    // Get all repos
    public List<RepoInfo> getRepos(){return repoCollection.stream().toList();}

    // Get a single repo's data
    public RepoInfo getRepo(Integer id){return getRepos().get(id);}

    public int getIgnoredRepos(){
        return ignoredRepos;
    }
    public int getAddedRepos(){
        return addedRepos;
    }

}
