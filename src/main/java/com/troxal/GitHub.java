package com.troxal;

public class GitHub {
    // Query GraphQL API, returns headers and body
    public static Requests GraphQL(String authToken, String query, String variables){
        String body = "{\"query\": \""+query+"\","+"\"variables\": "+variables+"}";
        return Requests.post("https://api.github.com/graphql",authToken,body);
    }

    // Query REST API, returns headers and body
    public Requests REST(String authToken,String query){
        //TODO: Implement REST API
        return null;
    }

}
