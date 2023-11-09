package com.troxal.database;

import com.troxal.Config;

public class Manager{
    // Create the database/connect to it
    Database DB = new Database(
            Config.get("POSTGRE_SERVER"),
            Config.get("POSTGRE_USER"),
            Config.get("POSTGRE_PASSWORD")
    );

    public Manager(){

    }

    public Manager(Boolean init){
        if(init)
            init();
    }

    public void close(){
        DB.close();
    }

    // Initialize the database if it hasn't been already (create tables)
    public void init(){
            DB.create(
                "Runs", new Object[]{
                        "ID  INTEGER    PRIMARY KEY",
                        "NAME   VARCHAR    NOT NULL",
                        "RUNDATE   VARCHAR    NOT NULL"
                });
            DB.create(
                "Repositories", new Object[]{
                        "ID VARCHAR PRIMARY KEY",
                        "NAME   VARCHAR  NOT NULL",
                        "OWNER  VARCHAR NOT NULL",
                        "URL    VARCHAR NOT NULL",
                        "DESCRIPTION    TEXT    NOT NULL",
                        "PRIMARYLANGUAGE    VARCHAR    NOT NULL",
                        "CREATIONDATE   VARCHAR NOT NULL",
                        "UPDATEDATE VARCHAR NOT NULL",
                        "PUSHDATE VARCHAR NOT NULL",
                        "ISARCHIVED BOOLEAN NOT NULL",
                        "ARCHIVEDAT VARCHAR NOT NULL",
                        "ISFORKED BOOLEAN NOT NULL",
                        "ISEMPTY BOOLEAN NOT NULL",
                        "ISLOCKED BOOLEAN NOT NULL",
                        "ISDISABLED BOOLEAN NOT NULL",
                        "ISTEMPLATE BOOLEAN NOT NULL",
                        "TOTALISSUEUSERS INTEGER NOT NULL",
                        "TOTALMENTIONABLEUSERS INTEGER NOT NULL",
                        "TOTALCOMMITTERCOUNT INTEGER NOT NULL",
                        "TOTALPROJECTSIZE INTEGER NOT NULL",
                        "TOTALCOMMITS INTEGER NOT NULL",
                        "ISSUECOUNT INTEGER NOT NULL",
                        "FORKCOUNT INTEGER NOT NULL",
                        "STARCOUNT INTEGER NOT NULL",
                        "WATCHCOUNT INTEGER NOT NULL",
                        "BRANCHNAME VARCHAR NOT NULL",
                });
            DB.create(
                "RepositoryStatus", new Object[]{
                        "ID  VARCHAR    PRIMARY KEY",
                        "STATUS   INTEGER    NOT NULL"
                });
            DB.create(
                "Languages", new Object[]{
                        "REPOID   VARCHAR    NOT NULL",
                        "NAME   VARCHAR    NOT NULL",
                        "SIZE   INTEGER    NOT NULL",
                        "PRIMARY KEY (REPOID,NAME)"
                });
            DB.create(
                "CommitStatus", new Object[]{
                        "ID  VARCHAR    PRIMARY KEY",
                        "STATUS   INTEGER    NOT NULL"
                });
            DB.create(
                    "Refactorings", new Object[]{
                            "refactoringhash    VARCHAR NOT NULL",
                            "commit    VARCHAR NOT NULL",
                            "gituri  VARCHAR    NOT NULL",
                            "repositoryid  VARCHAR    NOT NULL",
                            "refactoringname   VARCHAR",
                            "refactoringside   VARCHAR",
                            "startLine    INTEGER",
                            "endLine    INTEGER",
                            "startColumn    INTEGER",
                            "endColumn    INTEGER",
                            "filePath    VARCHAR",
                            "CodeElementType    VARCHAR ",
                            "description    TEXT ",
                            "codeElement    VARCHAR",
                            "commitauthor    VARCHAR",
                            "commitmessage    VARCHAR",
                            "commitdate    TIMESTAMP",
                            "PRIMARY KEY (refactoringhash, commit, repositoryid)"
                    });

    }

    public Database access(){
        return DB;
    }
}