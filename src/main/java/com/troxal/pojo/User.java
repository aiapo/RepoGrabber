package com.troxal.pojo;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "login"
})
@Generated("jsonschema2pojo")
public class User {
    @JsonProperty("login")
    private String login;

    @JsonProperty("login")
    public String getLogin() {
        return login;
    }
    @JsonProperty("login")
    public void setLogin(String login) {
        this.login = login;
    }

}