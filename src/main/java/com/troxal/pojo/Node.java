
package com.troxal.pojo;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "id",
    "authoredDate"
})
@Generated("jsonschema2pojo")
public class Node {

    @JsonProperty("name")
    private String name;
    @JsonProperty("id")
    private String id;
    @JsonProperty("authoredDate")
    private String authoredDate;


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

}
