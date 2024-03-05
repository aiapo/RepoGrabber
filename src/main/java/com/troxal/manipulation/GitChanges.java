package com.troxal.manipulation;

import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.troxal.RepoGrab;
import com.troxal.database.Database;
import com.troxal.database.Manager;
import com.troxal.pojo.MoveAttributeInfo;
import com.troxal.pojo.RepoInfo;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class GitChanges {
    ExecutorService dbExecutor = Executors.newFixedThreadPool(30);
    ExecutorService reposExecutor = Executors.newFixedThreadPool(12);
    ExecutorService commitExecutor = Executors.newFixedThreadPool(50);
    private Database db;
    private Integer count = 0;
    private Integer totalRepos = 0;

    public GitChanges(RepoGrab repos){
        db=new Manager().access();
        totalRepos = repos.getRepos().size();

        for(RepoInfo r : repos.getRepos()){
            reposExecutor.submit(() -> {
                classStatistics(r);
            });
        }

        // Shut down threads
        reposExecutor.shutdown();
        // Wait for all threads to terminate
        while (!reposExecutor.isTerminated()) {
        }

        // Shut down threads
        dbExecutor.shutdown();
        // Wait for all threads to terminate
        while (!dbExecutor.isTerminated()) {
        }

        // Shut down threads
        commitExecutor.shutdown();
        // Wait for all threads to terminate
        while (!commitExecutor.isTerminated()) {
        }

        db.close();
        System.out.println("[INFO] Finished!");
    }

    private void classStatistics(RepoInfo r) {
        List<Future> commitRuns = new ArrayList<>();
        count++;
        System.out.println("[DEBUG] ["+count+"/"+totalRepos+"] Start '"+r.getName()+"' analysis task!");
        // directory unique
        String dir = "repos/" + r.getName() + "_" + r.getId();
        try {
            ResultSet repoA = db.select("info", new String[]{"refactorcommit"}, "repositoryid = ?",
                    new Object[]{r.getId()});

            if (!repoA.next()) {
                try{
                    // open the git repo
                    Git git = Git.cloneRepository().
                            setNoCheckout(true).setURI(r.getUrl()+".git").setDirectory(new File(dir)).call();
                    Repository repo = git.getRepository();
                    System.out.println("[DEBUG] Opened repo '"+r.getName()+"' via git!");

                    List<Object[]> statsList = new ArrayList<>();

                    // select all the commits for (in this case) move attribute refactoring for a given repo on the *left* side
                    ResultSet leftSide = db.select("Refactorings",new String[]{"*"},"refactoringname = ? AND " +
                            "repositoryid = ? AND refactoringside = ?", new Object[]{"Move Attribute",r.getId(),"left"});
                    System.out.println("[DEBUG] Got all left sides for repo '"+r.getName()+"'!");

                    // for each *left* commit
                    while(leftSide.next()){
                        // left side specifics
                        String leftFilePath = leftSide.getString("filepath");
                        String leftCodeElement = leftSide.getString("codeelement");
                        String leftCodeElementType = leftSide.getString("codeelementtype");
                        String leftField = leftCodeElement.split("\\s+")[1];
                        String leftDescription = leftSide.getString("description");
                        Integer leftFieldStartLine = leftSide.getInt("startline");
                        Integer leftFieldEndLine = leftSide.getInt("endline");
                        Integer leftFieldStartColumn = leftSide.getInt("startcolumn");
                        Integer leftFieldEndColumn = leftSide.getInt("endcolumn");

                        // commit stats
                        String hashID = leftSide.getString("commit");
                        String refactoringHash = leftSide.getString("refactoringhash");
                        String description = leftSide.getString("commitmessage");
                        String refactoringName = leftSide.getString("refactoringname");
                        String commitAuthor = leftSide.getString("commitAuthor");
                        Date commitDate = leftSide.getDate("commitDate");

                        System.out.println("[DEBUG] Got left side DB for hash "+hashID+" for repo '"+r.getName()+"'!");

                        commitRuns.add(commitExecutor.submit(() -> {
                            System.out.println("[DEBUG] Start commit analysis on "+hashID+" for repo '"+r.getName()+"'!");
                            try {
                                // get what was refactored
                                RevCommit newCommit;
                                MoveAttributeInfo left;
                                MoveAttributeInfo right;

                                // intent is that the committer wrote in their message that it's a refactoring
                                Boolean dIntent = description.contains("refactor");

                                // get right side
                                ResultSet rightSide = db.select("Refactorings", new String[]{"*"}, "refactoringhash = ?" +
                                        " AND refactoringside = ?", new Object[]{refactoringHash, "right"});

                                System.out.println("[DEBUG] Got right side DB for hash " + hashID + " for repo '" + r.getName() + "'!");

                                // right side specifics
                                if (rightSide.next()) {
                                    String rightFilePath = rightSide.getString("filepath");
                                    String rightCodeElement = rightSide.getString("codeelement");
                                    String rightCodeElementType = rightSide.getString("codeelementtype");
                                    String rightDescription = rightSide.getString("description");
                                    String rightField = rightCodeElement.split("\\s+")[1];
                                    Integer rightFieldStartLine = rightSide.getInt("startline");
                                    Integer rightFieldEndLine = rightSide.getInt("endline");
                                    Integer rightFieldStartColumn = rightSide.getInt("startcolumn");
                                    Integer rightFieldEndColumn = rightSide.getInt("endcolumn");

                                    // try to get the given commit (in this case the refactored/right side)
                                    try (RevWalk walk = new RevWalk(repo)) {
                                        newCommit = walk.parseCommit(repo.resolve(hashID));

                                        //Get commit that is previous to the current one (in this case the original/left side)
                                        try (RevWalk walkB = new RevWalk(repo)) {
                                            // Starting point
                                            walkB.markStart(newCommit);
                                            int count = 0;
                                            for (RevCommit rev : walkB) {
                                                // got the previous commit.
                                                if (count == 1) {
                                                    if(rev!=null){
                                                        System.out.println("[DEBUG] Got left side of repo for hash "
                                                                + hashID + " for repo '" + r.getName() + "'!");
                                                        left = getCStat(repo, rev, leftFilePath, leftField,
                                                                leftFieldStartLine, false);
                                                        rev.disposeBody();

                                                        // current commit (refactored)
                                                        System.out.println("[DEBUG] Got right side of repo for hash "
                                                                + hashID + " for repo '" + r.getName() + "'!");
                                                        right = getCStat(repo, newCommit, rightFilePath, rightField,
                                                                rightFieldStartLine, true);
                                                        newCommit.disposeBody();

                                                        if (!Objects.equals(left.getPackageName(), "") &&
                                                                !Objects.equals(right.getPackageName(), "")) {
                                                            statsList.add(new Object[]{
                                                                    refactoringHash,
                                                                    hashID,
                                                                    r.getUrl() + ".git",
                                                                    r.getId(),
                                                                    refactoringName,
                                                                    commitAuthor,
                                                                    description,
                                                                    commitDate,
                                                                    dIntent,
                                                                    rev.getName(),
                                                                    leftFieldStartLine,
                                                                    leftFieldEndLine,
                                                                    leftFieldStartColumn,
                                                                    leftFieldEndColumn,
                                                                    leftFilePath,
                                                                    leftDescription,
                                                                    leftCodeElementType,
                                                                    leftCodeElement,
                                                                    left.getPackageName(),
                                                                    left.getClassI().getName(),
                                                                    left.getClassI().getFieldCount(),
                                                                    left.getClassI().getMethodCount(),
                                                                    left.getClassI().getAccess(),
                                                                    left.getClassI().getIsAbstract(),
                                                                    left.getClassI().getIsStatic(),
                                                                    left.getClassI().getIsInnerClass(),
                                                                    left.getClassI().getStartLine(),
                                                                    left.getClassI().getEndLine(),
                                                                    left.getFieldI().getName(),
                                                                    left.getFieldI().getAccess(),
                                                                    left.getFieldI().getIsAbstract(),
                                                                    left.getFieldI().getIsStatic(),
                                                                    left.getFieldI().getIsFinal(),
                                                                    left.getFieldI().getStartLine(),
                                                                    left.getFieldI().getEndLine(),
                                                                    newCommit.getName(),
                                                                    rightFieldStartLine,
                                                                    rightFieldEndLine,
                                                                    rightFieldStartColumn,
                                                                    rightFieldEndColumn,
                                                                    rightFilePath,
                                                                    rightDescription,
                                                                    rightCodeElementType,
                                                                    rightCodeElement,
                                                                    right.getPackageName(),
                                                                    right.getClassI().getName(),
                                                                    right.getClassI().getFieldCount(),
                                                                    right.getClassI().getMethodCount(),
                                                                    right.getClassI().getAccess(),
                                                                    right.getClassI().getIsAbstract(),
                                                                    right.getClassI().getIsStatic(),
                                                                    right.getClassI().getIsInnerClass(),
                                                                    right.getClassI().getStartLine(),
                                                                    right.getClassI().getEndLine(),
                                                                    right.getFieldI().getName(),
                                                                    right.getFieldI().getAccess(),
                                                                    right.getFieldI().getIsAbstract(),
                                                                    right.getFieldI().getIsStatic(),
                                                                    right.getFieldI().getIsFinal(),
                                                                    right.getFieldI().getStartLine(),
                                                                    right.getFieldI().getEndLine()
                                                            });
                                                        }
                                                    }
                                                    break;
                                                }
                                                count++;
                                            }
                                            walkB.dispose();
                                        }
                                    } catch (AmbiguousObjectException e) {
                                        System.out.println("[ERROR] AmbiguousObjectException: " + e);
                                    } catch (IncorrectObjectTypeException e) {
                                        System.out.println("[ERROR] IncorrectObjectTypeException: " + e);
                                    } catch (MissingObjectException e) {
                                        System.out.println("[ERROR] MissingObjectException: " + e);
                                    } catch (IOException e) {
                                        System.out.println("[ERROR] IOException: " + e);
                                    }
                                }

                                // close *this* right side
                                rightSide.close();

                                System.out.println("[DEBUG] Finished commit analysis on "+hashID+" for repo '"+r.getName()+"'!");
                            }catch(SQLException e){
                                System.out.println("[ERROR] SQLException: "+e);
                            }
                        }));
                    }

                    // Wait for all commits for this repository to be done before we close the repo
                    for(Future fut : commitRuns){
                        try {
                            Object commit = fut.get();
                            if (commit!=null){
                                System.out.println("[ERROR] Error with thread, no return! (classStatistics [GitChanges.java])");
                            }
                        }catch (Exception e) {
                            System.out.println("[ERROR] Thread Future: "+e+" (classStatistics [GitChanges.java])");
                        }
                    }

                    // close the git repo
                    git.close();
                    commitRuns.clear();
                    System.out.println("[DEBUG] Closed repo '"+r.getName()+"' via git!");

                    // and delete the repo
                    try {
                        FileUtils.deleteDirectory(new File(dir));
                        System.out.println("[DEBUG] Deleted repo '"+r.getName()+"' local directory!");
                    } catch (IOException e) {
                        System.out.println("[ERROR] " + e);
                    }

                    // close this repo's left resultset
                    leftSide.close();

                    // add the stats to the database
                    dbExecutor.submit(() -> {
                        Database dba=new Manager().access();
                        System.out.println("[INFO] Start repository stats to db: "+r.getName()+" with "+statsList.size()+" " +
                                "entries.");
                        dba.insert("info", statsList, new Object[]{"refactorHash", "refactorCommit", "repositoryId",
                                "refactoringName"});
                        dba.close();
                        statsList.clear();
                        System.out.println("[INFO] Finished repository stats to db: "+r.getName());
                    });
                } catch (GitAPIException e) {
                    System.out.println("[ERROR] GitAPIException: "+e);
                }
            }
            repoA.close();
        }catch (SQLException e) {
            System.out.println("[ERROR] SQLException: "+e);
        }
        System.out.println("[DEBUG] ["+count+"/"+totalRepos+"] End '"+r.getName()+"' analysis task!");
    }

    private MoveAttributeInfo getCStat(Repository repo,RevCommit commit,String filePath,String field,
                                       Integer startLine,Boolean refactored){
        // get the java file and use javaparser to parse it
        String javaFile = getFileAtCommit(repo,commit,filePath);
        CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);

        // go through all the classes in a java file continue only if we have a match with our looked for field
        Optional<ClassOrInterfaceDeclaration> result = compilationUnit
                // find all classes
                .findAll(ClassOrInterfaceDeclaration.class)
                .stream()
                // filter to only get classes that have the field at its specific location
                .filter(f -> f.getFieldByName(field).isPresent()
                        && f.getFieldByName(field).get().getRange().get().begin.line==startLine)
                // just get one (theoretically there should only be one anyway)
                .findFirst();

        // just a double check if we have a match
        if(result.isPresent()){
            // we know we got it so GET it
            ClassOrInterfaceDeclaration f = result.get();

            // get the field
            FieldDeclaration fieldI = f.getFieldByName(field).get();

            // get the ranges of the class and field
            Range getRField = fieldI.getRange().get();
            Range getRClass = f.getRange().get();

            // class, field and package info
            AtomicReference<String> packageName = new AtomicReference<>("");
            List<String> outerClasses = new ArrayList<>();
            AtomicReference<String> fieldAccess = new AtomicReference<>("");
            AtomicReference<Boolean> isStaticField = new AtomicReference<>(false);
            AtomicReference<Boolean> isAbstractField = new AtomicReference<>(false);
            AtomicReference<Boolean> isFinalField = new AtomicReference<>(false);

            // set class access type
            String classAccess = "";
            if(f.isPublic()){
                classAccess="public";
            }else if(f.isPrivate()){
                classAccess="private";
            }else if(f.isProtected()){
                classAccess="protected";
            }

            for (ClassOrInterfaceType extendedClass : f.getExtendedTypes()) {
                outerClasses.add(extendedClass.getNameAsString());
            }

            // set the package name
            compilationUnit.getPackageDeclaration().ifPresent(packageDeclaration -> {
                packageName.set(packageDeclaration.getNameAsString());
            });

            // get the extra modifiers for the field
            fieldI.getModifiers().forEach(fieldModifier -> {
                String modifierKeyword = fieldModifier.getKeyword().asString();
                if(Objects.equals(modifierKeyword, "public") ||
                        Objects.equals(modifierKeyword, "private") ||
                        Objects.equals(modifierKeyword, "protected")){
                    fieldAccess.set(modifierKeyword);
                }else if(Objects.equals(modifierKeyword,"static")){
                    isStaticField.set(true);
                }else if(Objects.equals(modifierKeyword,"abstract")){
                    isAbstractField.set(true);
                }else if(Objects.equals(modifierKeyword,"final")){
                    isFinalField.set(true);
                }else{
                    System.out.println(modifierKeyword);
                }
            });

            // return MoveAttributeInfo with all the info
            return new MoveAttributeInfo(refactored,packageName.get(),
                    f.getNameAsString(), classAccess, f.isAbstract(), f.isStatic(), f.isInnerClass(),
                    outerClasses,getRClass.begin.line, getRClass.end.line, f.getFields().size(),
                    f.getMethods().size(),field,fieldAccess.get(), isAbstractField.get(),
                    isStaticField.get(), isFinalField.get(), getRField.begin.line, getRField.end.line);
        }

        // otherwise return just the default MoveAttributeInfo
        return new MoveAttributeInfo();
    }


    private String getFileAtCommit(Repository repo,RevCommit commit,String filePath){
        try {
            // get the tree of the commit
            RevTree tree = commit.getTree();

            // walk it until file path found
            TreeWalk treewalk = TreeWalk.forPath(repo, filePath, tree);

            // if file exists
            if (treewalk != null) {
                // use the blob id to read the file's data
                byte[] data = repo.open(treewalk.getObjectId(0)).getBytes();
                return new String(data, "utf-8");
            }
        } catch (IOException e) {
            System.out.println("[ERROR] IOException: "+e);
        }
        return "";
    }
}
