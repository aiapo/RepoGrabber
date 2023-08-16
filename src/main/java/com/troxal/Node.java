
package com.troxal;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "createdAt",
    "isArchived",
    "isPrivate",
    "url",
    "assignableUsers",
    "languages",
    "defaultBranchRef"
})
@Generated("jsonschema2pojo")
public class Node {

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("createdAt")
    private String createdAt;
    @JsonProperty("isArchived")
    private Boolean isArchived;
    @JsonProperty("isPrivate")
    private Boolean isPrivate;
    @JsonProperty("url")
    private String url;
    @JsonProperty("assignableUsers")
    private AssignableUsers assignableUsers;
    @JsonProperty("languages")
    private Languages languages;
    @JsonProperty("defaultBranchRef")
    private DefaultBranchRef defaultBranchRef;

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

    @JsonProperty("createdAt")
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("createdAt")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("isArchived")
    public Boolean getIsArchived() {
        return isArchived;
    }

    @JsonProperty("isArchived")
    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

    @JsonProperty("isPrivate")
    public Boolean getIsPrivate() {
        return isPrivate;
    }

    @JsonProperty("isPrivate")
    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("assignableUsers")
    public AssignableUsers getAssignableUsers() {
        return assignableUsers;
    }

    @JsonProperty("assignableUsers")
    public void setAssignableUsers(AssignableUsers assignableUsers) {
        this.assignableUsers = assignableUsers;
    }

    @JsonProperty("languages")
    public Languages getLanguages() {
        return languages;
    }

    @JsonProperty("languages")
    public void setLanguages(Languages languages) {
        this.languages = languages;
    }

    @JsonProperty("defaultBranchRef")
    public DefaultBranchRef getDefaultBranchRef() {
        return defaultBranchRef;
    }

    @JsonProperty("defaultBranchRef")
    public void setDefaultBranchRef(DefaultBranchRef defaultBranchRef) {
        this.defaultBranchRef = defaultBranchRef;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        //sb.append(Node.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("createdAt");
        sb.append('=');
        sb.append(((this.createdAt == null)?"<null>":this.createdAt));
        sb.append(',');
        sb.append("isArchived");
        sb.append('=');
        sb.append(((this.isArchived == null)?"<null>":this.isArchived));
        sb.append(',');
        sb.append("isPrivate");
        sb.append('=');
        sb.append(((this.isPrivate == null)?"<null>":this.isPrivate));
        sb.append(',');
        sb.append("url");
        sb.append('=');
        sb.append(((this.url == null)?"<null>":this.url));
        sb.append(',');
        sb.append("assignableUsers");
        sb.append('=');
        sb.append(((this.assignableUsers == null)?"<null>":this.assignableUsers));
        sb.append(',');
        sb.append("languages");
        sb.append('=');
        sb.append(((this.languages == null)?"<null>":this.languages));
        sb.append(',');
        sb.append("defaultBranchRef");
        sb.append('=');
        sb.append(((this.defaultBranchRef == null)?"<null>":this.defaultBranchRef));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
