package com.troxal.manipulation;

import com.troxal.pojo.RepoInfo;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class RefMine implements Callable<RefMine>, Serializable {
    private String dir;
    private List<Refactorings> allRefactorings;
    private RepoInfo repo;
    private String branchName = null;

    public RefMine(RepoInfo repo, Boolean runAllBranches){
        this.repo=repo;
        if(!runAllBranches)
            branchName = repo.getBranchName();
    }

    public RefMine(List<Refactorings> allRefactorings,String dir){
        this.allRefactorings = allRefactorings;
        this.dir = dir;
    }

    @Override
    public RefMine call() {
        System.out.println("** Running RefMiner on "+repo.getName());
        String dir = "repos/"+repo.getName()+"_"+repo.getId();

        // Run RefMiner and get inner JSON
        List<Refactorings> allRefactorings = runRef(repo.getUrl()+".git",dir,branchName);

        if(allRefactorings!=null){
            System.out.println("** RefMiner successful");
            return new RefMine(allRefactorings,dir);
        }else
            System.out.println("** RefMiner failed");
            return null;
    }

    private static List<Refactorings> runRef(String gitURI,String dir,String branchName){
        GitService gitService = new GitServiceImpl();
        try{
            Repository repo = gitService.cloneIfNotExists(dir,gitURI);

            // Run RefMiner
            return detectAll(repo, branchName,gitURI, new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                }

                @Override
                public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
                    System.out.println(String.format("Total count: [Commits: %d, Errors: %d, Refactorings: %d]",
                            commitsCount, errorCommitsCount, refactoringsCount));
                }

                @Override
                public void handleException(String commit, Exception e) {
                    System.out.println("[ERROR] Error processing commit " + commit);
                }
            });

        }catch (Exception e){
            System.out.println("[ERROR] Exception: "+e);
            return null;
        }
    }

    private static List<Refactorings> detect(GitService gitService, Repository repository, final RefactoringHandler handler,
                                 String gitURI, Iterator<RevCommit> i) {
        final Integer maxProcessors = Runtime.getRuntime().availableProcessors();
        final Integer totalThreadPool = maxProcessors-1;
        ExecutorService executor = Executors.newFixedThreadPool(totalThreadPool);

        List<Future> rf = new ArrayList<>();

        int commitsCount = 0;
        int errorCommitsCount = 0;
        int refactoringsCount = 0;

        File metadataFolder = repository.getDirectory();
        File projectFolder = metadataFolder.getParentFile();
        String projectName = projectFolder.getName();

        long time = System.currentTimeMillis();
        while (i.hasNext()) {
            RevCommit currentCommit = i.next();
            try {
                rf.add(executor.submit(new Refactorings(gitService, repository, handler, gitURI, currentCommit)));
            } catch (Exception e) {
                System.out.println(String.format("Ignored revision %s due to error %s", currentCommit.getId().getName(),e));
                handler.handleException(currentCommit.getId().getName(),e);
                errorCommitsCount++;
            }

            commitsCount++;
            long time2 = System.currentTimeMillis();
            if ((time2 - time) > 20000) {
                time = time2;
                System.out.println(String.format("Processing %s [Commits: %d, Errors: %d, Refactorings: %d]", projectName, commitsCount, errorCommitsCount, refactoringsCount));
            }
        }

        List<Refactorings> Refactor = new ArrayList<>();
        for(Future<Refactorings> fut : rf){
            try {
                Refactorings refactorings = fut.get();
                System.out.println("[INFO]["+new Date()+"] Finished processing commit "+refactorings.getCommitId());

                Refactor.add(refactorings);
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("[ERROR] "+e);
            }
        }

        // Shut down threads
        executor.shutdown();

        while (!executor.isTerminated()) {
        }

        System.out.println("\nFinished all sub-threads for repo "+projectName);
        return Refactor;
    }

    public static List<Refactorings> detectAll(Repository repository, String branch, String gitURI, final RefactoringHandler handler) throws Exception {
        List<Refactorings> Refactor;
        GitService gitService = new GitServiceImpl() {
            @Override
            public boolean isCommitAnalyzed(String sha1) {
                return handler.skipCommit(sha1);
            }
        };
        RevWalk walk = gitService.createAllRevsWalk(repository, branch);
        try {
            Refactor = detect(gitService, repository, handler, gitURI, walk.iterator());
        } finally {
            walk.dispose();
        }
        return Refactor;
    }

    public String getDir() {
        return dir;
    }
    public List<Refactorings> getAllRefactorings(){
        return allRefactorings;
    }
}
