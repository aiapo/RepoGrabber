package com.troxal.manipulation;

import com.troxal.database.Database;
import com.troxal.database.Manager;
import com.troxal.pojo.RepoInfo;
import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.diff.CodeRange;
import gr.uom.java.xmi.diff.MoveSourceFolderRefactoring;
import gr.uom.java.xmi.diff.UMLModelDiff;
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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl.*;

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
                        commitRuns.add(service.submit(() -> {
                                    try{
                                        List<Refactoring> refactoringsAtRevision;
                                        String commitId = currentCommit.getId().getName();
                                        Set<String> filePathsBefore = new LinkedHashSet<String>();
                                        Set<String> filePathsCurrent = new LinkedHashSet<String>();
                                        Map<String, String> renamedFilesHint = new HashMap<>();
                                        gitService.fileTreeDiff(repository, currentCommit, filePathsBefore,
                                                                filePathsCurrent, renamedFilesHint);
                                        Set<String> repositoryDirectoriesBefore = new LinkedHashSet<String>();
                                        Set<String> repositoryDirectoriesCurrent = new LinkedHashSet<String>();
                                        Map<String, String> fileContentsBefore = new LinkedHashMap<String, String>();
                                        Map<String, String> fileContentsCurrent = new LinkedHashMap<String, String>();

                                        if (!filePathsBefore.isEmpty() && !filePathsCurrent.isEmpty()
                                                && currentCommit.getParentCount() > 0) {
                                            RevCommit parentCommit = currentCommit.getParent(0);
                                            populateFileContents(repository, parentCommit, filePathsBefore,
                                                    fileContentsBefore, repositoryDirectoriesBefore);
                                            populateFileContents(repository, currentCommit, filePathsCurrent,
                                                    fileContentsCurrent, repositoryDirectoriesCurrent);
                                            List<MoveSourceFolderRefactoring> moveSourceFolderRefactorings =
                                                    processIdenticalFiles(fileContentsBefore, fileContentsCurrent,
                                                            renamedFilesHint);
                                            UMLModel parentUMLModel = createModel(fileContentsBefore,
                                                    repositoryDirectoriesBefore);
                                            UMLModel currentUMLModel = createModel(fileContentsCurrent,
                                                    repositoryDirectoriesCurrent);

                                            UMLModelDiff modelDiff = parentUMLModel.diff(currentUMLModel);
                                            refactoringsAtRevision = modelDiff.getRefactorings();
                                            refactoringsAtRevision.addAll(moveSourceFolderRefactorings);
                                            moveSourceFolderRefactorings.clear();
                                        } else {
                                            refactoringsAtRevision = Collections.emptyList();
                                        }

                                        List<Object[]> rList = new ArrayList<>();
                                        for(Refactoring refactoring : refactoringsAtRevision) {
                                            System.out.println("[DEBUG] Detected "+id+"'s commit "+commitId+
                                                    " refactored "+refactoring.getName());
                                            String md5 = getMD5(refactoring.toJSON());

                                            if(!refactoring.leftSide().isEmpty()&&!refactoring.rightSide().isEmpty()){
                                                CodeRange ls = refactoring.leftSide().get(0);
                                                CodeRange rs = refactoring.leftSide().get(0);

                                                rList.add(new Object[]{
                                                        md5,
                                                        commitId,
                                                        gitURI,
                                                        id,
                                                        refactoring.getName(),
                                                        ls.getStartLine(),
                                                        ls.getEndLine(),
                                                        ls.getStartColumn(),
                                                        ls.getEndColumn(),
                                                        ls.getFilePath(),
                                                        ls.getCodeElementType().toString(),
                                                        ls.getDescription(),
                                                        ls.getCodeElement(),
                                                        rs.getStartLine(),
                                                        rs.getEndLine(),
                                                        rs.getStartColumn(),
                                                        rs.getEndColumn(),
                                                        rs.getFilePath(),
                                                        rs.getCodeElementType().toString(),
                                                        rs.getDescription(),
                                                        rs.getCodeElement(),
                                                        currentCommit.getAuthorIdent().getName(),
                                                        currentCommit.getFullMessage(),
                                                        LocalDateTime.ofInstant(
                                                                currentCommit.getAuthorIdent().getWhenAsInstant(),
                                                                currentCommit.getAuthorIdent().getTimeZone().toZoneId()
                                                        )
                                                });
                                            }
                                        }

                                        db.insert("Refactorings",rList,new Object[]{
                                                "refactoringhash", "commit", "repositoryid"}
                                        );

                                        if(db.insert("CommitStatus",new Object[]{commitId,1}))
                                            System.out.println("[INFO] Added commit status for repo "+id+
                                                    ": "+commitId);
                                        else
                                            System.out.println("[ERROR] Failed to add commit status: "+id+
                                                    "call (RefMine.java)");

                                        // garbage collection
                                        refactoringsAtRevision.clear();
                                        filePathsBefore.clear();
                                        fileContentsCurrent.clear();
                                        renamedFilesHint.clear();
                                        rList.clear();
                                        repositoryDirectoriesBefore.clear();
                                        repositoryDirectoriesCurrent.clear();
                                        fileContentsBefore.clear();
                                        fileContentsCurrent.clear();

                                        return true;
                                    } catch (Exception e) {
                                        System.out.println("[ERROR] "+id+": " +e+" (call [RefMine.java])");
                                        return false;
                                    }
                                }
                        ));
                    } catch (Exception e) {
                        System.out.println(String.format("[ERROR] Ignored revision %s due to error %s",
                                currentCommit.getId().getName(),e));
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

    public String getMD5(String input)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);

            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
