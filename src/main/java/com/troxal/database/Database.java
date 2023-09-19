package com.troxal.database;

import java.sql.*;
import java.util.List;

public class Database implements AutoCloseable{

    // Create a database connection
    protected Connection connection;

    // Import the Query class (this class will format the queries)
    protected QueryBuilder query;

    // Constructor to create connection to specified database
    public Database(String server,String user,String password){
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://"+server,user,password);
        }catch (SQLException e){
            System.out.println("[ERROR] SQL Exception: "+e+" (Database [Database.java])");
        }
    }
    public void close(){
        try{
            connection.close();
        }catch (SQLException e){
            System.out.println("[ERROR] SQL Exception: "+e+" (close [Database.java])");
        }
    }

    // Execute the queries
    // This takes the prepared query (ex. DELETE FROM ? WHERE ? = ?)
    // And the params and creates a prepared statement for efficiency
    // Then executes...
    // Special cases: CREATE and SELECT
    private int execute(String query, Object[] params){
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
            System.out.println("[ERROR] SQL Exception: "+e+" (execute [Database.java])");
            return 1;
        }
    }

    // Creates a new table based on an object of attributes
    public Boolean create(String table, Object[] params){
        if(!tableExists(table)){
            query = new QueryBuilder();
            query.create(table).attributes(params);

            if(execute(query.stringifyQuery(), null)==0)
                return true;
            else
                return false;
        }else
            return true;
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

    // Inserts into a table an object of values
    public int[] insert(String table, List<Object[]> params){
        if(!params.isEmpty()){
            query = new QueryBuilder();
            query.insert(table).values(params.get(0));

            try {
                PreparedStatement ps = connection.prepareStatement(query.stringifyQuery());

                for(Object[] p : params){
                    if (p != null) {
                        int index = 1;
                        for (Object param : p) {
                            ps.setObject(index, param);
                            index++;
                        }
                        ps.addBatch();

                    }
                }

                return ps.executeBatch();
            } catch (SQLException e) {
                System.out.println("[ERROR] SQL Exception: "+e+" (insert [Database.java])");
            }
        }
        return null;
    }

    // Inserts into a table an object of values based on attributes
    public Boolean insert(String table, Object[] attributes, Object[] params){
        query = new QueryBuilder();
        query.insert(table).attributes(attributes).values(params);

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

    private Boolean tableExists(String table) {
        Boolean tExists = false;
        try (ResultSet rs = connection.getMetaData().getTables(null,null,null,new String[] {"TABLE"})) {
            while (rs.next()) {
                String tName = rs.getString("TABLE_NAME");
                if (tName != null && tName.equals(table.toLowerCase())) {
                    tExists = true;
                    break;
                }
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] SQL Exception : " + e+" (tableExists [Database.java])");
            return false;
        }
        return tExists;
    }
}
