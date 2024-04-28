package com.troxal.manipulation;

import com.troxal.database.Database;
import com.troxal.database.Manager;
import com.troxal.pojo.LanguageInfo;
import com.troxal.pojo.RepoInfo;

import java.io.*;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class RGDS {
    String comment = "\n% ";
    String RGDS_Version = "1.0.0";
    Database db;

    public RGDS(){
        db = new Manager().access();
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
                                tempLine.add(repos.getString(attributeName).replaceAll("[\\t\\n\\r]+"," "));
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
            System.out.println("[ERROR] SQL Exception: "+e+" (getFromDB [RGDS.java])");
        }

        try{
            gos.write("\n".getBytes());
        }catch(IOException e){
            System.out.println(e);
        }
    }

    public Boolean write(Boolean compressed, String fileName) {
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
                "leftDescription,TEXT",
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

        if(compressed){
            new File("./results").mkdirs();
            File file = new File("results/"+ fileName + ".rgds");


            try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(file))){
                gos.write(header);
                for(Map.Entry<String,String[]> relation : relations.entrySet()){
                    gos.write(createRelation(relation.getKey(),relation.getValue()));
                    getFromDB(gos,relation);
                }
                return true;
            }catch(IOException e){
                System.out.println(e);
            }
        }else{
            System.out.println("[ERROR] Only compressed RGDS is supported at this time.");
        }
        return false;
    }

    public List<RepoInfo> read(Boolean compressed, String fileName){
        List<RepoInfo> repos = new ArrayList<>();

        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            Map<String,Integer> validAction = new HashMap<>();
            validAction.put("rgds_version",1);
            validAction.put("title",1);
            validAction.put("relation",1);
            validAction.put("attribute",2);
            validAction.put("data",0);

            if(compressed){
                try(InputStream fileStream = new FileInputStream("results/"+fileName+".rgds")){
                    try(InputStream gzipStream = new GZIPInputStream(fileStream)){
                        new DigestInputStream(gzipStream, md);
                        Reader decoder = new InputStreamReader(gzipStream);
                        BufferedReader buffered = new BufferedReader(decoder);

                        String hashtext = new BigInteger(1, md.digest()).toString(16);
                        while (hashtext.length() < 32) {
                            hashtext = "0" + hashtext;
                        }
                        System.out.println("[DEBUG] This RGDS hash is: "+hashtext);

                        String currentRelation = "";
                        Boolean startDataRead = false;

                        while (buffered.ready()) {
                            String currentLine = buffered.readLine();
                            if(!currentLine.equalsIgnoreCase("")){
                                if(!currentLine.startsWith("%")){
                                    if(currentLine.startsWith("@")){
                                        String[] currentActionFull = currentLine.substring(1)
                                                .split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                                        String currentAction = currentActionFull[0].toLowerCase();
                                        if(validAction.containsKey(currentAction)){
                                            if(validAction.get(currentAction)==currentActionFull.length-1){
                                                startDataRead=false;
                                                if(currentAction.equalsIgnoreCase("relation")){
                                                    currentRelation = currentActionFull[1];
                                                    System.out.println("[DEBUG] Switch currentRelation to "+currentRelation);
                                                }else if(currentAction.equalsIgnoreCase("data")){
                                                    startDataRead=true;
                                                }
                                            }else
                                                System.out.println("[ERROR] Invalid number of parameters for @Action " +
                                                        "\""+currentAction+"\" in RGDS file, ignoring... :: "+
                                                        Arrays.toString(currentActionFull));
                                        }else
                                            System.out.println("[ERROR] Invalid @Action in RGDS file, ignoring...");
                                    }else if(startDataRead){
                                        List<LanguageInfo> tempLanguages = new ArrayList<>();
                                        String[] currentDataLine = currentLine.split("\",\"", -1);

                                        for(int i=0;i<currentDataLine.length;i++){
                                            currentDataLine[i] = removeQuotes(currentDataLine[i]);
                                        }

                                        if(currentRelation.equalsIgnoreCase("repositories")){
                                            repos.add(new RepoInfo(
                                                    currentDataLine[0],
                                                    currentDataLine[1],
                                                    currentDataLine[2],
                                                    currentDataLine[3],
                                                    currentDataLine[4],
                                                    currentDataLine[5],
                                                    currentDataLine[6],
                                                    currentDataLine[7],
                                                    currentDataLine[8],
                                                    parseBool(currentDataLine[9]),
                                                    currentDataLine[10],
                                                    parseBool(currentDataLine[11]),
                                                    parseBool(currentDataLine[12]),
                                                    parseBool(currentDataLine[13]),
                                                    parseBool(currentDataLine[14]),
                                                    parseBool(currentDataLine[15]),
                                                    Integer.valueOf(currentDataLine[16]),
                                                    Integer.valueOf(currentDataLine[17]),
                                                    Integer.valueOf(currentDataLine[18]),
                                                    Integer.valueOf(currentDataLine[19]),
                                                    Integer.valueOf(currentDataLine[20]),
                                                    Integer.valueOf(currentDataLine[21]),
                                                    Integer.valueOf(currentDataLine[22]),
                                                    Integer.valueOf(currentDataLine[23]),
                                                    Integer.valueOf(currentDataLine[24]),
                                                    tempLanguages,
                                                    currentDataLine[25]
                                            ));
                                        }else if(currentRelation.equalsIgnoreCase("languages")){
                                            for(RepoInfo r : repos){
                                                if(Objects.equals(r.getId(), currentDataLine[0])){
                                                    r.addLanguage(new LanguageInfo(
                                                            currentDataLine[1],
                                                            Integer.valueOf(currentDataLine[2] )
                                                    ));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }catch(IOException e){
                        System.out.println(e);
                    }
                }catch(IOException e){
                    System.out.println(e);
                }
            }
        }catch(java.security.NoSuchAlgorithmException ex){
            System.out.println("[ERROR] "+ex);
        }

        return repos;
    }

    private Boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equals(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }

    private String removeQuotes(String in){
        return in.replaceAll("^\"|\"$", "");
    }

    private Boolean parseBool(String in){
        return in.equalsIgnoreCase("t");
    }

    public Boolean close(){
        db.close();
        return true;
    }
}

