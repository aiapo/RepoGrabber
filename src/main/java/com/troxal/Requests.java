package com.troxal;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.Headers;

public class Requests {
    private String body;
    private Headers headers;
    private Integer status;

    public Requests(){
    }
    public Requests(Integer status,Headers headers,String body){
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    public Integer getStatus(){
        return status;
    }
    public Headers getHeader(){
        return headers;
    }
    public String getBody(){
        return body;
    }

    // POST request with an authToken and body
    public static Requests post(String url,String authToken,String body) {
        try {
            // Use Unirest to POST
            // Includes url, auth token, body
            HttpResponse<JsonNode> httpResponse = Unirest.post(url)
                    .header("Authorization", "Bearer " + authToken)
                    .body(body)
                    .connectTimeout(20000)
                    .asJson();

            //System.out.println("[DEBUG] Sent POST Request to: "+url);

            // Return the headers and the body
            return new Requests(
                    httpResponse.getStatus(),
                    httpResponse.getHeaders(),
                    httpResponse.getBody().getObject().toString(4)
            );
        } catch (UnirestException e) {
            System.out.println("** Unirest had an error: \n - "+e+"\n - URL: "+url+"\n - Auth: "+authToken+"\n - Body: "+body);
            return new Requests(400,null,"Unirest Error, see previous log.");
        }
    }

    // POST with just authToken
    public static Requests get(String url,String authToken){
        try {
            // Use Unirest to GET
            // Includes url, auth token
            HttpResponse<JsonNode> httpResponse = Unirest.get(url)
                    .header("Authorization", "Bearer " + authToken)
                    .connectTimeout(20000)
                    .asJson();

            //System.out.println("[DEBUG] Sent GET Request to: "+url);

            // Return the headers and the body
            return new Requests(
                    httpResponse.getStatus(),
                    httpResponse.getHeaders(),
                    httpResponse.getBody().getObject().toString(4)
            );
        } catch (UnirestException e) {
            System.out.println("** Unirest had an error: \n - "+e+"\n - URL: "+url+"\n - Auth: "+authToken);
            return new Requests(400,null,"Unirest Error, see previous log.");
        }
    }
}
