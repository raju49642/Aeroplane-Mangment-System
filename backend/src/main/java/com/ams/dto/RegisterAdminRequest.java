package com.ams.dto;

import com.ams.validation.AllowedEmailDomain;
import jakarta.validation.constraints.*;

/**
 * Public admin *request* DTO (not immediate admin creation).
 * Submitting this creates a PENDING admin account that has no privileges
 * until a SUPER_ADMIN approves it.
 */
public class RegisterAdminRequest {

    @NotBlank(message = "userName is required")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{3,29}$",
            message = "Username must be 4-30 characters, start with a letter, and contain only letters, numbers, and underscores")
    private String userName;

    @NotBlank(message = "password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number, and one special character.")
    private String password;

    @NotBlank(message = "emailId is required")
    @jakarta.validation.constraints.Email(message = "Enter a valid email address")
    @AllowedEmailDomain(message = "Email domain is not in the allowed provider list")
    private String emailId;

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9")
    private String phone;

    @NotBlank(message = "favouriteSport is required")
    @Size(min = 2, max = 50, message = "favouriteSport must be 2-50 characters")
    private String favouriteSport;
    @NotBlank(message = "favouriteHobby is required")
    @Size(min = 2, max = 50, message = "favouriteHobby must be 2-50 characters")
    private String favouriteHobby;

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmailId() { return emailId; }
    public void setEmailId(String emailId) { this.emailId = emailId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getFavouriteSport() { return favouriteSport; }
    public void setFavouriteSport(String favouriteSport) { this.favouriteSport = favouriteSport; }
    public String getFavouriteHobby() { return favouriteHobby; }
    public void setFavouriteHobby(String favouriteHobby) { this.favouriteHobby = favouriteHobby; }
}
