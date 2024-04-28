package com.troxal.manipulation;

import com.troxal.database.Database;
import com.troxal.database.Manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class RGDS {
    String comment = "\n% ";
    String RGDS_Version = "1.0.0";

    public RGDS(Boolean headless){
        String fileName;

        if(!headless){
            //Allows user to input filename for CSV and fixes any illegal characters
            System.out.println("Enter a name for the file here: ");
            Scanner fileInput = new Scanner(System.in);
            fileName = fileInput.nextLine();
            fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        }else{
            fileName="dataset";
        }

        create(fileName);
    }

    private byte[] createHeader(String title, String description){
        StringBuilder sb = new StringBuilder();
        sb.append("@RGDS_VERSION "+RGDS_Version);

        sb.append("\n% Title: ").
                append(title).
                append(comment).
                append(comment).
                append("Description: ").
                append(description);

        int i = 0;
        while (i + 70 < sb.length() && (i = sb.lastIndexOf(" ", i + 70)) != -1) {
            sb.replace(i, i + 1, comment);
        }

        sb.append("\n\n")
                .append("@TITLE \"")
                .append(title.replaceAll("\"", "\\\\\""))
                .append("\"")
                .append("\n\n");

        return sb.toString().getBytes();
    }

    private byte[] createRelation(String relationName,String[] attributes){
        StringBuilder sb = new StringBuilder();

        sb.append("@RELATION ")
                .append(relationName)
                .append("\n");

        for(String a : attributes){
            a = a.toLowerCase();
            String[] splitA = a.split(",");
            sb.append("@ATTRIBUTE ")
                    .append(splitA[0])
                    .append(" ")
                    .append(splitA[1])
                    .append("\n");
        }

        sb.append("\n");

        return sb.toString().getBytes();
    }

    private byte[] dataRowFormat(List<String> dataLine){
        StringBuilder sb = new StringBuilder();

        for(int i=0;i<dataLine.size();i++){
            String data = dataLine.get(i).replaceAll("\"", "\\\\\"");
            sb.append("\"")
                    .append(data)
                    .append("\"");

            if(!(i == dataLine.size()-1))
                sb.append(",");
        }

        sb.append("\n");

        return sb.toString().getBytes();
    }

    private void getFromDB(GZIPOutputStream gos,Map.Entry<String,String[]> relation){
        String[] attributes = relation.getValue();
        Database db = new Manager().access();

        try{
            gos.write("@DATA\n".getBytes());
        }catch(IOException e){
            System.out.println(e);
        }

        try(ResultSet repos = db.select(relation.getKey(),new String[]{"*"})){
            while(repos.next()){
                List<String> tempLine = new ArrayList<>();
                for(int i=0;i<attributes.length;i++){
                    String attributeName = attributes[i].split(",")[0].toLowerCase();
                    if(!attributeName.isBlank()){
                        if(hasColumn(repos,attributeName))
                            if(repos.getString(attributeName)!=null)
                                tempLine.add(repos.getString(attributeName));
                            else
                                tempLine.add("");
                        else{
                            ResultSetMetaData rsmd = repos.getMetaData();
                            for(int x = 1; x <= rsmd.getColumnCount(); x++){
                                System.out.println(rsmd.getColumnName(x)+" v "+attributeName);
                            }

                        }

                    }
                }

                try{
                    gos.write(dataRowFormat(tempLine));
                }catch(IOException e){
                    System.out.println(e);
                }
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] SQL Exception: "+e+" (detect [RefMine.java])");
        }
        db.close();

        try{
            gos.write("\n".getBytes());
        }catch(IOException e){
            System.out.println(e);
        }
    }

    private void writeCompressed(String fileName, byte[] header, Map<String,String[]> relations) {
        new File("./results").mkdirs();
        File file = new File("results/"+ fileName + ".rgds");


        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(file))){
            gos.write(header);
            for(Map.Entry<String,String[]> relation : relations.entrySet()){
                gos.write(createRelation("Repositories",relation.getValue()));
                getFromDB(gos,relation);
            }
        }catch(IOException e){
            System.out.println(e);
        }
    }

    private void create(String fileName){
        byte[] header = createHeader("Example dataset","This dataset is for the purposes of testing " +
                "the RGDS format, and the RepoGrabberGrapher Tool.");

        Map<String,String[]> relations = new HashMap<>();

        relations.put("Repositories",new String[]{
                "ID,VARCHAR",
                "NAME,VARCHAR",
                "OWNER,VARCHAR",
                "URL,VARCHAR",
                "DESCRIPTION,TEXT",
                "PRIMARYLANGUAGE,VARCHAR",
                "CREATIONDATE,VARCHAR",
                "UPDATEDATE,VARCHAR",
                "PUSHDATE,VARCHAR",
                "ISARCHIVED,BOOLEAN",
                "ARCHIVEDAT,VARCHAR",
                "ISFORKED,BOOLEAN",
                "ISEMPTY,BOOLEAN",
                "ISLOCKED,BOOLEAN",
                "ISDISABLED,BOOLEAN",
                "ISTEMPLATE,BOOLEAN",
                "TOTALISSUEUSERS,INTEGER",
                "TOTALMENTIONABLEUSERS,INTEGER",
                "TOTALCOMMITTERCOUNT,INTEGER",
                "TOTALPROJECTSIZE,INTEGER",
                "TOTALCOMMITS,INTEGER",
                "ISSUECOUNT,INTEGER",
                "FORKCOUNT,INTEGER",
                "STARCOUNT,INTEGER",
                "WATCHCOUNT,INTEGER",
                "BRANCHNAME,VARCHAR",
                "DOMAIN,VARCHAR"}
        );

        relations.put("Languages",new String[]{
                "repoid,VARCHAR",
                "name,VARCHAR",
                "size,INTEGER"}
        );

        relations.put("Refactorings",new String[]{
                "refactoringhash,VARCHAR",
                "commit,VARCHAR",
                "gituri,VARCHAR",
                "repositoryid,VARCHAR",
                "refactoringname,VARCHAR",
                "leftStartLine,INTEGER",
                "leftEndLine,INTEGER",
                "leftStartColumn,INTEGER",
                "leftEndColumn,INTEGER",
                "leftFilePath,VARCHAR",
                "leftCodeElementType,VARCHAR",
                "leftDescription,TEXT ",
                "leftcodeelement,VARCHAR",
                "rightStartLine,INTEGER",
                "rightEndLine,INTEGER",
                "rightStartColumn,INTEGER",
                "rightEndColumn,INTEGER",
                "rightFilePath,VARCHAR",
                "rightCodeElementType,VARCHAR",
                "rightDescription,TEXT",
                "rightcodeelement,VARCHAR",
                "commitauthor,VARCHAR",
                "commitmessage,VARCHAR",
                "commitdate,TIMESTAMP"}
        );

        writeCompressed(fileName,header,relations);
    }

    public Boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equals(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }
}

