package com.troxal;

import java.util.ArrayList;
import java.util.List;

public class RepoData {
    private String id,name,url,creationDate;
    private Integer users,totalSize,totalCommits;
    private List<Language> languages = new ArrayList<>();

    public RepoData(String id, String name, String url, String creationDate, Integer users, Integer totalSize, Integer totalCommits, List<Language> languages){
        setId(id);
        setName(name);
        setUrl(url);
        setCreationDate(creationDate);
        setUsers(users);
        setTotalSize(totalSize);
        setLanguages(languages);
        setTotalCommits(totalCommits);
    }

    public void setId(String id){
        this.id=id;
    }
    public String getId(){
        return id;
    }
    public void setName(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }
    public void setUrl(String url){
        this.url=url;
    }
    public String getUrl(){
        return url;
    }
    public void setCreationDate(String creationDate){
        this.creationDate=creationDate;
    }
    public String getCreationDate(){
        return creationDate;
    }
    public void setUsers(Integer users){
        this.users=users;
    }
    public Integer getUsers(){
        return users;
    }
    public void setTotalSize(Integer totalSize){
        this.totalSize=totalSize;
    }
    public Integer getTotalSize(){
        return totalSize;
    }
    public void setTotalCommits(Integer totalCommits){
        this.totalCommits=totalCommits;
    }
    public Integer getTotalCommits(){
        return totalCommits;
    }
    public void setLanguages(List<Language> languages){
        this.languages=languages;
    }
    public List<Language> getLanguages(){
        return languages;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("** Repo **\n");
        sb.append("ID: ");
        sb.append(getId());
        sb.append('\n');
        sb.append("Name: ");
        sb.append(getName());
        sb.append('\n');
        sb.append("URL: ");
        sb.append(getUrl());
        sb.append('\n');
        sb.append("Creation Date: ");
        sb.append(getCreationDate());
        sb.append('\n');
        sb.append("Users: ");
        sb.append(getUsers());
        sb.append('\n');
        sb.append("Total Size: ");
        sb.append(getTotalSize());
        sb.append('\n');
        sb.append("Total Commits: ");
        sb.append(getTotalCommits());
        sb.append('\n');
        sb.append("Languages:");
        for (int i = 0; i < languages.size(); i++) {
            sb.append("\n - Name: ");
            sb.append(languages.get(i).getName());
            sb.append("\n   Size: ");
            sb.append(languages.get(i).getSize());
        }
        sb.append("\n**\n\n");
        return sb.toString();
    }
}
