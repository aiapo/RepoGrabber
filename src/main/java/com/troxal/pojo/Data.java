
package com.troxal.pojo;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "rateLimit",
    "search",
    "repo"
})
@Generated("jsonschema2pojo")
public class Data {
    private String errorType;
    @JsonProperty("rateLimit")
    private RateLimit rateLimit;
    @JsonProperty("search")
    private Search search;

    @JsonProperty("repo")
    private Repo repo;

    @JsonProperty("rateLimit")
    public RateLimit getRateLimit() {
        return rateLimit;
    }

    @JsonProperty("rateLimit")
    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    @JsonProperty("search")
    public Search getSearch() {
        return search;
    }

    @JsonProperty("search")
    public void setSearch(Search search) {
        this.search = search;
    }

    @JsonProperty("repo")
    public Repo getRepo() {
        return repo;
    }

    @JsonProperty("repo")
    public void setRepo(Repo repo) {
        this.repo = repo;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorType(){ return errorType; }

}
