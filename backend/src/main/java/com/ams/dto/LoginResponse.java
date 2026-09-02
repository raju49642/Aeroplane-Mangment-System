package com.ams.dto;

public class LoginResponse {

    private Integer userId;
    private String userName;
    private String role;
    private String token;
    private String message;

    public LoginResponse() {}

    public LoginResponse(Integer userId, String userName, String role, String token, String message) {
        this.userId = userId;
        this.userName = userName;
        this.role = role;
        this.token = token;
        this.message = message;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
