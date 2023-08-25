
package com.troxal;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "target"
})
@Generated("jsonschema2pojo")
public class MainBranch {

    @JsonProperty("target")
    private Target target;

    @JsonProperty("target")
    public Target getTarget() {
        return target;
    }

    @JsonProperty("target")
    public void setTarget(Target target) {
        this.target = target;
    }

}
