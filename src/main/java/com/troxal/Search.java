
package com.troxal;

import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "repositoryCount",
    "pageInfo",
    "repositories"
})
@Generated("jsonschema2pojo")
public class Search {

    @JsonProperty("repositoryCount")
    private Integer repositoryCount;
    @JsonProperty("pageInfo")
    private PageInfo pageInfo;
    @JsonProperty("repositories")
    private List<Repository> repositories;

    @JsonProperty("repositoryCount")
    public Integer getRepositoryCount() {
        return repositoryCount;
    }

    @JsonProperty("repositoryCount")
    public void setRepositoryCount(Integer repositoryCount) {
        this.repositoryCount = repositoryCount;
    }

    @JsonProperty("pageInfo")
    public PageInfo getPageInfo() {
        return pageInfo;
    }

    @JsonProperty("pageInfo")
    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    @JsonProperty("repositories")
    public List<Repository> getRepositories() {
        return repositories;
    }

    @JsonProperty("repositories")
    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

}
