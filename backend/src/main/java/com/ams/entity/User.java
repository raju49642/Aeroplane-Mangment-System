package com.ams.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "user_name", nullable = false, unique = true)
    private String userName;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    // SUPER_ADMIN | ADMIN | CUSTOMER
    @Column(name = "role", nullable = false)
    private String role;

    // Only meaningful for role=ADMIN: PENDING | APPROVED | REJECTED.
    // CUSTOMER and SUPER_ADMIN are always considered "approved"/active.
    @Column(name = "admin_status")
    private String adminStatus;

    @Column(name = "customer_category")
    private String customerCategory;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email_id")
    private String emailId;

    @Column(name = "address1")
    private String address1;

    @Column(name = "address2")
    private String address2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "dob")
    private LocalDate dob;

    @JsonIgnore
    @Column(name = "favourite_sport", length = 100)
    private String favouriteSport;

    @JsonIgnore
    @Column(name = "favourite_hobby", length = 100)
    private String favouriteHobby;

    public User() {
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAdminStatus() { return adminStatus; }
    public void setAdminStatus(String adminStatus) { this.adminStatus = adminStatus; }

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
