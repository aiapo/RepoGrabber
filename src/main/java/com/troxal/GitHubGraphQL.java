package com.troxal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import java.util.concurrent.TimeUnit;

/**
 * Class: GitHubGraphQL
 * Description: Handles POSTing to the GitHub GraphQL API and handling getting the data from JSON into objects
 * TODO: Generalize class/clean up class
 */
public class GitHubGraphQL {
    // POSTS to GH GraphQL
    // Requires: GitHub authToken, GraphQL query, GraphQL variables
    public static String post(String authToken,String query,String variables) {
        // Use Unirest to POST
        // Incluses GraphQL endpoint, auth token, query and variables
        HttpResponse<JsonNode> httpResponse = Unirest.post("https://api.github.com/graphql")
                .header("Authorization", "Bearer " + authToken)
                .body("{\"query\": \""+query+"\","+"\"variables\": "+variables+"}")
                // I ran into an issue where the 10000ms timeout ran out, so I increased it; could potentially remove
                .connectTimeout(20000)
                .asJson();

        // Try/Catch to throw error if wait fails
        try {
            // Wait 10 seconds each query to reduce API limits
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Print out the headers (some API limit stuff is stored here, we could potentially use)
        //System.out.println(httpResponse.getHeaders().toString());

        // Return the body of the response
        return httpResponse.getBody().getObject().toString(4);
    }

    // Uses POST to get the data, then converts the JSON data returned to Objects
    public static Data getData(String authToken,String query,String variables) throws JsonProcessingException {
        // Create a ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        // Get the JSON data from the POST query and map each JSON Object to their respective Java Object
        Repo d = objectMapper.readValue(post(authToken,query,variables), Repo.class);
        // Return the Data Object
        return d.getData();
    }

}
