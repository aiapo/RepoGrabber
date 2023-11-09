package com.troxal.manipulation;

import com.troxal.database.Database;
import com.troxal.database.Manager;
import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.diff.CodeRange;
import gr.uom.java.xmi.diff.MoveSourceFolderRefactoring;
import gr.uom.java.xmi.diff.UMLModelDiff;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;

import static org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl.*;
import static org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl.createModel;

public class Refactorings implements Callable {
    private final GitService gitService;
    private String id,gitURI;
    private Repository repository;
    private final RefactoringHandler handler;
    private RevCommit currentCommit;

    public Refactorings(String id, String gitURI, GitService gitService, Repository repository,
                        RefactoringHandler handler, RevCommit currentCommit){
        this.gitService = gitService;
        this.repository = repository;
        this.handler = handler;
        this.currentCommit = currentCommit;
        this.id=id;
        this.gitURI=gitURI;
    }

    @Override
    public Boolean call() {
        try{
            List<Refactoring> refactoringsAtRevision;
            String commitId = currentCommit.getId().getName();
            Set<String> filePathsBefore = new LinkedHashSet<String>();
            Set<String> filePathsCurrent = new LinkedHashSet<String>();
            Map<String, String> renamedFilesHint = new HashMap<>();
            gitService.fileTreeDiff(repository, currentCommit, filePathsBefore, filePathsCurrent, renamedFilesHint);
            Set<String> repositoryDirectoriesBefore = new LinkedHashSet<String>();
            Set<String> repositoryDirectoriesCurrent = new LinkedHashSet<String>();
            Map<String, String> fileContentsBefore = new LinkedHashMap<String, String>();
            Map<String, String> fileContentsCurrent = new LinkedHashMap<String, String>();
            // If no java files changed, there is no refactoring. Also, if there are
            // only ADD's or only REMOVE's there is no refactoring
            if (!filePathsBefore.isEmpty() && !filePathsCurrent.isEmpty() && currentCommit.getParentCount() > 0) {
                RevCommit parentCommit = currentCommit.getParent(0);
                populateFileContents(repository, parentCommit, filePathsBefore, fileContentsBefore, repositoryDirectoriesBefore);
                populateFileContents(repository, currentCommit, filePathsCurrent, fileContentsCurrent, repositoryDirectoriesCurrent);
                List<MoveSourceFolderRefactoring> moveSourceFolderRefactorings = processIdenticalFiles(fileContentsBefore, fileContentsCurrent, renamedFilesHint);
                UMLModel parentUMLModel = createModel(fileContentsBefore, repositoryDirectoriesBefore);
                UMLModel currentUMLModel = createModel(fileContentsCurrent, repositoryDirectoriesCurrent);

                UMLModelDiff modelDiff = parentUMLModel.diff(currentUMLModel);
                refactoringsAtRevision = modelDiff.getRefactorings();
                refactoringsAtRevision.addAll(moveSourceFolderRefactorings);
                moveSourceFolderRefactorings.clear();
            } else {
                //logger.info(String.format("Ignored revision %s with no changes in java files", commitId));
                refactoringsAtRevision = Collections.emptyList();
            }

            Database db=new Manager().access();

            List<Object[]> rList = new ArrayList<>();
            for(Refactoring refactoring : refactoringsAtRevision) {
                System.out.println("[DEBUG] Refactoring name: "+refactoring.getName());
                for(CodeRange a : refactoring.leftSide()){
                    rList.add(new Object[]{
                            getMD5(refactoring.toJSON()),
                            commitId,
                            gitURI,
                            id,
                            refactoring.getName(),
                            "left",
                            a.getStartLine(),
                            a.getEndLine(),
                            a.getStartColumn(),
                            a.getEndColumn(),
                            a.getFilePath(),
                            a.getCodeElementType().toString(),
                            a.getDescription(),
                            a.getCodeElement(),
                            currentCommit.getAuthorIdent().getName(),
                            currentCommit.getFullMessage(),
                            LocalDateTime.ofInstant(currentCommit.getAuthorIdent().getWhenAsInstant(), currentCommit.getAuthorIdent().getTimeZone().toZoneId())});
                }
                for(CodeRange a : refactoring.rightSide()){
                    rList.add(new Object[]{
                            getMD5(refactoring.toJSON()),
                            commitId,
                            gitURI,
                            id,
                            refactoring.getName(),
                            "right",
                            a.getStartLine(),
                            a.getEndLine(),
                            a.getStartColumn(),
                            a.getEndColumn(),
                            a.getFilePath(),
                            a.getCodeElementType().toString(),
                            a.getDescription(),
                            a.getCodeElement(),
                            currentCommit.getAuthorIdent().getName(),
                            currentCommit.getFullMessage(),
                            LocalDateTime.ofInstant(currentCommit.getAuthorIdent().getWhenAsInstant(), currentCommit.getAuthorIdent().getTimeZone().toZoneId())});
                }
            }

            db.insert("Refactorings",rList,new Object[]{"refactoringhash", "commit", "repositoryid","refactoringside"});

            if(db.insert("CommitStatus",new Object[]{commitId,1}))
                System.out.println("[INFO] Added commit status: "+id);
            else
                System.out.println("[ERROR] Failed to add commit status: "+id+" (Refactorings.java)");

            db.close();

            // garbage collection
            refactoringsAtRevision.clear();
            filePathsBefore.clear();
            fileContentsCurrent.clear();
            renamedFilesHint.clear();
            repositoryDirectoriesBefore.clear();
            repositoryDirectoriesCurrent.clear();
            fileContentsBefore.clear();
            fileContentsCurrent.clear();

            return true;
        } catch (Exception e) {
            System.out.println("[ERROR] "+e+" (call [Refactorings.java])");
            return false;
        }
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