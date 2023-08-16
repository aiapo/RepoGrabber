
package com.troxal;

import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "edges",
    "totalSize"
})
@Generated("jsonschema2pojo")
public class Languages {

    @JsonProperty("edges")
    private List<Edge__1> edges;
    @JsonProperty("totalSize")
    private Integer totalSize;

    @JsonProperty("edges")
    public List<Edge__1> getEdges() {
        return edges;
    }

    @JsonProperty("edges")
    public void setEdges(List<Edge__1> edges) {
        this.edges = edges;
    }

    @JsonProperty("totalSize")
    public Integer getTotalSize() {
        return totalSize;
    }

    @JsonProperty("totalSize")
    public void setTotalSize(Integer totalSize) {
        this.totalSize = totalSize;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Languages.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("edges");
        sb.append('=');
        sb.append(((this.edges == null)?"<null>":this.edges));
        sb.append(',');
        sb.append("totalSize");
        sb.append('=');
        sb.append(((this.totalSize == null)?"<null>":this.totalSize));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
