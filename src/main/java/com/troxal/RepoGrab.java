package com.troxal;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public class RepoGrab{
    static private String authToken;
    private String variables;
    Data JSONResponse = null;

    public RepoGrab(String authToken,String variables) {
        this.authToken = authToken;
        this.variables = variables;

        queryData();
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

    public List<Edge> getRepos(){
        return getData().getSearch().getEdges();
    }

    public Node getRepo(Integer id){
        return getRepos().get(id).getNode();
    }

}
