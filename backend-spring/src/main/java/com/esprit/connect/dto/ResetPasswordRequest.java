package com.esprit.connect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResetPasswordRequest {

    private String uid;
    private String token;
    private String password;

    @JsonProperty("password_confirm")
    private String passwordConfirm;

    public ResetPasswordRequest() {}

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPasswordConfirm() { return passwordConfirm; }
    public void setPasswordConfirm(String passwordConfirm) { this.passwordConfirm = passwordConfirm; }
}
