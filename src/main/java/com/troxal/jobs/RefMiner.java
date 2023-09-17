package com.troxal.jobs;

import com.troxal.RepoGrab;
import com.troxal.database.Database;
import com.troxal.database.Manager;
import com.troxal.manipulation.RefMine;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RefMiner {
    public static void runJobs(RepoGrab repos){
        Database db=new Manager().access();

        final Integer maxProcessors = Runtime.getRuntime().availableProcessors();
        final Integer totalThreadPool = maxProcessors-1;

        ExecutorService executor = Executors.newWorkStealingPool();
        ExecutorService service = Executors.newWorkStealingPool();

        for(int j=0;j<repos.getRepos().size();j++){
            Boolean ignoreRepo = false;
            try {
                ResultSet rStatus = db.select("RepositoryStatus",new String[]{"status"},"id = ?",
                        new Object[]{repos.getRepo(j).getId()});
                if(rStatus.next()){
                    if(rStatus.getInt("status")==1)
                        ignoreRepo = true;
                }
            } catch (SQLException e) {
                System.out.println("[ERROR] "+e);
            }

            if(!ignoreRepo) {
                executor.execute(new RefMine(repos.getRepo(j), false, service));
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
