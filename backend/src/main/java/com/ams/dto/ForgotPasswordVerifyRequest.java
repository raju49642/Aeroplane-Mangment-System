package com.ams.dto;

import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordVerifyRequest {
    @NotBlank private String userName;
    @NotBlank private String favouriteSport;
    @NotBlank private String favouriteHobby;
    public String getUserName() { return userName; }
    public void setUserName(String v) { userName = v; }
    public String getFavouriteSport() { return favouriteSport; }
    public void setFavouriteSport(String v) { favouriteSport = v; }
    public String getFavouriteHobby() { return favouriteHobby; }
    public void setFavouriteHobby(String v) { favouriteHobby = v; }
}
