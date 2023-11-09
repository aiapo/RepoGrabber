package com.troxal.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.troxal.pojo.Data;
import com.troxal.pojo.GitHubJSON;

public class Mapper {
    // Converts the JSON response to POJO
    private static Data jsonToRepo(String responseData){
        // Create a ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Return the JSON data from the POST query and map each JSON Object to their respective Java Object
            GitHubJSON d = objectMapper.readValue(responseData, GitHubJSON.class);
            if(d.getErrors()==null)
                return d.getData();
            else{
                // So GitHub API returns 200 on an error, that's bad API design but whatever
                System.out.println("[ERROR] Encountered an error on from GitHub: \n - "+d.getErrors().get(0).getMessage());
                Data tempData = new Data();
                tempData.setErrorType(d.getErrors().get(0).getType());
                return tempData;
            }

        } catch (JsonMappingException e) {
            System.out.println("[ERROR] Encountered an error on when JSON mapping: \n"+e);
        } catch (JsonProcessingException e) {
            System.out.println("[ERROR] Encountered an error on when JSON processing: \n"+e);
        }
        return null;
    }

    // Gets the data from the API and error handles API stuff
    public static Data queryData(String query, String queryVariables, String authToken){
        // Actually make the query
        Requests data = GitHub.GraphQL(authToken, query, queryVariables);

        // If we have no error, map
        if(data!=null) {
            // Convert JSON response to Object
            Data repos = jsonToRepo(data.getBody());
            // If no error mapping, return the mapped repos
            if (repos!=null)
                return repos;
        }
        return null;
    }
}
