
package com.troxal.pojo;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "cost",
    "remaining",
    "resetAt"
})
@Generated("jsonschema2pojo")
public class RateLimit {

    @JsonProperty("cost")
    private Integer cost;
    @JsonProperty("remaining")
    private Integer remaining;
    @JsonProperty("resetAt")
    private String resetAt;

    @JsonProperty("cost")
    public Integer getCost() {
        return cost;
    }

    @JsonProperty("cost")
    public void setCost(Integer cost) {
        this.cost = cost;
    }

    @JsonProperty("remaining")
    public Integer getRemaining() {
        return remaining;
    }

    @JsonProperty("remaining")
    public void setRemaining(Integer remaining) {
        this.remaining = remaining;
    }

    @JsonProperty("resetAt")
    public String getResetAt() {
        return resetAt;
    }

    @JsonProperty("resetAt")
    public void setResetAt(String resetAt) {
        this.resetAt = resetAt;
    }

}
