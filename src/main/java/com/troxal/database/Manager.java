package com.troxal.database;

import java.io.File;
import java.sql.SQLException;

public class Manager {
    static String databaseName = "results/results.db";
    // Create the database/connect to it
    Database DB = new Database(databaseName);

    public Manager() {
        init();
    }

    // Initialize the database if it hasn't been already (create tables)
    protected void init(){
        if (new File(databaseName).length() <= 0) {
            DB.create(
                    "Refactorings", new Object[]{
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT",
                            "COMMIT_HASH  TEXT    NOT NULL",
                            "JSON   TEXT    NOT NULL"});
            DB.create(
                    "Commits", new Object[]{
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT",
                            "HASH   TEXT  NOT NULL",
                            "GIT    TEXT    NOT NULL",
                            "DIR    TEXT    NOT NULL"});
            DB.create(
                    "Repositories", new Object[]{
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT",
                            "DIR   TEXT  NOT NULL"});
        }
    }

    public Database access(){
        return DB;
    }

}