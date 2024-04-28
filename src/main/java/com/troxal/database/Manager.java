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
                        "rid    SERIAL",
                        "ID  INTEGER    PRIMARY KEY",
                        "NAME   VARCHAR    NOT NULL",
                        "RUNDATE   VARCHAR    NOT NULL"
                });
            DB.create(
                "Repositories", new Object[]{
                        "rid    SERIAL",
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
                        "README TEXT",
                        "DOMAIN VARCHAR"
                });
            DB.create(
                "RepositoryStatus", new Object[]{
                        "rid    SERIAL",
                        "ID  VARCHAR    PRIMARY KEY",
                        "STATUS   INTEGER    NOT NULL"
                });
            DB.create(
                "Languages", new Object[]{
                        "rid    SERIAL",
                        "REPOID   VARCHAR    NOT NULL",
                        "NAME   VARCHAR    NOT NULL",
                        "SIZE   INTEGER    NOT NULL",
                        "PRIMARY KEY (REPOID,NAME)"
                });
            DB.create(
                "CommitStatus", new Object[]{
                        "rid    SERIAL",
                        "ID  VARCHAR    PRIMARY KEY",
                        "STATUS   INTEGER    NOT NULL"
                });
            DB.create(
                    "Refactorings", new Object[]{
                            "rid    SERIAL",
                            "refactoringhash    VARCHAR NOT NULL",
                            "commit    VARCHAR NOT NULL",
                            "gituri  VARCHAR    NOT NULL",
                            "repositoryid  VARCHAR    NOT NULL",
                            "refactoringname   VARCHAR",
                            "leftStartLine    INTEGER",
                            "leftEndLine    INTEGER",
                            "leftStartColumn    INTEGER",
                            "leftEndColumn    INTEGER",
                            "leftFilePath    VARCHAR",
                            "leftCodeElementType    VARCHAR ",
                            "leftDescription    TEXT ",
                            "leftCodeElement    VARCHAR",
                            "rightStartLine    INTEGER",
                            "rightEndLine    INTEGER",
                            "rightStartColumn    INTEGER",
                            "rightEndColumn    INTEGER",
                            "rightFilePath    VARCHAR",
                            "rightCodeElementType    VARCHAR ",
                            "rightDescription    TEXT ",
                            "rightCodeElement    VARCHAR",
                            "commitauthor    VARCHAR",
                            "commitmessage    VARCHAR",
                            "commitdate    TIMESTAMP",
                            "PRIMARY KEY (refactoringhash, commit, repositoryid)"
                    });
            DB.create(
                    "Imports", new Object[]{
                            "rid    SERIAL",
                            "RGDSHASH   VARCHAR NOT NULL",
                            "PRIMARY KEY(RGDSHASH)"
                    }
            );

    }

    public Database access(){
        return DB;
    }
}