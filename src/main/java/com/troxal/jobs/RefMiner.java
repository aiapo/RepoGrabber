package com.troxal.jobs;

import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.troxal.RepoGrab;
import com.troxal.manipulation.RefMine;

import java.util.concurrent.TimeUnit;

public class RefMiner {
    public static void runJobs(RepoGrab repos){
        final Integer maxProcessors = Runtime.getRuntime().availableProcessors();
        final Integer totalThreadPool = maxProcessors-1;
        com.hazelcast.config.Config config = new com.hazelcast.config.Config();
        ExecutorConfig executorConfig = config.getExecutorConfig("exec");
        config.setClusterName("exec");
        executorConfig.setPoolSize(totalThreadPool).setQueueCapacity( 10 )
                .setStatisticsEnabled( false )
                .setName("exec")
                .setSplitBrainProtectionName("splitbrainprotectionname");

        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        IExecutorService executor = hazelcastInstance.getExecutorService( "exec" );

        for(int j=0;j<repos.getRepos().size();j++){
            while(executor.getLocalExecutorStats().getStartedTaskCount()>=10) {
                try {
                    System.out.println("[DEBUG] Tasks Active: "+executor.getLocalExecutorStats().getStartedTaskCount());
                    System.out.println("[DEBUG] Tasks Pending: "+executor.getLocalExecutorStats().getPendingTaskCount());
                    System.out.println("[DEBUG] Tasks Completed: "+executor.getLocalExecutorStats().getCompletedTaskCount());
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    System.out.println("[ERROR] Error trying to wait: \n"+e);
                }
            }
            executor.execute(new RefMine(repos.getRepo(j),false));
        }

        // Shut down threads
        executor.shutdown();

        while (!executor.isTerminated()) {
        }

        hazelcastInstance.shutdown();

        System.out.println("\nFinished processing all repos!");
    }


}
