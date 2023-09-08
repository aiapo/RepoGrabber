package com.troxal.manipulation;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.diff.MoveSourceFolderRefactoring;
import gr.uom.java.xmi.diff.UMLModelDiff;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;

import static org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl.*;
import static org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl.createModel;

public class Refactorings implements Callable<Refactorings>, HazelcastInstanceAware {
    private GitService gitService;
    private String commitId,gitURI;
    private List<Refactoring> refactorings;
    private Repository repository;
    private RefactoringHandler handler;
    private RevCommit currentCommit;
    private transient HazelcastInstance hazelcastInstance;

    public Refactorings(GitService gitService, Repository repository, RefactoringHandler handler, String gitURI,
                        RevCommit currentCommit){
        this.gitService = gitService;
        this.repository = repository;
        this.handler = handler;
        this.currentCommit = currentCommit;
        this.gitURI = gitURI;
    }

    public Refactorings (List<Refactoring> refactorings,String commitId,String gitURI){
        this.refactorings = refactorings;
        this.commitId = commitId;
        this.gitURI = gitURI;
    }

    @Override
    public Refactorings call() throws Exception {
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
        return new Refactorings(refactoringsAtRevision,commitId,gitURI);
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    public List<Refactoring> getRefactorings() {
        return refactorings;
    }
    public String getCommitId() {
        return commitId;
    }
    public String getGitURI() {
        return gitURI;
    }
}
