package com.troxal.manipulation;

import com.troxal.RepoGrab;
import com.troxal.database.Database;
import com.troxal.database.Manager;
import com.troxal.pojo.RepoInfo;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommitHistory {
    public CommitHistory(RepoGrab rg){
        getHistories(rg);
    }

    public void getHistories(RepoGrab repos){
        ExecutorService dbExecutor = Executors.newWorkStealingPool();
        ExecutorService reposExecutor = Executors.newFixedThreadPool(30);

        for(RepoInfo r : repos.getRepos()){
            reposExecutor.submit(() -> {
                String dir = "repos/" + r.getName() + "_" + r.getId();
                try{
                    Git git = Git.cloneRepository().setNoCheckout(true).setURI(r.getUrl()+".git")
                            .setDirectory(new File(dir)).call();
                    Iterable<RevCommit> commits = git.log().all().call();
                    List<Object[]> commitList = new ArrayList<>();
                    for (RevCommit commit : commits) {
                        System.out.println(commit.getName());
                        commitList.add(new Object[]{
                                commit.getName(),
                                r.getId(),
                                commit.getAuthorIdent().getName(),
                                commit.getFullMessage(),
                                LocalDateTime.ofInstant(commit.getAuthorIdent().getWhenAsInstant(), commit.getAuthorIdent().getTimeZone().toZoneId())
                        });
                    }
                    git.close();
                    // idea is to delete the clone if we don't need it anymore
                    try {
                        FileUtils.deleteDirectory(new File(dir));
                    } catch (IOException e) {
                        System.out.println("[ERROR] " + e);
                    }
                    dbExecutor.submit(() -> {
                        Database dba = new Manager().access();
                        dba.insert("CommitI", commitList, new Object[]{"commit", "repo"});
                        dba.close();
                    });
                } catch (Exception e) {
                    System.out.println("[ERROR] " + e);
                }
            });
        }

        // Shut down threads
        reposExecutor.shutdown();

        // Wait for all threads to terminate
        while (!reposExecutor.isTerminated()) {
        }

        // Shut down threads
        dbExecutor.shutdown();

        // Wait for all threads to terminate
        while (!dbExecutor.isTerminated()) {
        }

        System.out.println("[INFO] Finished getting all commits!");
    }
}
