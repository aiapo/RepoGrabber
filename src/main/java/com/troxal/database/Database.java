package com.troxal.database;

import java.io.File;
import java.sql.*;
public class Database {

    // Create a database connection
    protected Connection connection;

    // Import the Query class (this class will format the queries)
    protected QueryBuilder query;

    protected String databaseFile;

    // Constructor to create connection to specified database
    public Database(String databaseFile){
        this.databaseFile = databaseFile;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
        }catch (SQLException e){
            System.out.println("[ERROR] "+e);
        }
    }

    // Execute the queries
    // This takes the prepared query (ex. DELETE FROM ? WHERE ? = ?)
    // And the params and creates a prepared statement for efficiency
    // Then executes...
    // Special cases: CREATE and SELECT
    private int execute(String query, Object[] params){
        checkLock();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            if (params != null) {
                int index = 1;
                for (Object param : params) {
                    ps.setObject(index, param);
                    index++;
                }
            }
            return ps.executeUpdate();
        }catch (SQLException e){
            System.out.println("[ERROR] "+e);
            return 1;
        }
    }

    // Creates a new table based on an object of attributes
    public Boolean create(String table, Object[] params){
        query = new QueryBuilder();
        query.create(table).attributes(params);

        if(execute(query.stringifyQuery(), null)==0)
            return true;
        else
            return false;
    }

    // Inserts into a table an object of values
    public Boolean insert(String table, Object[] params){
        query = new QueryBuilder();
        query.insert(table).values(params);

        if(execute(query.stringifyQuery(), params)==1)
            return true;
        else
            return false;
    }


    // Updates tuple(s) in a table based on where condition
    public Boolean update(String table, String[] attribute, String condition, Object[] params){
        query = new QueryBuilder();
        query.update(table).set(attribute).where(condition);

        if(execute(query.stringifyQuery(), params)==1)
            return true;
        else
            return false;
    }

    // Overloaded select with no conditions
    public ResultSet select(String table, Object[] columns) throws SQLException{
        return this.select(table, columns, "", null);
    }

    // Select tuple(s) based on condition (specify "MAX") in condition to select the max.
    public ResultSet select(String table, Object[] columns, String condition, Object[] params) throws SQLException {
        checkLock();
        query = new QueryBuilder();
        if (condition.equals(""))
            query.select(columns).from(table);
        else if(condition.equals("MAX"))
            query.max(columns).from(table);
        else
            query.select(columns).from(table).where(condition);

        PreparedStatement ps = connection.prepareStatement(query.stringifyQuery());
        if(params != null){
            int index = 1;
            for(Object param : params){
                ps.setObject(index, param);
                index++;
            }
        }

        ResultSet rs = ps.executeQuery();
        return rs;
    }

    // Deletes from a table based on a where condition
    public Boolean delete(String table, String requirement, Object[] param){
        query = new QueryBuilder();
        query.delete(table).where(requirement);
        if(execute(query.stringifyQuery(), param)==1)
            return true;
        else
            return false;
    }

    public void checkLock(){
        File s1 = new File(databaseFile+"-journal");
        while(s1.exists()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
}
