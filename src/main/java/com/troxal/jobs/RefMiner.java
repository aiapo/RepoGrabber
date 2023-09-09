package com.troxal.jobs;

import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.durableexecutor.DurableExecutorService;
import com.troxal.RepoGrab;
import com.troxal.manipulation.RefMine;
import com.troxal.manipulation.Refactorings;
import com.troxal.pojo.RepoInfo;
import org.apache.commons.io.FileUtils;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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

        List<Future> refactoring = new ArrayList<>();

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
            refactoring.add(executor.submit(new RefMine(repos.getRepo(j),false)));
        }


        for(Future<RefMine> fut : refactoring){
            try {
                RefMine repoRefactor = fut.get();
                System.out.println("[INFO]["+new Date()+"] Finished processing "+repoRefactor.getDir());
                Path path = Paths.get("results/"+repoRefactor.getDir()+".json");

                // JSON creation/deletion
                try {
                    Files.createDirectories(path.getParent());
                    if(Files.exists(path)) {
                        Files.delete(path);
                    }
                    if(Files.notExists(path)) {
                        Files.createFile(path);
                    }
                } catch (IOException e) {
                    System.out.println("[ERROR] Error creating dir: "+e);
                }

                StringBuilder sb = new StringBuilder();

                // Start JSON
                sb.append("{").append("\n");
                sb.append("\"").append("commits").append("\"").append(": ");
                sb.append("[").append("\n");
                try {
                    Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    System.out.println("[ERROR] Error writing: "+e);
                }

                Integer commitCount = 0;
                for(Refactorings refactor : repoRefactor.getAllRefactorings()){
                    sb = new StringBuilder();

                    int counter = 0;

                    if(commitCount > 0) {
                        sb.append(",").append("\n");
                    }

                    sb.append("{").append("\n");
                    sb.append("\t").append("\"").append("repository").append("\"").append(": ").append("\"").append(refactor.getGitURI()).append("\"").append(",").append("\n");
                    sb.append("\t").append("\"").append("sha1").append("\"").append(": ").append("\"").append(refactor.getCommitId()).append("\"").append(",").append("\n");
                    String url = GitHistoryRefactoringMinerImpl.extractCommitURL(refactor.getGitURI(), refactor.getCommitId());
                    sb.append("\t").append("\"").append("url").append("\"").append(": ").append("\"").append(url).append(
                            "\"").append(",").append("\n");
                    sb.append("\t").append("\"").append("refactorings").append("\"").append(": ");
                    sb.append("[");

                    for(Refactoring rr : refactor.getRefactorings()){
                        sb.append(rr.toJSON());
                        if(counter < refactor.getRefactorings().size()-1) {
                            sb.append(",");
                        }
                        sb.append("\n");
                        counter++;
                    }

                    sb.append("]").append("\n");
                    sb.append("}");

                    try {
                        Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        System.out.println("[ERROR] Error writing: "+e);
                    }

                    commitCount++;
                }

                // End JSON
                sb = new StringBuilder();
                sb.append("]").append("\n");
                sb.append("}");
                try {
                    Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    System.out.println("[ERROR] Error writing: "+e);
                }

                try {
                    FileUtils.deleteDirectory(new File(repoRefactor.getDir()));
                }catch (IOException e){
                    System.out.println("[ERROR] "+e);
                }

            } catch (InterruptedException | ExecutionException e) {
                System.out.println("[ERROR] "+e);
            }
        }

        // Shut down threads
        executor.shutdown();

        while (!executor.isTerminated()) {
        }

        hazelcastInstance.shutdown();

        System.out.println("\nFinished processing all repos!");
    }


}
