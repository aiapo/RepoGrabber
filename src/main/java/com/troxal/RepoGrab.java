package com.troxal;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.FileWriter;
import java.io.IOException;
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
    private String variables,pushed,created,languages,isArchived,isPublic;
    private Integer amountReturned;
    private Data JSONResponse = null;
    private List<RepoData> repos = new ArrayList<>();

    public RepoGrab(String isArchived,String isPublic,String pushed,String created,String languages,Integer amountReturned) {
        this.isArchived=isArchived;
        this.isPublic=isPublic;
        this.pushed=pushed;
        this.created=created;
        this.languages=languages;
        this.amountReturned=amountReturned;

        jsonToRepoData(null);
    }

    private void queryData(String endCursor){
        GitHubGraphQL api = new GitHubGraphQL();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"queryString\": \"");
        if(isPublic.toLowerCase()=="y")
            sb.append("is:public");
        if(isArchived.toLowerCase()=="y")
            sb.append(" archived:true");
        else
            sb.append(" archived:false");
        if(pushed!=null)
            sb.append(" pushed:"+pushed);
        if(created!=null)
            sb.append(" created:"+created);
        if(languages!=null) {
            sb.append(" languages:"+languages);
        }
        if(amountReturned!=null)
            sb.append("\",\"amountReturned\":"+amountReturned);
        if(endCursor!=null)
            sb.append(",\"cursorValue\":\""+endCursor+"\"}");
        else
            sb.append(",\"cursorValue\":"+null+"}");
        variables = sb.toString();

        String query = "query listRepos($queryString: String!,$amountReturned: Int!,$cursorValue: String) { rateLimit { cost remaining resetAt } search(query: $queryString, type: REPOSITORY, first: $amountReturned after:$cursorValue) { repositoryCount pageInfo { endCursor hasNextPage } edges { node { ... on Repository { id name createdAt isArchived isPrivate url assignableUsers { totalCount } languages(first: 3, orderBy: {field: SIZE, direction: DESC}) { edges { size node { name } } totalSize } defaultBranchRef { target { ... on Commit { history { totalCount } } } } } } } } }";
        try{
            System.out.println(variables);
            JSONResponse = api.getData(authToken,query,variables);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void jsonToRepoData(String endCursor){
        queryData(endCursor);
        for(int i=0;i<JSONResponse.getSearch().getEdges().size();i++){
            if(JSONResponse.getSearch().getEdges().get(i).getNode().getDefaultBranchRef()!=null){
                Node tempRepo = JSONResponse.getSearch().getEdges().get(i).getNode();
                List<Language> languages = new ArrayList<>();
                for(int j=0;j<tempRepo.getLanguages().getEdges().size();j++){
                    languages.add(new Language(tempRepo.getLanguages().getEdges().get(j).getNode().getName(),tempRepo.getLanguages().getEdges().get(j).getSize()));
                }
                System.out.println("** Added "+tempRepo.getName());
                repos.add(new RepoData(tempRepo.getId(), tempRepo.getName(), tempRepo.getUrl(), tempRepo.getCreatedAt(), tempRepo.getAssignableUsers().getTotalCount(),tempRepo.getLanguages().getTotalSize(),tempRepo.getDefaultBranchRef().getTarget().getHistory().getTotalCount(),languages));
            }else{
                System.out.println("*** Skipped repo...");
            }
        }
        while (JSONResponse.getSearch().getPageInfo().gethasNextPage()&&JSONResponse.getRateLimit().getRemaining()>100){
            jsonToRepoData(JSONResponse.getSearch().getPageInfo().getEndCursor());
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
