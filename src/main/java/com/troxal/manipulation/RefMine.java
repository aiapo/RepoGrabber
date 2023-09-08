package com.troxal.manipulation;

import com.troxal.pojo.RepoInfo;
import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.diff.MoveSourceFolderRefactoring;
import gr.uom.java.xmi.diff.UMLModelDiff;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import static org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RefMine implements Runnable {
    private RepoInfo repo;
    private String branchName = null;

    public RefMine(RepoInfo repo, Boolean runAllBranches){
        this.repo=repo;
        if(!runAllBranches)
            branchName = repo.getBranchName();
    }

    @Override
    public void run() {
        System.out.println("** Running RefMiner on "+repo.getName());
        String dir = "repos/"+repo.getName()+"_"+repo.getId();
        if(runRef(repo.getUrl()+".git",dir,branchName)){
            System.out.println("** RefMiner successful");
            try {
                FileUtils.deleteDirectory(new File(dir));
            }catch (IOException e){
                System.out.println("[ERROR] Error deleting: "+e);
            }
        }else
            System.out.println("** RefMiner failed");
    }

    private static Boolean runRef(String gitURI,String dir,String branchName){
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        try{
            Repository repo = gitService.cloneIfNotExists(dir,gitURI);
            Path path = Paths.get("results/"+dir+".json");

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

            // Start JSON
            StringBuilder sb = new StringBuilder();
            sb.append("{").append("\n");
            sb.append("\"").append("commits").append("\"").append(": ");
            sb.append("[").append("\n");
            try {
                Files.write(path, sb.toString().getBytes());
            } catch (IOException e) {
                System.out.println("[ERROR] Error writing: "+e);
            }

            // Run RefMiner
            detectAll(repo, branchName, new RefactoringHandler() {
                private int commitCount = 0;
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                    if(commitCount > 0) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(",").append("\n");
                        try {
                            Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
                        } catch (IOException e) {
                            System.out.println("[ERROR] Error writing: "+e);
                        }
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("{").append("\n");
                    sb.append("\t").append("\"").append("repository").append("\"").append(": ").append("\"").append(gitURI).append("\"").append(",").append("\n");
                    sb.append("\t").append("\"").append("sha1").append("\"").append(": ").append("\"").append(commitId).append("\"").append(",").append("\n");
                    String url = GitHistoryRefactoringMinerImpl.extractCommitURL(gitURI, commitId);
                    sb.append("\t").append("\"").append("url").append("\"").append(": ").append("\"").append(url).append("\"").append(",").append("\n");
                    sb.append("\t").append("\"").append("refactorings").append("\"").append(": ");
                    sb.append("[");
                    int counter = 0;
                    for(Refactoring refactoring : refactorings) {
                        sb.append(refactoring.toJSON());
                        if(counter < refactorings.size()-1) {
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
            sb = new StringBuilder();
            sb.append("]").append("\n");
            sb.append("}");
            try {
                Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.out.println("[ERROR] Error writing: "+e);
            }
            return true;

        }catch (Exception e){
            System.out.println("[ERROR] Exception: "+e);
            return false;
        }
    }

    private static void detect(GitService gitService, Repository repository, final RefactoringHandler handler, Iterator<RevCommit> i) {
        final Integer maxProcessors = Runtime.getRuntime().availableProcessors();
        final Integer totalThreadPool = maxProcessors-1;
        ExecutorService executor = Executors.newFixedThreadPool(totalThreadPool);

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

                Future refactoring = executor.submit(new Refactorings(gitService, repository, handler, currentCommit));
                //List<Refactoring> refactoringsAtRevision = (List<Refactoring>) refactoring.get();

                //refactoringsCount += refactoringsAtRevision.size();

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

        handler.onFinish(refactoringsCount, commitsCount, errorCommitsCount);
        System.out.println(String.format("Analyzed %s [Commits: %d, Errors: %d, Refactorings: %d]", projectName, commitsCount, errorCommitsCount, refactoringsCount));

        // Shut down threads
        executor.shutdown();

        while (!executor.isTerminated()) {

        }

        System.out.println("\nFinished all threads for repo "+projectName);
    }

    public static void detectAll(Repository repository, String branch, final RefactoringHandler handler) throws Exception {
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
    }

}
