package com.troxal.pojo;

import com.troxal.pojo.LanguageInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RepoInfo implements Serializable {
    private String id,name,url,description,primaryLanguage,creationDate,updateDate,pushDate,branchName,owner,archivedAt;
    private Boolean isArchived,isFork,isEmpty,isLocked,isDisabled,isTemplate;
    private Integer totalIssueUsers,totalMentionableUsers,totalProjectSize,totalCommits,totalCommitterCount,issueCount;
    private Integer forkCount,starCount,watchCount;
    private List<LanguageInfo> languages = new ArrayList<>();

    public RepoInfo(
            String id,
            String name,
            String owner,
            String url,
            String description,
            String primaryLanguage,
            String creationDate,
            String updateDate,
            String pushDate,
            Boolean isArchived,
            String archivedAt,
            Boolean isFork,
            Boolean isEmpty,
            Boolean isLocked,
            Boolean isDisabled,
            Boolean isTemplate,
            Integer totalIssueUsers,
            Integer totalMentionableUsers,
            Integer totalCommitterCount,
            Integer totalProjectSize,
            Integer totalCommits,
            Integer issueCount,
            Integer forkCount,
            Integer starCount,
            Integer watchCount,
            List<LanguageInfo> languages,
            String branchName) {
        setId(id);
        setName(name);
        setUrl(url);
        setDescription(description);
        setPrimaryLanguage(primaryLanguage);
        setCreationDate(creationDate);
        setUpdateDate(updateDate);
        setPushDate(pushDate);
        setIsArchived(isArchived);
        setIsFork(isFork);
        setTotalMentionableUsers(totalMentionableUsers);
        setTotalIssueUsers(totalIssueUsers);
        setTotalCommitterCount(totalCommitterCount);
        setTotalProjectSize(totalProjectSize);
        setLanguages(languages);
        setTotalCommits(totalCommits);
        setForkCount(forkCount);
        setStarCount(starCount);
        setWatchCount(watchCount);
        setBranchName(branchName);
        setIsDisabled(isDisabled);
        setIsEmpty(isEmpty);
        setIsLocked(isLocked);
        setIsTemplate(isTemplate);
        setArchivedAt(archivedAt);
        setIssueCount(issueCount);
        setOwner(owner);
    }

    // Set/Get ID
    public void setId(String id){
        this.id=id;
    }
    public String getId(){
        return id;
    }

    // Set/Get name
    public void setName(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }

    // Set/Get owner
    public void setOwner(String owner){
        this.owner=owner;
    }
    public String getOwner(){
        return owner;
    }

    // Set/Get URL
    public void setUrl(String url){
        this.url=url;
    }
    public String getUrl(){
        return url;
    }

    // Set/Get description
    public void setDescription(String description){
        this.description=description;
    }
    public String getDescription(){
        return description;
    }

    // Set/Get primaryLanguage
    public void setPrimaryLanguage(String primaryLanguage){
        this.primaryLanguage=primaryLanguage;
    }
    public String getPrimaryLanguage(){
        return primaryLanguage;
    }

    // Set/Get creationDate
    public void setCreationDate(String creationDate){
        this.creationDate=creationDate;
    }
    public String getCreationDate(){
        return creationDate;
    }

    // Set/Get updateDate
    public void setUpdateDate(String updateDate){
        this.updateDate=updateDate;
    }
    public String getUpdateDate(){
        return updateDate;
    }

    // Set/Get pushDate
    public void setPushDate(String pushDate){
        this.pushDate=pushDate;
    }
    public String getPushDate(){
        return pushDate;
    }

    // Set/Get archivedAt
    public void setArchivedAt(String archivedAt){
        this.archivedAt=archivedAt;
    }
    public String getArchivedAt(){
        return archivedAt;
    }

    // Set/Get totalMentionableUsers
    public void setTotalMentionableUsers(Integer totalMentionableUsers){
        this.totalMentionableUsers=totalMentionableUsers;
    }
    public Integer getTotalMentionableUsers(){
        return totalMentionableUsers;
    }

    // Set/Get totalIssueUsers
    public void setTotalIssueUsers(Integer totalIssueUsers){
        this.totalIssueUsers=totalIssueUsers;
    }
    public Integer getTotalIssueUsers(){
        return totalIssueUsers;
    }

    // Set/Get totalProjectSize
    public void setTotalProjectSize(Integer totalProjectSize){
        this.totalProjectSize=totalProjectSize;
    }
    public Integer getTotalProjectSize(){
        return totalProjectSize;
    }

    // Set/Get totalCommits
    public void setTotalCommits(Integer totalCommits){
        this.totalCommits=totalCommits;
    }
    public Integer getTotalCommits(){
        return totalCommits;
    }

    // Set/Get forkCount
    public void setForkCount(Integer forkCount){
        this.forkCount=forkCount;
    }
    public Integer getForkCount(){
        return forkCount;
    }

    // Set/Get starCount
    public void setStarCount(Integer starCount){
        this.starCount=starCount;
    }
    public Integer getStarCount(){
        return starCount;
    }

    // Set/Get watchCount
    public void setWatchCount(Integer watchCount){
        this.watchCount=watchCount;
    }
    public Integer getWatchCount(){
        return watchCount;
    }

    // Set/Get issueCount
    public void setIssueCount(Integer issueCount){
        this.issueCount=issueCount;
    }
    public Integer getIssueCount(){
        return issueCount;
    }

    // Set/Get isArchived
    public void setIsArchived(Boolean isArchived){
        this.isArchived=isArchived;
    }
    public Boolean getIsArchived(){
        return isArchived;
    }

    // Set/Get isFork
    public void setIsFork(Boolean isFork){
        this.isFork=isFork;
    }
    public Boolean getIsFork(){
        return isFork;
    }

    // Set/Get isEmpty
    public void setIsEmpty(Boolean isEmpty){
        this.isEmpty=isEmpty;
    }
    public Boolean getIsEmpty(){
        return isEmpty;
    }

    // Set/Get isLocked
    public void setIsLocked(Boolean isLocked){
        this.isLocked=isLocked;
    }
    public Boolean getIsLocked(){
        return isLocked;
    }

    // Set/Get isDisabled
    public void setIsDisabled(Boolean isDisabled){
        this.isDisabled=isDisabled;
    }
    public Boolean getIsDisabled(){
        return isDisabled;
    }

    // Set/Get isTemplate
    public void setIsTemplate(Boolean isTemplate){
        this.isTemplate=isTemplate;
    }
    public Boolean getIsTemplate(){
        return isTemplate;
    }

    public void setLanguages(List<LanguageInfo> languages){
        this.languages=languages;
    }
    public List<LanguageInfo> getLanguages(){
        return languages;
    }
    public Boolean addLanguage(LanguageInfo language){ return languages.add(language); }

    // Set/Get branchName
    public void setBranchName(String branchName){
        this.branchName=branchName;
    }
    public String getBranchName(){
        return branchName;
    }

    // Set/Get totalCommitterCount
    public void setTotalCommitterCount(Integer totalCommitterCount){ this.totalCommitterCount=totalCommitterCount;}
    public Integer getTotalCommitterCount(){ return totalCommitterCount; }

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
        sb.append("Mentionable users: ");
        sb.append(getTotalMentionableUsers());
        sb.append('\n');
        sb.append("Total project size: ");
        sb.append(getTotalProjectSize());
        sb.append('\n');
        sb.append("Total Commits: ");
        sb.append(getTotalCommits());
        sb.append('\n');
        sb.append("Main Branch Name: ");
        sb.append(getBranchName());
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
