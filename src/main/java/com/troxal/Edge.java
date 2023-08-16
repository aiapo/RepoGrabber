
package com.troxal;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "node"
})
@Generated("jsonschema2pojo")
public class Edge {

    @JsonProperty("node")
    private Node node;

    @JsonProperty("node")
    public Node getNode() {
        return node;
    }

    @JsonProperty("node")
    public void setNode(Node node) {
        this.node = node;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        //sb.append(Edge.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        //sb.append("node");
        //sb.append('=');
        sb.append(((this.node == null)?"<null>":this.node));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
