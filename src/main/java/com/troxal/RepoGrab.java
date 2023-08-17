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

public class RepoGrab{
    static private String authToken = getConfig.getKey();
    private String variables,language;
    private Integer amountReturned,followers,users,percentLanguage,totalCommit;
    private LocalDate beginningDate = LocalDate.parse("2010-01-01"), endingDate = LocalDate.parse("2010-06-01"),currentDate=LocalDate.now();

    private Data JSONResponse = null;
    private List<RepoData> repos = new ArrayList<>();

    public RepoGrab(Integer followers,String language,Integer users,Integer percentLanguage,Integer totalCommit) {
        this.followers=followers;
        this.language=language;
        this.users=users;
        this.percentLanguage=percentLanguage;
        this.totalCommit=totalCommit;
        this.amountReturned=50;

        jsonToRepoData(null);
    }

    private void queryData(String endCursor){
        GitHubGraphQL api = new GitHubGraphQL();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"queryString\": \"");
        if(followers!=null)
            sb.append("followers:>="+followers);
        if(language!=null) {
            sb.append(" language:"+language);
        }
        sb.append(" pushed:");
        sb.append(beginningDate+".."+endingDate);
        if(amountReturned!=null)
            sb.append("\",\"amountReturned\":"+amountReturned);
        if(endCursor!=null)
            sb.append(",\"cursorValue\":\""+endCursor+"\"}");
        else
            sb.append(",\"cursorValue\":"+null+"}");
        variables = sb.toString();

        String query = "query listRepos($queryString: String!,$amountReturned: Int!,$cursorValue: String) { rateLimit { cost remaining resetAt } search(query: $queryString, type: REPOSITORY, first: $amountReturned after:$cursorValue) { repositoryCount pageInfo { endCursor hasNextPage } edges { node { ... on Repository { id name createdAt isArchived isPrivate url assignableUsers { totalCount } languages(first: 3, orderBy: {field: SIZE, direction: DESC}) { edges { size node { name } } totalSize } defaultBranchRef { target { ... on Commit { history { totalCount } } } } } } } } }";
        try{
            System.out.println("Variables: "+variables);
            JSONResponse = api.getData(authToken,query,variables);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void jsonToRepoData(String endCursor){
        queryData(endCursor);
        for(int i=0;i<JSONResponse.getSearch().getEdges().size();i++){
            if(JSONResponse.getSearch().getEdges().get(i).getNode().getDefaultBranchRef()!=null){
                Boolean ignoreRepo=false;
                Node tempRepo = JSONResponse.getSearch().getEdges().get(i).getNode();
                List<Language> languages = new ArrayList<>();
                for(int j=0;j<tempRepo.getLanguages().getEdges().size();j++){
                    Languages tempLanguage = tempRepo.getLanguages();
                    if(tempLanguage.getEdges().get(j).getNode().getName()==language)
                        if((tempLanguage.getEdges().get(j).getSize()/tempLanguage.getTotalSize())>=percentLanguage){
                            languages.add(new Language(tempRepo.getLanguages().getEdges().get(j).getNode().getName(),tempRepo.getLanguages().getEdges().get(j).getSize()));
                        }else{
                            ignoreRepo=true;
                        }
                }
                if(tempRepo.getAssignableUsers().getTotalCount()<users)
                    ignoreRepo=true;

                if(tempRepo.getDefaultBranchRef().getTarget().getHistory().getTotalCount()<totalCommit)
                    ignoreRepo=true;

                if(!ignoreRepo){
                    repos.add(new RepoData(tempRepo.getId(), tempRepo.getName(), tempRepo.getUrl(), tempRepo.getCreatedAt(), tempRepo.getAssignableUsers().getTotalCount(),tempRepo.getLanguages().getTotalSize(),tempRepo.getDefaultBranchRef().getTarget().getHistory().getTotalCount(),languages));
                    System.out.println("** Added "+tempRepo.getName());
                }else{
                    //System.out.println("*** Repo doesn't meet thresholds.");
                }
            }
        }
        while (JSONResponse.getSearch().getPageInfo().gethasNextPage()&&JSONResponse.getRateLimit().getRemaining()>100){
            System.out.println("Next cursor:"+JSONResponse.getSearch().getPageInfo().getEndCursor()+" :: Remaining API:"+JSONResponse.getRateLimit().getRemaining());
            jsonToRepoData(JSONResponse.getSearch().getPageInfo().getEndCursor());
        }
        while (JSONResponse.getRateLimit().getRemaining()>100&&endingDate.isBefore(currentDate)) {
            beginningDate = endingDate;
            endingDate = endingDate.plusMonths(6);
            System.out.println("Date range:"+beginningDate+" to "+endingDate+" :: Remaining API:"+JSONResponse.getRateLimit().getRemaining());
            jsonToRepoData(null);
        }
    }

    private List<RepoData> getData(){
        return repos;
    }

    public Integer getAPICallsLeft(){
        return JSONResponse.getRateLimit().getRemaining();
    }

    public Integer getTotalRepoCount(){
        return JSONResponse.getSearch().getRepositoryCount();
    }

    public List<RepoData> getRepos(){
        return getData();
    }

    public RepoData getRepo(Integer id){
        return getRepos().get(id);
    }

    private boolean cloneRepo(String url,String name){
        try{
            String cleanPath = FileNameCleaner.cleanFileName(name);
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

    public void cloneRepos(){
        for(int i=0;i<getRepos().size();i++) {
            System.out.println("** Cloning "+getRepo(i).getName());
            if(cloneRepo(getRepo(i).getUrl(),getRepo(i).getName()))
                System.out.println("** Clone successful");
        }
    }

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
