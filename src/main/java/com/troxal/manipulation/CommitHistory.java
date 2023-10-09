package com.troxal.manipulation;

import com.troxal.Config;
import com.troxal.RepoGrab;
import com.troxal.database.Database;
import com.troxal.database.Manager;
import com.troxal.pojo.*;
import com.troxal.request.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommitHistory {
    private final String authToken = Config.get("AUTH_TOKEN");

    public CommitHistory(RepoGrab rg){
        getHistories(rg);
    }

    // Easy GraphQL variable generator
    private String generateVariables(String endCursor, String owner, String name){
        // StringBuilder just allows for easier if/else of the variable string
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        // If there's an owner, set
        if(owner!=null)
            sb.append("\"owner\":\""+owner+"\"");
        // If there's a name, set
        if(name!=null) {
            sb.append(",\"name\":\""+name+"\"");
        }
        // if endCursor then put, else null
        if(endCursor!=null)
            sb.append(",\"cursorValue\":\""+endCursor+"\"}");
        else
            sb.append(",\"cursorValue\":"+null+"}");
        return sb.toString();
    }

    public void getHistories(RepoGrab repos){
        ExecutorService executor = Executors.newWorkStealingPool();
        String query =
                "query listRepos($owner: String!, $name: String!, $cursorValue: String) { " +
                    "rateLimit { " +
                        "cost " +
                        "remaining " +
                        "resetAt " +
                    "} " +
                    "repo: repository(owner: $owner, name: $name) { " +
                        "id " +
                        "name " +
                        "mainBranch: defaultBranchRef { " +
                            "name " +
                            "target { " +
                                "... on Commit { " +
                                    "history(after: $cursorValue){ " +
                                        "nodes { " +
                                            "id " +
                                            "authoredDate " +
                                        "} " +
                                        "pageInfo{ " +
                                            "endCursor " +
                                            "hasNextPage " +
                                        "}" +
                                    "}" +
                                "}" +
                            "}" +
                        "}" +
                    "}" +
                "}";

        for(RepoInfo r : repos.getRepos()){
            String endCursor = null;
            Boolean hasNextPage=true;

            String pURL[] = r.getUrl().replace("https://github.com/","").split("/");

            String owner = pURL[0];
            String name = pURL[1];

            while(hasNextPage){
                // For sake of testing, output the current variables
                System.out.println("[DEBUG] Variables: "+generateVariables(endCursor,owner,name));

                // Get the data
                Data repoData = Mapper.queryData(query,generateVariables(endCursor,owner,name),authToken);

                if(repoData!=null){
                    if (repoData.getRepo().getMainBranch() != null) {
                        MainBranch mB = repoData.getRepo().getMainBranch();
                        List<Object[]> commitList = new ArrayList<>();

                        for (Node n : mB.getTarget().getHistory().getNodes()){
                            commitList.add(new Object[]{
                                    n.getId(),
                                    r.getId(),
                                    LocalDateTime.parse(n.getAuthoredDate().replace("Z",""))
                            });
                        }

                        executor.submit(() -> {
                            Database db=new Manager().access();
                            db.insert("CommitInfo",commitList);
                            db.close();
                        });

                        // Print status on cursor/remaining API
                        System.out.println("[INFO] Next cursor: "+mB.getTarget().getHistory().getPageInfo().getEndCursor()+
                                " :: Remaining API: "+repoData.getRateLimit().getRemaining());

                        // Recurse with next cursor
                        endCursor = mB.getTarget().getHistory().getPageInfo().getEndCursor();
                        hasNextPage = mB.getTarget().getHistory().getPageInfo().getHasNextPage();
                    }
                }
            }
        }

        // Shut down threads
        executor.shutdown();

        // Wait for all threads to terminate
        while (!executor.isTerminated()) {
        }

        System.out.println("[INFO] Finished getting all commits!");
    }

     private void getHistory(Data repoData, String endCursor){

    }


}
