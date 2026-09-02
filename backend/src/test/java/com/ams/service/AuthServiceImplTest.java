package com.ams.service;

import com.ams.dto.LoginRequest;
import com.ams.dto.LoginResponse;
import com.ams.dto.RegisterUserRequest;
import com.ams.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceImplTest {

    @Autowired private AuthService authService;
    @Autowired private UserService userService;

    @BeforeEach
    void setUp() {
        RegisterUserRequest r = new RegisterUserRequest();
        r.setUserName("loginuser");
        r.setPassword("Passw0rd!");
        r.setCustomerCategory("REGULAR");
        r.setPhone("9876543210");
        r.setEmailId("loginuser@example.com");
        r.setAddress1("Address 1");
        r.setCity("Chennai");
        r.setState("Tamil Nadu");
        r.setZipCode("600001");
        r.setDob(LocalDate.of(2000, 1, 1));
        userService.registerUser(r);
    }

    @Test
    void login_validCredentials_returnsJwtToken() {
        LoginRequest r = new LoginRequest();
        r.setUserName("loginuser");
        r.setPassword("Passw0rd!");

        LoginResponse response = authService.login(r);

        assertEquals("CUSTOMER", response.getRole());
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isBlank());
    }

    @Test
    void login_invalidPassword_throwsException() {
        LoginRequest r = new LoginRequest();
        r.setUserName("loginuser");
        r.setPassword("WrongPassw0rd!");
        assertThrows(InvalidCredentialsException.class, () -> authService.login(r));
    }

    @Test
    void login_unknownUsername_throwsException() {
        LoginRequest r = new LoginRequest();
        r.setUserName("doesnotexist");
        r.setPassword("Passw0rd!");
        assertThrows(InvalidCredentialsException.class, () -> authService.login(r));
    }
}
