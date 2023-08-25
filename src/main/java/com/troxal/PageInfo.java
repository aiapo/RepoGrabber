package com.troxal;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "endCursor",
        "hasNextPage"
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
    public Boolean getHasNextPage() {
        return hasNextPage;
    }

    @JsonProperty("hasNextPage")
    public void setHasNextPage(Boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

}