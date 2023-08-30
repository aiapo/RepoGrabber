
package com.troxal.pojo;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "repo"
})
@Generated("jsonschema2pojo")
public class Repository {

    @JsonProperty("repo")
    private Repo repo;

    @JsonProperty("repo")
    public Repo getRepo() {
        return repo;
    }

    @JsonProperty("repo")
    public void setRepo(Repo repo) {
        this.repo = repo;
    }

}
