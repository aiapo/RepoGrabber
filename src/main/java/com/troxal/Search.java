
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
    "edges"
})
@Generated("jsonschema2pojo")
public class Search {

    @JsonProperty("repositoryCount")
    private Integer repositoryCount;
    @JsonProperty("pageInfo")
    private PageInfo pageInfo;
    @JsonProperty("edges")
    private List<Edge> edges;

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

    @JsonProperty("edges")
    public List<Edge> getEdges() {
        return edges;
    }

    @JsonProperty("edges")
    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Search.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("repositoryCount");
        sb.append('=');
        sb.append(((this.repositoryCount == null)?"<null>":this.repositoryCount));
        sb.append(',');
        sb.append("pageInfo");
        sb.append('=');
        sb.append(((this.pageInfo == null)?"<null>":this.pageInfo));
        sb.append(',');
        sb.append("edges");
        sb.append('=');
        sb.append(((this.edges == null)?"<null>":this.edges));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
