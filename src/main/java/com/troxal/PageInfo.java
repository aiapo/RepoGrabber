
package com.troxal;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "endCursor",
    "startCursor"
})
@Generated("jsonschema2pojo")
public class PageInfo {

    @JsonProperty("endCursor")
    private String endCursor;
    @JsonProperty("startCursor")
    private String startCursor;

    @JsonProperty("endCursor")
    public String getEndCursor() {
        return endCursor;
    }

    @JsonProperty("endCursor")
    public void setEndCursor(String endCursor) {
        this.endCursor = endCursor;
    }

    @JsonProperty("startCursor")
    public String getStartCursor() {
        return startCursor;
    }

    @JsonProperty("startCursor")
    public void setStartCursor(String startCursor) {
        this.startCursor = startCursor;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(PageInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("endCursor");
        sb.append('=');
        sb.append(((this.endCursor == null)?"<null>":this.endCursor));
        sb.append(',');
        sb.append("startCursor");
        sb.append('=');
        sb.append(((this.startCursor == null)?"<null>":this.startCursor));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
