package com.ams.dto;

import com.ams.validation.MinimumAge;
import com.ams.validation.AllowedEmailDomain;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class RegisterUserRequest {

    @NotBlank(message = "userName is required")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{3,29}$",
            message = "Username must be 4-30 characters, start with a letter, and contain only letters, numbers, and underscores")
    private String userName;

    @NotBlank(message = "password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number, and one special character.")
    private String password;

    @NotBlank(message = "customerCategory is required")
    @Pattern(regexp = "REGULAR|SILVER|GOLD|PLATINUM", message = "customerCategory must be one of REGULAR, SILVER, GOLD, PLATINUM")
    private String customerCategory;

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9")
    private String phone;

    @NotBlank(message = "emailId is required")
    @Email(message = "Enter a valid email address")
    @AllowedEmailDomain(message = "Email domain is not in the allowed provider list")
    @Size(max = 100, message = "emailId must not exceed 100 characters")
    private String emailId;

    @NotBlank(message = "address1 is required")
    @Size(min = 5, max = 100, message = "address1 must be between 5 and 100 characters")
    private String address1;

    @Size(max = 100, message = "address2 must not exceed 100 characters")
    private String address2;

    @NotBlank(message = "city is required")
    @Size(min = 2, max = 50, message = "city must be between 2 and 50 characters")
    private String city;

    @NotBlank(message = "state is required")
    @Size(min = 2, max = 50, message = "state must be between 2 and 50 characters")
    private String state;

    @NotBlank(message = "zipCode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Enter a valid 6-digit Indian ZIP code")
    private String zipCode;

    @NotNull(message = "dob is required")
    @Past(message = "dob must be in the past")
    @MinimumAge(value = 12, maxValue = 75, message = "You must be between 12 and 75 years old to register")
    private LocalDate dob;

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

    public String getCustomerCategory() { return customerCategory; }
    public void setCustomerCategory(String customerCategory) { this.customerCategory = customerCategory; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmailId() { return emailId; }
    public void setEmailId(String emailId) { this.emailId = emailId; }

    public String getAddress1() { return address1; }
    public void setAddress1(String address1) { this.address1 = address1; }

    public String getAddress2() { return address2; }
    public void setAddress2(String address2) { this.address2 = address2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public String getFavouriteSport() { return favouriteSport; }
    public void setFavouriteSport(String favouriteSport) { this.favouriteSport = favouriteSport; }
    public String getFavouriteHobby() { return favouriteHobby; }
    public void setFavouriteHobby(String favouriteHobby) { this.favouriteHobby = favouriteHobby; }
}
