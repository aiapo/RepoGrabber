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

public class RefMiner {
    public static void runJobs(RepoGrab repos){
        Database db=new Manager().access();

        ExecutorService executor = Executors.newWorkStealingPool();
        ExecutorService service = Executors.newWorkStealingPool();

        List<RepoInfo> holdPool = new ArrayList<>();

        for(int j=0;j<repos.getRepos().size();j++){
            runJob(db,executor,service,repos,holdPool,j);
        }

        for(int j=0;j<holdPool.size();j++){
            runJob(db,executor,service,repos,holdPool,j);
        }

        db.close();

        // Shut down threads
        executor.shutdown();

        while (!executor.isTerminated()) {
        }

        System.out.println("[INFO] Finished processing all repos!");
    }

    private static void runJob(Database db, ExecutorService executor, ExecutorService service, RepoGrab repos,
                               List<RepoInfo> holdPool, Integer j){
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
                executor.execute(new RefMine(repos.getRepo(j), false, service));
            }
            rStatus.close();
        } catch (SQLException e) {
            System.out.println("[ERROR] "+e);
        }
    }
}
