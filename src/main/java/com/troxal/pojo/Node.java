
package com.troxal.pojo;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "id",
    "messageHeadline",
    "messageBody",
    "authoredDate",
    "additions",
    "deletions",
    "changedFilesIfAvailable",
    "author"
})
@Generated("jsonschema2pojo")
public class Node {

    @JsonProperty("name")
    private String name;
    @JsonProperty("id")
    private String id;
    @JsonProperty("messageHeadline")
    private String messageHeadline;
    @JsonProperty("messageBody")
    private String messageBody;
    @JsonProperty("authoredDate")
    private String authoredDate;
    @JsonProperty("additions")
    private Integer additions;
    @JsonProperty("deletions")
    private Integer deletions;
    @JsonProperty("changedFilesIfAvailable")
    private Integer changedFilesIfAvailable;
    @JsonProperty("author")
    private Author author;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("authoredDate")
    public String getAuthoredDate() {
        return authoredDate;
    }

    @JsonProperty("authoredDate")
    public void setAuthoredDate(String authoredDate) {
        this.authoredDate = authoredDate;
    }

    @JsonProperty("messageHeadline")
    public String getMessageHeadline() {
        return messageHeadline;
    }

    @JsonProperty("messageHeadline")
    public void setMessageHeadline(String messageHeadline) {
        this.messageHeadline = messageHeadline;
    }

    @JsonProperty("messageBody")
    public String getMessageBody() {
        return messageBody;
    }

    @JsonProperty("messageBody")
    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    @JsonProperty("additions")
    public Integer getAdditions() {
        return additions;
    }

    @JsonProperty("additions")
    public void setAdditions(Integer additions) {
        this.additions = additions;
    }

    @JsonProperty("deletions")
    public Integer getDeletions() {
        return deletions;
    }

    @JsonProperty("deletions")
    public void setDeletions(Integer deletions) {
        this.deletions = deletions;
    }

    @JsonProperty("changedFilesIfAvailable")
    public Integer getChangedFilesIfAvailable() {
        return changedFilesIfAvailable;
    }

    @JsonProperty("changedFilesIfAvailable")
    public void setChangedFilesIfAvailable(Integer changedFilesIfAvailable) {
        this.changedFilesIfAvailable = changedFilesIfAvailable;
    }

    @JsonProperty("author")
    public Author getAuthor() {
        return author;
    }

    @JsonProperty("author")
    public void setAuthor(Author author) { this.author = author; }

}
