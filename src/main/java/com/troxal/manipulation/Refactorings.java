package com.troxal.manipulation;

import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.diff.MoveSourceFolderRefactoring;
import gr.uom.java.xmi.diff.UMLModelDiff;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;

import java.util.*;
import java.util.concurrent.Callable;

import static org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl.*;
import static org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl.createModel;

public class Refactorings implements Runnable {
    private final GitService gitService;
    private Repository repository;
    private final RefactoringHandler handler;
    private RevCommit currentCommit;

    public Refactorings(GitService gitService, Repository repository, RefactoringHandler handler,
                        RevCommit currentCommit){
        this.gitService = gitService;
        this.repository = repository;
        this.handler = handler;
        this.currentCommit = currentCommit;
    }

    @Override
    public void run() {
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
            } else {
                //logger.info(String.format("Ignored revision %s with no changes in java files", commitId));
                refactoringsAtRevision = Collections.emptyList();
            }
            handler.handle(commitId, refactoringsAtRevision);
        } catch (Exception e) {
            System.out.println("[ERROR] "+e);
        }
    }
}