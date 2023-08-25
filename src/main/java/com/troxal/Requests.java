package com.troxal;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

public class Requests {
    private String headers,body;
    private Integer status;

    public Requests(){
    }
    public Requests(Integer status,String headers,String body){
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    public Integer getStatus(){
        return status;
    }
    public String getHeader(){
        return headers;
    }
    public String getBody(){
        return body;
    }

    // POST request with an authToken, body and timeout
    public static Requests post(String url,String authToken,String body) {
        try {
            // Use Unirest to POST
            // Incluses GraphQL endpoint, auth token, query and variables
            HttpResponse<JsonNode> httpResponse = Unirest.post(url)
                    .header("Authorization", "Bearer " + authToken)
                    .body(body)
                    .connectTimeout(20000)
                    .asJson();

            // Return the headers and the body
            return new Requests(
                    httpResponse.getStatus(),
                    httpResponse.getHeaders().toString(),
                    httpResponse.getBody().getObject().toString(4)
            );
        } catch (UnirestException e) {
            System.out.println("** Unirest had an error: \n - "+e+"\n - URL: "+url+"\n - Auth: "+authToken+"\n - Body: "+body);
            return new Requests(400,null,"Unirest Error, see previous log.");
        }
    }

}