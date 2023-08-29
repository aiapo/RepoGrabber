package com.troxal;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class GitHub {
    // Query GraphQL API, returns headers and body
    public static Requests GraphQL(String authToken, String query, String variables){
        String body = "{\"query\": \""+query+"\","+"\"variables\": "+variables+"}";
        return apiHandler(Requests.post("https://api.github.com/graphql",authToken,body));
    }

    // Query REST API, returns headers and body
    public static Requests REST(String authToken,String query){
        return apiHandler(Requests.get("https://api.github.com"+query,authToken));
    }

    // Handle the requests
    private static Requests apiHandler(Requests data){
        // Use HTTP Status responses to determine if we have an error or not
        // 200 = OK
        if(data.getStatus()==200) {
            return data;
        // 401 = Unauthorized
        }else if(data.getStatus()==401){
            System.out.println("[ERROR] You provided an invalid or expired GitHub API Key. Make sure you put your key in 'github-oauth.properties'");
            return null;
        // 403 = Forbidden
        }else if(data.getStatus()==403){
            Long ghTimeout;
            // If no rate calls left
            if(Integer.valueOf(data.getHeader().getFirst("x-ratelimit-remaining"))==0){
                ghTimeout = Long.valueOf(data.getHeader().getFirst("x-ratelimit-reset"))- Instant.now().getEpochSecond();
                System.out.println("[ERROR] You've ran out of API queries, so you have to wait "+ghTimeout+" seconds...\n - Response:\n"+data.getBody());
            }else{
                ghTimeout = Long.valueOf(data.getHeader().getFirst("retry-after"));
                System.out.println("[ERROR] A rate limit or unauthorized request encountered... waiting "+ghTimeout+" seconds...\n - Response:\n"+data.getBody());
            }
            try {
                // Wait determined by 'retry-after' seconds to comply with API limits
                TimeUnit.SECONDS.sleep(ghTimeout);
            } catch (InterruptedException e) {
                System.out.println("[ERROR] Error trying to wait: \n"+e);
            }
            return null;
            // 502 = Bad Gateway (this seems to be when GraphQL timeouts)
        }else if(data.getStatus()==502){
            System.out.println("[ERROR] A bad response encountered... waiting "+15+" seconds...\n - Response:\n"+data.getBody());
            try {
                // Wait 15 seconds to try again
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                System.out.println("[ERROR] Error trying to wait: \n"+e);
            }
            return null;
            // Just error if any other status
        } else{
            System.out.println("[ERROR] Encountered an error from GitHub: \n - Status:"+data.getStatus()+"\n - Body: "+data.getBody());
            return null;
        }
    }
}
