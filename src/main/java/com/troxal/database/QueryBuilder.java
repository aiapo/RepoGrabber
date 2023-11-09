package com.troxal.database;

public class QueryBuilder {
    private StringBuilder query;

    // Adds from condition
    public QueryBuilder from(String table){
        query.append(" FROM ");
        query.append(table);
        return this;
    }

    // Adds value(s)
    public QueryBuilder values(Object[] params){
        query.append(" VALUES(");

        int count = params.length;

        if(count == 0)
            throw new IllegalArgumentException("Invalid parameter count");

        for (int i = 0; i<count; i++) {
            query.append("?,");
        }
        //removes the last comma
        query.deleteCharAt(query.lastIndexOf(","));
        query.append(")");
        return this;
    }

    // Adds where condition(s)
    public QueryBuilder where(String requirement){
        query.append(" WHERE ");
        query.append(requirement);
        return this;
    }

    // Adds attribute(s)
    public QueryBuilder attributes(Object[] params){
        query.append(" (");

        int count = params.length;

        if(count == 0)
            throw new IllegalArgumentException("Invalid parameter count");

        for (int i = 0; i<count; i++) {
            query.append(params[i]+",");
        }

        query.deleteCharAt(query.lastIndexOf(","));
        query.append(")");

        return this;
    }

    // Adds tuple update(s)
    public QueryBuilder set(String[] columns){
        int count = columns.length;
        if(count == 0)
            throw new IllegalArgumentException("Invalid argument count");

        for(String column : columns){
            query.append(column);
            query.append(" = ");
            query.append("?");
            query.append(",");
        }
        query.deleteCharAt(query.lastIndexOf(","));
        return this;
    }

    // Creates prepared statement for creates
    public QueryBuilder create(String table){
        query = new StringBuilder();
        query.append("CREATE TABLE ");
        query.append(table);

        return this;
    }

    // Creates prepared statement for insertions
    public QueryBuilder insert(String table){
        query = new StringBuilder();
        query.append("INSERT INTO ");
        query.append(table);
        return this;
    }

    // Creates prepared statement for selections
    public QueryBuilder select(Object[] columns){
        query = new StringBuilder();
        query.append("SELECT ");
        if(columns != null){
            for(Object column : columns){
                query.append(column);
                query.append(",");
            }
            query.deleteCharAt(query.lastIndexOf(","));
        }
        else
            query.append("*");

        return this;
    }

    // Creates prepared statement for MAX selections
    public QueryBuilder max(Object[] columns){
        query = new StringBuilder();
        query.append("SELECT MAX(");
        if(columns != null){
            for(Object column : columns){
                query.append(column);
                query.append(",");
            }
            query.deleteCharAt(query.lastIndexOf(","));
        }
        else
            query.append("*");

        query.append(") AS max_id");

        return this;
    }

    // Creates a prepared statement for deletions
    public QueryBuilder delete(String table){
        query = new StringBuilder();
        query.append("DELETE FROM ");
        query.append(table);
        return this;
    }

    // Creates prepared statement for updates
    public QueryBuilder update(String table){
        query = new StringBuilder();
        query.append("UPDATE ");
        query.append(table);
        query.append(" SET ");
        return this;
    }

    public QueryBuilder onConflict(Object[] columns){
        if(columns != null){
            query.append(" ON CONFLICT (");
            for(Object column : columns){
                query.append(column);
                query.append(",");
            }
            query.deleteCharAt(query.lastIndexOf(","));
            query.append(") DO NOTHING");
        }
        return this;
    }

    // Stringify query
    public String stringifyQuery(){
        query.append(";");
        return query.toString();
    }
}