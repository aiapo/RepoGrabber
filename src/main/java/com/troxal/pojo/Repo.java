
package com.troxal.pojo;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "owner",
    "url",
    "description",
    "createdAt",
    "updatedAt",
    "pushedAt",
    "isArchived",
    "archivedAt",
    "isPrivate",
    "isFork",
    "isLocked",
    "isEmpty",
    "isDisabled",
    "isTemplate",
    "primaryLanguage",
    "pullRequests",
    "forkCount",
    "stargazerCount",
    "watchers",
    "issues",
    "issueUsers",
    "mentionableUsers",
    "languages",
    "mainBranch"
})
@Generated("jsonschema2pojo")
public class Repo {

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("owner")
    private Owner owner;
    @JsonProperty("url")
    private String url;
    @JsonProperty("description")
    private String description;
    @JsonProperty("createdAt")
    private String createdAt;
    @JsonProperty("updatedAt")
    private String updatedAt;
    @JsonProperty("pushedAt")
    private String pushedAt;
    @JsonProperty("isArchived")
    private Boolean isArchived;
    @JsonProperty("archivedAt")
    private String archivedAt;
    @JsonProperty("isPrivate")
    private Boolean isPrivate;
    @JsonProperty("isFork")
    private Boolean isFork;
    @JsonProperty("isLocked")
    private Boolean isLocked;
    @JsonProperty("isEmpty")
    private Boolean isEmpty;
    @JsonProperty("isDisabled")
    private Boolean isDisabled;
    @JsonProperty("isTemplate")
    private Boolean isTemplate;
    @JsonProperty("primaryLanguage")
    private PrimaryLanguage primaryLanguage;
    @JsonProperty("pullRequests")
    private PullRequests pullRequests;
    @JsonProperty("forkCount")
    private Integer forkCount;
    @JsonProperty("stargazerCount")
    private Integer stargazerCount;
    @JsonProperty("watchers")
    private Watchers watchers;
    @JsonProperty("issues")
    private Issues issues;
    @JsonProperty("issueUsers")
    private IssueUsers issueUsers;
    @JsonProperty("mentionableUsers")
    private MentionableUsers mentionableUsers;
    @JsonProperty("languages")
    private Languages languages;
    @JsonProperty("mainBranch")
    private MainBranch mainBranch;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("owner")
    public Owner getOwner() {
        return owner;
    }

    @JsonProperty("owner")
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("createdAt")
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("createdAt")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("updatedAt")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updatedAt")
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonProperty("pushedAt")
    public String getPushedAt() {
        return pushedAt;
    }

    @JsonProperty("pushedAt")
    public void setPushedAt(String pushedAt) {
        this.pushedAt = pushedAt;
    }

    @JsonProperty("isArchived")
    public Boolean getIsArchived() {
        return isArchived;
    }

    @JsonProperty("isArchived")
    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

    @JsonProperty("archivedAt")
    public String getArchivedAt() {
        return archivedAt;
    }

    @JsonProperty("archivedAt")
    public void setArchivedAt(String archivedAt) {
        this.archivedAt = archivedAt;
    }

    @JsonProperty("isPrivate")
    public Boolean getIsPrivate() {
        return isPrivate;
    }

    @JsonProperty("isPrivate")
    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    @JsonProperty("isFork")
    public Boolean getIsFork() {
        return isFork;
    }

    @JsonProperty("isFork")
    public void setIsFork(Boolean isFork) {
        this.isFork = isFork;
    }

    @JsonProperty("isLocked")
    public Boolean getIsLocked() {
        return isLocked;
    }

    @JsonProperty("isLocked")
    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }

    @JsonProperty("isEmpty")
    public Boolean getIsEmpty() {
        return isEmpty;
    }

    @JsonProperty("isEmpty")
    public void setIsEmpty(Boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

    @JsonProperty("isDisabled")
    public Boolean getIsDisabled() {
        return isDisabled;
    }

    @JsonProperty("isDisabled")
    public void setIsDisabled(Boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    @JsonProperty("isTemplate")
    public Boolean getIsTemplate() {
        return isTemplate;
    }

    @JsonProperty("isTemplate")
    public void setIsTemplate(Boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    @JsonProperty("primaryLanguage")
    public PrimaryLanguage getPrimaryLanguage() {
        return primaryLanguage;
    }

    @JsonProperty("primaryLanguage")
    public void setPrimaryLanguage(PrimaryLanguage primaryLanguage) {
        this.primaryLanguage = primaryLanguage;
    }

    @JsonProperty("pullRequests")
    public PullRequests getPullRequests() {
        return pullRequests;
    }

    @JsonProperty("pullRequests")
    public void setPullRequests(PullRequests pullRequests) {
        this.pullRequests = pullRequests;
    }

    @JsonProperty("forkCount")
    public Integer getForkCount() {
        return forkCount;
    }

    @JsonProperty("forkCount")
    public void setForkCount(Integer forkCount) {
        this.forkCount = forkCount;
    }

    @JsonProperty("stargazerCount")
    public Integer getStargazerCount() {
        return stargazerCount;
    }

    @JsonProperty("stargazerCount")
    public void setStargazerCount(Integer stargazerCount) {
        this.stargazerCount = stargazerCount;
    }

    @JsonProperty("watchers")
    public Watchers getWatchers() {
        return watchers;
    }

    @JsonProperty("watchers")
    public void setWatchers(Watchers watchers) {
        this.watchers = watchers;
    }

    @JsonProperty("issues")
    public Issues getIssues() {
        return issues;
    }

    @JsonProperty("issues")
    public void setIssues(Issues issues) {
        this.issues = issues;
    }

    @JsonProperty("issueUsers")
    public IssueUsers getIssueUsers() {
        return issueUsers;
    }

    @JsonProperty("issueUsers")
    public void setIssueUsers(IssueUsers issueUsers) {
        this.issueUsers = issueUsers;
    }

    @JsonProperty("mentionableUsers")
    public MentionableUsers getMentionableUsers() {
        return mentionableUsers;
    }

    @JsonProperty("mentionableUsers")
    public void setMentionableUsers(MentionableUsers mentionableUsers) {
        this.mentionableUsers = mentionableUsers;
    }

    @JsonProperty("languages")
    public Languages getLanguages() {
        return languages;
    }

    @JsonProperty("languages")
    public void setLanguages(Languages languages) {
        this.languages = languages;
    }

    @JsonProperty("mainBranch")
    public MainBranch getMainBranch() {
        return mainBranch;
    }

    @JsonProperty("mainBranch")
    public void setMainBranch(MainBranch mainBranch) {
        this.mainBranch = mainBranch;
    }

}
