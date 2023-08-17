package com.troxal;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import com.fasterxml.jackson.core.*;

import java.util.concurrent.TimeUnit;

public class GitHubGraphQL {
    public static String post(String authToken,String query,String variables) {
        HttpResponse<JsonNode> httpResponse = Unirest.post("https://api.github.com/graphql")
                .header("Authorization", "Bearer " + authToken)
                .body("{\"query\": \""+query+"\","+"\"variables\": "+variables+"}")
                .asJson();

        try {
            TimeUnit.SECONDS.sleep(7);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //System.out.println(httpResponse.getHeaders().toString());

        return httpResponse.getBody().getObject().toString(4);
    }

    public static Data getData(String authToken,String query,String variables) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Repo d = objectMapper.readValue(post(authToken,query,variables), Repo.class);
        return d.getData();
    }

}
