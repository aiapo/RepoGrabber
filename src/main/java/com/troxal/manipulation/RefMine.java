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
    private String allRefactorings;
    private RepoInfo repo;
    private String branchName = null;

    public RefMine(RepoInfo repo, Boolean runAllBranches){
        this.repo=repo;
        if(!runAllBranches)
            branchName = repo.getBranchName();
    }

    public RefMine(String allRefactorings,String dir){
        this.allRefactorings = allRefactorings;
        this.dir = dir;
    }

    @Override
    public RefMine call() {
        System.out.println("** Running RefMiner on "+repo.getName());
        String dir = "repos/"+repo.getName()+"_"+repo.getId();

        StringBuilder sb = new StringBuilder();

        // Start JSON
        sb.append("{").append("\n");
        sb.append("\"").append("commits").append("\"").append(": ");
        sb.append("[").append("\n");

        // Run RefMiner and get inner JSON
        sb.append(runRef(repo.getUrl()+".git",dir,branchName));

        // End JSON
        sb.append("]").append("\n");
        sb.append("}");

       allRefactorings = sb.toString();

        if(allRefactorings!=null){
            System.out.println("** RefMiner successful");
            try {
                FileUtils.deleteDirectory(new File(dir));
            }catch (IOException e){
                System.out.println("[ERROR] "+e);
            }
            return new RefMine(allRefactorings,dir);
        }else
            System.out.println("** RefMiner failed");
            return null;
    }

    private static String runRef(String gitURI,String dir,String branchName){
        GitService gitService = new GitServiceImpl();
        StringBuilder allRefactorings = new StringBuilder();
        try{
            Repository repo = gitService.cloneIfNotExists(dir,gitURI);

            // Run RefMiner
            allRefactorings.append(detectAll(repo, branchName,gitURI, new RefactoringHandler() {
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
            }));
            //System.out.println(allRefactorings.toString());
            return allRefactorings.toString();

        }catch (Exception e){
            System.out.println("[ERROR] Exception: "+e);
            return null;
        }
    }

    private static String detect(GitService gitService, Repository repository, final RefactoringHandler handler,
                                 String gitURI, Iterator<RevCommit> i) {
        final Integer maxProcessors = Runtime.getRuntime().availableProcessors();
        final Integer totalThreadPool = maxProcessors-1;
        ExecutorService executor = Executors.newFixedThreadPool(totalThreadPool);

        List<Future> rf = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

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

        Integer commitCount = 0;
        for(Future<Refactorings> fut : rf){
            try {
                Refactorings refactorings = fut.get();
                System.out.println("[INFO]["+new Date()+"] Finished processing commit "+refactorings.getCommitId());
                int counter = 0;

                if(commitCount > 0) {
                    sb.append(",").append("\n");
                }
                sb.append("{").append("\n");
                sb.append("\t").append("\"").append("repository").append("\"").append(": ").append("\"").append(refactorings.getGitURI()).append("\"").append(",").append("\n");
                sb.append("\t").append("\"").append("sha1").append("\"").append(": ").append("\"").append(refactorings.getCommitId()).append("\"").append(",").append("\n");
                String url = GitHistoryRefactoringMinerImpl.extractCommitURL(refactorings.getGitURI(), refactorings.getCommitId());
                sb.append("\t").append("\"").append("url").append("\"").append(": ").append("\"").append(url).append(
                        "\"").append(",").append("\n");
                sb.append("\t").append("\"").append("refactorings").append("\"").append(": ");
                sb.append("[");

                for(Refactoring rr : refactorings.getRefactorings()){
                    sb.append(rr.toJSON());
                    if(counter < refactorings.getRefactorings().size()-1) {
                        sb.append(",");
                    }
                    sb.append("\n");
                    counter++;
                }

                sb.append("]").append("\n");
                sb.append("}");

                commitCount++;

            } catch (InterruptedException | ExecutionException e) {
                System.out.println("[ERROR] "+e);
            }
        }

        System.out.println(String.format("Analyzed %s [Commits: %d, Errors: %d, Refactorings: %d]", projectName, commitsCount, errorCommitsCount, refactoringsCount));

        // Shut down threads
        executor.shutdown();

        while (!executor.isTerminated()) {

        }

        System.out.println("\nFinished all threads for repo "+projectName);
        return sb.toString();
    }

    public static String detectAll(Repository repository, String branch, String gitURI,
                                   final RefactoringHandler handler) throws Exception {
        String sb;
        GitService gitService = new GitServiceImpl() {
            @Override
            public boolean isCommitAnalyzed(String sha1) {
                return handler.skipCommit(sha1);
            }
        };
        RevWalk walk = gitService.createAllRevsWalk(repository, branch);
        try {
            sb = detect(gitService, repository, handler, gitURI, walk.iterator());
        } finally {
            walk.dispose();
        }
        return sb;
    }

    public String getDir() {
        return dir;
    }
    public String getAllRefactorings(){
        return allRefactorings;
    }
}
