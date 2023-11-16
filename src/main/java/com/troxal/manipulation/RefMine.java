package com.troxal.manipulation;

import com.troxal.database.Database;
import com.troxal.database.Manager;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class RefMine implements Runnable, Serializable {
    private RepoInfo repo;
    private String branchName = null;
    private ExecutorService service = null;
    private Integer totalCommits = 0;
    List<Future> commitRuns = new ArrayList<>();

    public RefMine(RepoInfo repo, Boolean runAllBranches, ExecutorService service){
        this.repo=repo;
        this.service=service;
        if(!runAllBranches)
            branchName = repo.getBranchName();
    }

    @Override
    public void run() {
        System.out.println("[INFO] ** Running RefMiner on "+repo.getName());
        String dir = "repos/"+repo.getName()+"_"+repo.getId();

        if(runRef(repo,dir,branchName)){
            System.out.println("[INFO] ** RefMiner success on "+dir);
        }else
            System.out.println("[ERROR] ** RefMiner failed on "+dir+" (run [RefMine.java])");

        //Runtime.getRuntime().gc();
    }

    private Boolean runRef(RepoInfo r, String dir, String branchName){
        Database db=new Manager().access();

        if(db.insert("RepositoryStatus",new Object[]{r.getId(),2}))
            System.out.println("[INFO] Updated repository status to in-progress: "+r.getId());
        else
            System.out.println("[ERROR] Failed to update repository status to in-progress: "+r.getId()+" (runRef [RefMine" +
                    ".java])");

        GitService gitService = new GitServiceImpl();
        try{
            Repository repo = gitService.cloneIfNotExists(dir,r.getUrl()+".git");

            // Run RefMiner
            detectAll(db,r.getId(), r.getUrl()+".git", repo, branchName, new RefactoringHandler() {
                @Override
                public void handleException(String commit, Exception e) {
                    System.out.println("[ERROR] Error processing commit " + commit+" (runRef [RefMine.java])");
                }
            });

            repo.close();
            // idea is to delete the clone if we don't need it anymore
            try {
                FileUtils.deleteDirectory(new File(dir));
            }catch (IOException e){
                System.out.println("[ERROR] "+e);
            }

            if(db.update("RepositoryStatus", new String[]{"status"},"id = ?",new Object[]{1,r.getId()}))
                System.out.println("[INFO] Updated repository status to completed: "+r.getId());
            else
                System.out.println("[ERROR] Failed to update repository status to completed: "+r.getId()+" (runRef [RefMine" +
                        ".java])");
            db.close();

            return true;
        }catch (Exception e){
            System.out.println("[ERROR] Exception: "+e+" (runRef [RefMine.java])");
            return false;
        }
    }

    private void detect(String id, String gitURI, GitService gitService, Repository repository,
                        final RefactoringHandler handler, Iterator<RevCommit> i, Database db) {
        File metadataFolder = repository.getDirectory();
        File projectFolder = metadataFolder.getParentFile();
        String projectName = projectFolder.getName();

        while (i.hasNext()) {
            totalCommits++;
            RevCommit currentCommit = i.next();
            Integer commitStatus = 0;
            try {
                ResultSet cStatus = db.select("CommitStatus",new String[]{"status"},"id = ?",
                        new Object[]{currentCommit.getId().getName()});
                if(cStatus.next()){
                    commitStatus = cStatus.getInt("status");
                    if(commitStatus==1){
                        //System.out.println("[INFO] Ignoring commit: " + currentCommit.getId().getName() +
                        //        " because it's already been processed.");
                    }else{
                        System.out.print("[DEBUG] Unknown status encountered for commit!"+" (detect [RefMine.java])");
                    }
                }else{
                    try {
                        commitRuns.add(service.submit(new Refactorings(id, gitURI, gitService, repository, handler,
                                currentCommit,db)));
                    } catch (Exception e) {
                        System.out.println(String.format("[ERROR] Ignored revision %s due to error %s", currentCommit.getId().getName(),e));
                        handler.handleException(currentCommit.getId().getName(),e);
                    }
                }
                cStatus.close();

            } catch (SQLException e) {
                System.out.println("[ERROR] SQL Exception: "+e+" (detect [RefMine.java])");
            }
        }

        for(Future<Boolean> fut : commitRuns){
            try {
                Boolean commit = fut.get();
                if (!commit){
                    System.out.println("[ERROR] Error with thread, no return!"+" (detect [RefMine.java])");
                }
            }catch (Exception e) {
                System.out.println("[ERROR] Thread Future: " + e+" (detect [RefMine.java])");
            }
        }
        System.out.println("\nFinished all sub-threads for repo "+projectName);
    }

    public void detectAll(Database db,String id, String gitURI, Repository repository, String branch,
                          final RefactoringHandler handler) throws Exception {
        GitService gitService = new GitServiceImpl() {
            @Override
            public boolean isCommitAnalyzed(String sha1) {
                return handler.skipCommit(sha1);
            }
        };
        RevWalk walk = gitService.createAllRevsWalk(repository, branch);
        try {
            detect(id,gitURI,gitService, repository, handler, walk.iterator(),db);
        } finally {
            walk.dispose();
        }
        walk.close();
    }
}
