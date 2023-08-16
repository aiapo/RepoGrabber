
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
    @JsonProperty("hasNextPage")
    private Boolean hasNextPage;

    @JsonProperty("endCursor")
    public String getEndCursor() {
        return endCursor;
    }

    @JsonProperty("endCursor")
    public void setEndCursor(String endCursor) {
        this.endCursor = endCursor;
    }

    @JsonProperty("hasNextPage")
    public Boolean gethasNextPage() {
        return hasNextPage;
    }

    @JsonProperty("hasNextPage")
    public void setHasNextPage(Boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(PageInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("endCursor");
        sb.append('=');
        sb.append(((this.endCursor == null)?"<null>":this.endCursor));
        sb.append(',');
        sb.append("hasNextPage");
        sb.append('=');
        sb.append(((this.hasNextPage == null)?"<null>":this.hasNextPage));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
