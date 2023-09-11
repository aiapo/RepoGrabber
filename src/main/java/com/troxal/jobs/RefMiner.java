package com.troxal.jobs;

import com.troxal.RepoGrab;
import com.troxal.database.Database;
import com.troxal.database.Manager;
import com.troxal.manipulation.RefMine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RefMiner {
    public static void runJobs(RepoGrab repos){
        final Integer maxProcessors = Runtime.getRuntime().availableProcessors();
        final Integer totalThreadPool = maxProcessors-1;

        ExecutorService executor = Executors.newFixedThreadPool(totalThreadPool);
        ExecutorService service = Executors.newWorkStealingPool();
        Database db = new Manager().access();

        for(int j=0;j<repos.getRepos().size();j++){
            executor.execute(new RefMine(repos.getRepo(j),false,db,service));
        }

        // Shut down threads
        executor.shutdown();

        while (!executor.isTerminated()) {
        }

        System.out.println("[INFO] Finished processing all repos!");
    }
}
