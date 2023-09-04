package com.troxal.manipulation;

import com.troxal.pojo.RepoInfo;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class RefMine implements Runnable {
    private RepoInfo repo;
    String branchName = null;

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
        }
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
                e.printStackTrace();
            }

            // Start JSON
            StringBuilder sb = new StringBuilder();
            sb.append("{").append("\n");
            sb.append("\"").append("commits").append("\"").append(": ");
            sb.append("[").append("\n");
            try {
                Files.write(path, sb.toString().getBytes());
            } catch (IOException e) {
                System.out.println(e);
            }

            // Run RefMiner
            miner.detectAll(repo, branchName, new RefactoringHandler() {
                private int commitCount = 0;
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                    if(commitCount > 0) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(",").append("\n");
                        try {
                            Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
                        } catch (IOException e) {
                            e.printStackTrace();
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
                        e.printStackTrace();
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
                    System.err.println("Error processing commit " + commit);
                    e.printStackTrace(System.err);
                }
            });
            sb = new StringBuilder();
            sb.append("]").append("\n");
            sb.append("}");
            try {
                Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;

        }catch (Exception e){
            System.out.println("[ERROR] Exception: "+e);
            return false;
        }
    }
}
