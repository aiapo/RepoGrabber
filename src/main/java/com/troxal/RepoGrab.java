package com.troxal;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepoGrab{
    static private String authToken;
    private String variables;
    Data JSONResponse = null;

    List<RepoData> repos = new ArrayList<>();

    public RepoGrab(String authToken,String variables) {
        this.authToken = authToken;
        this.variables = variables;

        queryData();
        jsonToRepoData();
    }

    private void queryData(){
        GitHubGraphQL api = new GitHubGraphQL();
        String query = "query listRepos($queryString: String!,$amountReturned: Int!) { rateLimit { cost remaining resetAt } search(query: $queryString, type: REPOSITORY, first: $amountReturned) { repositoryCount pageInfo { endCursor startCursor } edges { node { ... on Repository { id name createdAt isArchived isPrivate url assignableUsers { totalCount } languages(first: 3, orderBy: {field: SIZE, direction: DESC}) { edges { size node { name } } totalSize } defaultBranchRef { target { ... on Commit { history { totalCount } } } } } } } } }";

        try{
            JSONResponse = api.getData(authToken,query,variables);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Data getData(){
        return JSONResponse;
    }

    public Integer getAPICallsLeft(){
        return getData().getRateLimit().getRemaining();
    }

    public Integer getTotalRepoCount(){
        return getData().getSearch().getRepositoryCount();
    }

    public List<RepoData> getRepos(){
        return repos;
    }

    public RepoData getRepo(Integer id){
        return getRepos().get(id);
    }

    private void jsonToRepoData(){
        for(int i=0;i<getData().getSearch().getEdges().size();i++){
            Node tempRepo = getData().getSearch().getEdges().get(i).getNode();
            List<Language> languages = new ArrayList<>();
            for(int j=0;j<tempRepo.getLanguages().getEdges().size();j++){
                languages.add(new Language(tempRepo.getLanguages().getEdges().get(j).getNode().getName(),tempRepo.getLanguages().getEdges().get(j).getSize()));
            }
            repos.add(new RepoData(tempRepo.getId(), tempRepo.getName(), tempRepo.getUrl(), tempRepo.getCreatedAt(), tempRepo.getAssignableUsers().getTotalCount(),tempRepo.getLanguages().getTotalSize(),languages));
        }
    }

}
