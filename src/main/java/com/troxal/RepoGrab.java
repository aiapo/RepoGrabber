package com.troxal;

import com.troxal.database.Database;
import com.troxal.database.Manager;
import com.troxal.manipulation.CSV;
import com.troxal.pojo.Data;
import com.troxal.pojo.LanguageInfo;
import com.troxal.pojo.Repo;
import com.troxal.pojo.RepoInfo;
import com.troxal.request.GitHub;
import com.troxal.request.Mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RepoGrab {
    // Initialize variables
    private final String authToken = Config.get("AUTH_TOKEN");
    private String language;
    private Integer followers,users,percentLanguage,totalCommit,minTotalSize,maxTotalSize,ignoredRepos=0,addedRepos = 0;
    private Integer addedTime=20;
    private Integer amountReturned=35;
    private Boolean ranAtLeastOnce=false;
    private LocalDate beginningDate = LocalDate.parse("2010-01-01"), endingDate = LocalDate.parse("2010-01-15"),currentDate=LocalDate.now();
    private Set<RepoInfo> repoCollection = new HashSet<>();
    private Database db;

    // Constructor to get query info, basically just sets all the variable data to their respective global variables
    public RepoGrab(Integer followers, String language, Integer users, Integer percentLanguage, Integer totalCommit,
                    Integer minTotalSize, Integer maxTotalSize, String sDate, String endDate) {
        this.followers=followers;
        this.language=language;
        this.users=users;
        this.percentLanguage=percentLanguage;
        this.totalCommit=totalCommit;
        this.minTotalSize = minTotalSize;
        this.maxTotalSize = maxTotalSize;
        this.beginningDate = LocalDate.parse(sDate);
        this.endingDate = LocalDate.parse(endDate);
        this.addedTime= Math.toIntExact(ChronoUnit.DAYS.between(beginningDate, endingDate));
        this.db = new Manager().access();

        getRepos(null);
    }

    // Import CSV/DB constructor
    public RepoGrab(Boolean headless,Boolean fromDB){
        this.db = new Manager().access();

        if(fromDB)
            repoCollection = new HashSet<>(getFromDB());
        else
            repoCollection = new HashSet<>(CSV.read(headless));

        System.out.println("Imported "+repoCollection.size()+" repos!");
    }

    // Easy GraphQL variable generator
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
        if(minTotalSize !=null) {
            sb.append(" size:>="+ minTotalSize);
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

    // Optimizes the chunk period so that it will return a chunk that has between 900-1000 repos
    private void dayOptimizer(Integer days){
        try {
            // Wait 1 seconds to comply with API limits
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            System.out.println("[ERROR] Error trying to wait: \n"+e+" (RepoGrab.java)");
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
        Data repoData = Mapper.queryData(query,generateVariables(null),authToken);

        // If no error from API
        if(repoData!=null){
            Integer totalRepoCount = repoData.getSearch().getRepositoryCount();
            if((totalRepoCount>=900&&totalRepoCount<=1000)||
                    ((endingDate.isEqual(currentDate)||endingDate.isAfter(currentDate))&&totalRepoCount<=1000)) {
                addedTime = days;
                System.out.println("[INFO] Landed on "+addedTime+" days for the optimal period.");
            }else{
                if(totalRepoCount>1000){
                    addedTime=days/2;
                }else{
                    addedTime=days+(100-(totalRepoCount/10));
                }
                endingDate=beginningDate.plusDays(addedTime);
                dayOptimizer(addedTime);
            }

        }else
            System.out.println("error with repoData");
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
                                "owner { username: login } " +
                                "url " +
                                "description " +
                                "createdAt " +
                                "updatedAt " +
                                "pushedAt " +
                                "isArchived " +
                                "archivedAt " +
                                "isPrivate " +
                                "isFork " +
                                "isEmpty " +
                                "isLocked " +
                                "isDisabled " +
                                "isTemplate " +
                                "primaryLanguage { name } " +
                                "forkCount " +
                                "issues { totalCount } " +
                                "stargazerCount " +
                                "watchers { totalCount } " +
                                "issueUsers: assignableUsers { totalCount } " +
                                "mentionableUsers { totalCount } " +
                                "languages(first: 3, orderBy: {field: SIZE, direction: DESC}) { " +
                                    "edges { size node { name } } " +
                                "totalSize " +
                            "} " +
                            "mainBranch: defaultBranchRef { " +
                                "name " +
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
        Data repoData = Mapper.queryData(query,generateVariables(endCursor),authToken);

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
                        // Set a tempRepo var for less code duplication
                        Repo tempRepo = repoData.getSearch().getRepositories().get(i).getRepo();

                        // Try adding repo, add to repoCollection array
                        if (addRepo(tempRepo)) {
                            System.out.println("** Added " + tempRepo.getName() + " (" + tempRepo.getUrl() + ")");
                            addedRepos++;
                        } else
                            ignoredRepos++;
                    }
                }

                // While there is a next page AND you still have at least 100 API calls left
                if(repoData.getSearch().getPageInfo().getHasNextPage()&&repoData.getRateLimit().getRemaining()>100){
                    // Print status on cursor/remaining API
                    System.out.println("[INFO] Next cursor: "+repoData.getSearch().getPageInfo().getEndCursor()+" :: Remaining API: "+repoData.getRateLimit().getRemaining());
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
    public List<RepoInfo> getRepos(){
        return repoCollection.stream().toList();
    }

    // Get a single repo's data
    public RepoInfo getRepo(Integer id){return getRepos().get(id);}

    private Boolean addRepo(Repo tempRepo){
        // If no languages, don't add repo
        if(tempRepo.getLanguages().getEdges().isEmpty())
            return false;

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
                    return false;
        }

        // If the commit count is less than the specified amount, ignore repo
        if (tempRepo.getMainBranch().getTarget().getHistory().getTotalCount() < totalCommit)
            return false;

        // If the size is over the max, ignore repo
        if(tempRepo.getLanguages().getTotalSize()>maxTotalSize)
            return false;

        Integer commiterCount = GitHub.getCommiters(tempRepo.getUrl());

        // If the committer users is less than the specified amount, ignore repo
        if (commiterCount < users)
            return false;

        //If not ignored repo, add to repoCollection array
        RepoInfo Repo = new RepoInfo(
                tempRepo.getId(),
                tempRepo.getName(),
                tempRepo.getOwner().getUsername(),
                tempRepo.getUrl(),
                tempRepo.getDescription(),
                tempRepo.getPrimaryLanguage().getName(),
                tempRepo.getCreatedAt(),
                tempRepo.getUpdatedAt(),
                tempRepo.getPushedAt(),
                tempRepo.getIsArchived(),
                tempRepo.getArchivedAt(),
                tempRepo.getIsFork(),
                tempRepo.getIsEmpty(),
                tempRepo.getIsLocked(),
                tempRepo.getIsDisabled(),
                tempRepo.getIsTemplate(),
                tempRepo.getIssueUsers().getTotalCount(),
                tempRepo.getMentionableUsers().getTotalCount(),
                commiterCount,
                tempRepo.getLanguages().getTotalSize(),
                tempRepo.getMainBranch().getTarget().getHistory().getTotalCount(),
                tempRepo.getIssues().getTotalCount(),
                tempRepo.getForkCount(),
                tempRepo.getStargazerCount(),
                tempRepo.getWatchers().getTotalCount(),
                languageList,
                tempRepo.getMainBranch().getName()
        );

        // If repo already added, don't add it again
        if(checkExists(Repo))
            return false;

        repoCollection.add(Repo);
        addToDB(Repo);
        return true;
    }

    public int getIgnoredRepos(){
        return ignoredRepos;
    }
    public int getAddedRepos(){
        return addedRepos;
    }

    private Boolean checkExists(RepoInfo repo){
        for(RepoInfo r : repoCollection)
            if(r==repo)
                return true;
        return false;
    }

    public void addToDB(RepoInfo repo){
        List<Object[]> a = new ArrayList<>();
        a.add(new Object[]{

            repo.getId(),
            repo.getName(),
            repo.getOwner(),
            repo.getUrl(),
            repo.getDescription(),
            repo.getPrimaryLanguage(),
            repo.getCreationDate(),
            repo.getUpdateDate(),
            repo.getPushDate(),
            repo.getIsArchived(),
            repo.getArchivedAt(),
            repo.getIsFork(),
            repo.getIsEmpty(),
            repo.getIsLocked(),
            repo.getIsDisabled(),
            repo.getIsTemplate(),
            repo.getTotalIssueUsers(),
            repo.getTotalMentionableUsers(),
            repo.getTotalCommitterCount(),
            repo.getTotalProjectSize(),
            repo.getTotalCommits(),
            repo.getIssueCount(),
            repo.getForkCount(),
            repo.getStarCount(),
            repo.getWatchCount(),
            repo.getBranchName()
        });

        if(db.insert("Repositories",a)!=null)
            System.out.println("[INFO] Added repo: "+repo.getName());
        else
            System.out.println("[ERROR] Failed to add repo: "+repo.getName());

        for(int j=0;j<repo.getLanguages().size();j++){
            Object[] newLanguage = {
                    repo.getId(),
                    repo.getLanguages().get(j).getName(),
                    repo.getLanguages().get(j).getSize()
            };
            if(db.insert("Languages",new Object[]{"repoid","name","size"},newLanguage))
                System.out.println("[INFO] Added "+repo.getLanguages().get(j).getName()+" to "+repo.getName());
            else
                System.out.println("[ERROR] Failed to add "+repo.getLanguages().get(j).getName()+" to "+repo.getName());
        }
    }

    private List<RepoInfo> getFromDB(){
        List<RepoInfo> repositories = new ArrayList<>();
        try(ResultSet repos = db.select("Repositories",new String[]{"*"})){
            while(repos.next()){
                try {
                    List<LanguageInfo> languageList = new ArrayList<>();
                    try(ResultSet lang = db.select("Languages",new String[]{"*"},"repoid = ?",
                            new Object[]{repos.getString("id")})){
                        while(lang.next()){
                            languageList.add(
                                    new LanguageInfo(
                                            lang.getString("name"),
                                            lang.getInt("size")
                                    )
                            );
                        }
                    }catch(SQLException ex){
                        System.out.println("[ERROR] Error getting languages from db: "+ex);
                    }
                    repositories.add(
                            new RepoInfo(
                                    repos.getString("id"),
                                    repos.getString("name"),
                                    repos.getString("owner"),
                                    repos.getString("url"),
                                    repos.getString("description"),
                                    repos.getString("primarylanguage"),
                                    repos.getString("creationdate"),
                                    repos.getString("updatedate"),
                                    repos.getString("pushdate"),
                                    repos.getBoolean("isarchived"),
                                    repos.getString("archivedat"),
                                    repos.getBoolean("isforked"),
                                    repos.getBoolean("isempty"),
                                    repos.getBoolean("islocked"),
                                    repos.getBoolean("isdisabled"),
                                    repos.getBoolean("istemplate"),
                                    repos.getInt("totalissueusers"),
                                    repos.getInt("totalmentionableusers"),
                                    repos.getInt("totalcommittercount"),
                                    repos.getInt("totalprojectsize"),
                                    repos.getInt("totalcommits"),
                                    repos.getInt("issuecount"),
                                    repos.getInt("forkcount"),
                                    repos.getInt("starcount"),
                                    repos.getInt("watchcount"),
                                    languageList,
                                    repos.getString("branchname")
                            )
                    );
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }catch(SQLException ex){
            System.out.println("[ERROR] Error getting repositories from db: "+ex);
        }



        return repositories;
    }
}
