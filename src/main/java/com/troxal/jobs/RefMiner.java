package com.troxal.jobs;

import com.troxal.RepoGrab;
import com.troxal.database.Database;
import com.troxal.database.Manager;
import com.troxal.manipulation.RefMine;
import com.troxal.pojo.RepoInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RefMiner {
    public static void runJobs(RepoGrab repos){
        Database db=new Manager().access();

        ExecutorService executor = Executors.newWorkStealingPool();
        ExecutorService service = Executors.newWorkStealingPool();

        List<RepoInfo> holdPool = new ArrayList<>();
        List<Future> repoRuns = new ArrayList<>();

        for(int j=0;j<repos.getRepos().size();j++){
            Boolean ignoreRepo = false;
            Integer repoStatus;
            try {
                ResultSet rStatus = db.select("RepositoryStatus",new String[]{"status"},"id = ?",
                        new Object[]{repos.getRepo(j).getId()});
                if(rStatus.next()) {
                    repoStatus = rStatus.getInt("status");
                    if (repoStatus == 1){
                        System.out.println("[INFO] Ignoring repo: " + repos.getRepo(j).getName() + " because it's " +
                                "already been processed.");
                    }else if(repoStatus==2){
                        holdPool.add(repos.getRepo(j));
                        System.out.println("[INFO] Holding back repo to do later: "+repos.getRepo(j).getName()+
                                " because it's potentially being processed.");
                    }
                }else{
                    repoRuns.add(executor.submit(new RefMine(repos.getRepo(j), false, service)));
                }
                rStatus.close();
            } catch (SQLException e) {
                System.out.println("[ERROR] SQL Exception: "+e+" (runJobs [RefMiner.java])");
            }
        }

        // Wait for the initial repos to be done, so that we don't step on other workers toes
        for(Future<Boolean> fut : repoRuns){
            try {
                Boolean commit = fut.get();
                if (!commit){
                    System.out.println("[ERROR] Error with thread, no return!"+" (runJobs [RefMiner.java])");
                }
            }catch (Exception e) {
                System.out.println("[ERROR] Thread Future: " + e+" (runJobs [RefMiner.java])");
            }
        }

        System.out.println("[INFO] Done with initial, moving to holding pool!");

        for(int j=0;j<holdPool.size();j++){
            Boolean ignoreRepo = false;
            try {
                ResultSet rStatus = db.select("RepositoryStatus",new String[]{"status"},"id = ?",
                        new Object[]{repos.getRepo(j).getId()});
                if(rStatus.next()&&rStatus.getInt("status")==1) {
                        System.out.println("[INFO] Ignoring repo: " + repos.getRepo(j).getName() + " because it's " +
                                "already been processed.");
                }else{
                    executor.execute(new RefMine(repos.getRepo(j), false, service));
                }
                rStatus.close();
            } catch (SQLException e) {
                System.out.println("[ERROR] SQL Exception: "+e+" (runJobs [RefMiner.java])");
            }
        }

        db.close();

        // Shut down threads
        executor.shutdown();

        while (!executor.isTerminated()) {
        }

        System.out.println("[INFO] Finished processing all repos!");
    }
}
