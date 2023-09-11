package com.troxal.manipulation;

import com.troxal.database.Database;
import com.troxal.pojo.RepoInfo;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.util.GitServiceImpl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class RefMine implements Runnable, Serializable {
    private RepoInfo repo;
    private String branchName = null;
    private static Database db = null;
    private ExecutorService service = null;
    private Integer totalCommits = 0;
    List<Future> commitRuns = new ArrayList<>();

    public RefMine(RepoInfo repo, Boolean runAllBranches, Database db, ExecutorService service,
                   List<Future> commitRuns){
        this.repo=repo;
        this.db=db;
        this.service=service;
        this.commitRuns=commitRuns;
        if(!runAllBranches)
            branchName = repo.getBranchName();
    }

    @Override
    public void run() {
        System.out.println("[INFO] ** Running RefMiner on "+repo.getName());
        String dir = "repos/"+repo.getName()+"_"+repo.getId();

        if(runRef(repo.getUrl()+".git",dir,branchName)){
            System.out.println("[INFO] ** RefMiner success on "+dir);
        }else
            System.out.println("[ERROR] ** RefMiner failed on "+dir);

        Runtime.getRuntime().gc();
    }

    private Boolean runRef(String gitURI, String dir, String branchName){
        GitService gitService = new GitServiceImpl();
        try{
            Repository repo = gitService.cloneIfNotExists(dir,gitURI);

            Object[] newRepo = {null,dir};
            if(db.insert("Repositories",newRepo))
                System.out.println("[INFO] Added repo: "+dir);
            else
                System.out.println("[ERROR] Failed to add repo: "+dir);

            // Run RefMiner
            detectAll(repo, branchName, new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                    Object[] newCommit = {null,commitId,gitURI,dir};
                    if(db.insert("Commits",newCommit))
                        System.out.println("[INFO] Added commit: "+commitId);
                    else
                        System.out.println("[ERROR] Failed to add commit: "+commitId);

                    for(Refactoring refactoring : refactorings) {
                        Object[] newRefactoring = {null,commitId,refactoring.toJSON()};
                        if(db.insert("Refactorings",newRefactoring))
                            System.out.println("[INFO] Added refactoring: "+refactoring.getName());
                        else
                            System.out.println("[ERROR] Failed to add refactoring: "+refactoring.getName());
                    }
                    refactorings.clear();
                }

                @Override
                public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
                    System.out.println("[DEBUG] onFinish");
                }
                @Override
                public void handleException(String commit, Exception e) {
                    System.out.println("[ERROR] Error processing commit " + commit);
                }
            });

            repo.close();
            // idea is to delete the clone if we don't need it anymore
            try {
                FileUtils.deleteDirectory(new File(dir));
            }catch (IOException e){
                System.out.println("[ERROR] "+e);
            }

            return true;
        }catch (Exception e){
            System.out.println("[ERROR] Exception: "+e);
            return false;
        }
    }

    private void detect(GitService gitService, Repository repository, final RefactoringHandler handler,
                            Iterator<RevCommit> i) {
        File metadataFolder = repository.getDirectory();
        File projectFolder = metadataFolder.getParentFile();
        String projectName = projectFolder.getName();

        while (i.hasNext()) {
            totalCommits++;
            RevCommit currentCommit = i.next();
            try {
                commitRuns.add(service.submit(new Refactorings(gitService, repository, handler, currentCommit)));
            } catch (Exception e) {
                System.out.println(String.format("[ERROR] Ignored revision %s due to error %s", currentCommit.getId().getName(),e));
                handler.handleException(currentCommit.getId().getName(),e);
            }
        }

        for(Future<Boolean> fut : commitRuns){
            try {
                Boolean commit = fut.get();
                if (commit)
                    System.out.println("[DEBUG] Done thread");
            }catch (Exception e) {
                System.out.println("[ERROR] " + e);
            }
        }

        System.out.println("\nFinished all sub-threads for repo "+projectName);
    }

    public void detectAll(Repository repository, String branch, final RefactoringHandler handler) throws Exception {
        GitService gitService = new GitServiceImpl() {
            @Override
            public boolean isCommitAnalyzed(String sha1) {
                return handler.skipCommit(sha1);
            }
        };
        RevWalk walk = gitService.createAllRevsWalk(repository, branch);
        try {
            detect(gitService, repository, handler, walk.iterator());
        } finally {
            walk.dispose();
        }
        walk.close();
    }
}
