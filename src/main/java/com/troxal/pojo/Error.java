
package com.troxal.pojo;

import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "path",
        "locations",
        "message"
})
@Generated("jsonschema2pojo")
public class Error {

    @JsonProperty("type")
    private String type;
    @JsonProperty("path")
    private List<String> path;
    @JsonProperty("locations")
    private List<Location> locations;
    @JsonProperty("message")
    private String message;

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("path")
    public List<String> getPath() {
        return path;
    }

    @JsonProperty("path")
    public void setPath(List<String> path) {
        this.path = path;
    }

    @JsonProperty("locations")
    public List<Location> getLocations() {
        return locations;
    }

    @JsonProperty("locations")
    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

}