package com.troxal.request;

import org.jsoup.Connection;
import org.jsoup.select.Elements;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.jsoup.Jsoup.parse;

public class GitHub {
    // Query GraphQL API, returns headers and body
    public static Requests GraphQL(String authToken, String query, String variables){
        String body = "{\"query\": \""+query+"\","+"\"variables\": "+variables+"}";

        Integer i=0;
        while(i++<5){
            Requests returnRequest = apiHandler(Requests.post("https://api.github.com/graphql",authToken,body));
            if(returnRequest!=null){
                return returnRequest;
            }
        }
        return null;
    }

    // Query REST API, returns headers and body
    public static Requests REST(String authToken,String query){
        return apiHandler(Requests.get("https://api.github.com"+query,authToken));
    }

    public static Integer getCommiters(String url){
        Connection.Response page = Scrape.scrapePage(url);
        Elements e = parse(page.body()).select("a.Link--primary.no-underline.Link.d-flex.flex-items-center");
        for(int i=0;i<e.size();i++)
            if(e.get(i).text().contains("Contributors"))
                return Integer.valueOf(e.get(i).select("span.Counter.ml-1").text().replace(",",""));
        return -1;
    }

    public static Map<String,String> getCommitInfo(String url){
        Map a = new HashMap<>();
        Connection.Response page = Scrape.scrapePage(url);

        String title = parse(page.body()).select("div.commit-title.markdown-title").text();
        String description = null;
        String author = parse(page.body()).select("a.commit-author.user-mention").text();
        String time = parse(page.body()).select("relative-time.no-wrap").attr("datetime");

        if(parse(page.body()).select("div.commit-desc").get(0).hasText()){
            description = parse(page.body()).select("div.commit-desc").get(0).text();
        }
        
        if(title.endsWith("…")&&description.startsWith("…")){
            a.put("title",title.replace("…",description.replace("…","")));
            a.put("description",null);
        }else{
            a.put("title",title);
            a.put("description",description);
        }
        a.put("author",author);
        a.put("time",time);

        return a;
    }

    // Handle the requests
    private static Requests apiHandler(Requests data){
        Long ghTimeout;
        // Use HTTP Status responses to determine if we have an error or not
        // 200 = OK
        if(data.getStatus()==200) {
            if(Integer.valueOf(data.getHeader().getFirst("X-RateLimit-Remaining"))==0){
                ghTimeout =
                        (Long.valueOf(data.getHeader().getFirst("x-ratelimit-reset"))
                                - Instant.now().getEpochSecond())+2;
                System.out.println("[ERROR] You've ran out of API queries, so you have to wait "+ghTimeout+" seconds...\n - Response:\n"+data.getBody());
                try {
                    // Wait determined by 'retry-after' seconds to comply with API limits
                    TimeUnit.SECONDS.sleep(ghTimeout);
                } catch (InterruptedException e) {
                    System.out.println("[ERROR] Error trying to wait: \n"+e);
                }
            }else{
                return data;
            }

        // 401 = Unauthorized
        }else if(data.getStatus()==401){
            System.out.println("[ERROR] You provided an invalid or expired GitHub API Key. Make sure you put your key" +
                    " in 'config/.env'");

        // 403 = Forbidden
        }else if(data.getStatus()==403){

            try{
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
            }catch(NumberFormatException e){
                System.out.println("[ERROR] Number format: "+data.getBody());
                try {
                    System.out.println("Sleep 5 seconds...");
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ex) {
                    System.out.println("[ERROR] Error trying to wait: \n"+ex);
                }
            }

        // 502 = Bad Gateway (this seems to be when GraphQL timeouts)
        }else if(data.getStatus()==502){
            System.out.println("[ERROR] A bad response encountered... waiting "+15+" seconds...\n - Response:\n"+data.getBody());
            try {
                // Wait 15 seconds to try again
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                System.out.println("[ERROR] Error trying to wait: \n"+e);
            }
        // Just error if any other status
        }else{
            System.out.println("[ERROR] Encountered an error from GitHub: \n - Status:"+data.getStatus()+"\n - Body: "+data.getBody());
        }
        return null;
    }
}
