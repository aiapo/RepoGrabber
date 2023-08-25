
package com.troxal;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "data"
})
@Generated("jsonschema2pojo")
public class GitHubJSON {

    @JsonProperty("data")
    private Data data;

    @JsonProperty("data")
    public Data getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(Data data) {
        this.data = data;
    }

   /* @JsonProperty("errors")
    private Errors errors;

    @JsonProperty("errors")
    public Errors getErrors() {
        return errors;
    }

    @JsonProperty("errors")
    public void setErrors(Errors errors) {
        this.errors = errors;
    }*/

}
