package com.ams.service;

import com.ams.dto.RegisterAdminRequest;
import com.ams.dto.RegisterUserRequest;
import com.ams.entity.User;
import com.ams.exception.DuplicateResourceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplTest {

    @Autowired private UserService userService;
    @Autowired private PasswordEncoder passwordEncoder;

    private RegisterUserRequest buildUserRequest(String userName) {
        RegisterUserRequest r = new RegisterUserRequest();
        r.setUserName(userName);
        r.setPassword("Passw0rd!");
        r.setCustomerCategory("REGULAR");
        r.setPhone("9876543210");
        r.setEmailId(userName + "@example.com");
        r.setAddress1("Address 1");
        r.setCity("Chennai");
        r.setState("Tamil Nadu");
        r.setZipCode("600001");
        r.setDob(LocalDate.of(2000, 1, 1));
        return r;
    }

    @Test
    void registerUser_success_forcesCustomerRole() {
        User user = userService.registerUser(buildUserRequest("johndoe"));
        assertEquals("CUSTOMER", user.getRole());
        assertTrue(passwordEncoder.matches("Passw0rd!", user.getPassword()));
    }

    @Test
    void registerUser_duplicateUsername_throwsException() {
        userService.registerUser(buildUserRequest("janedoe"));
        assertThrows(DuplicateResourceException.class, () -> userService.registerUser(buildUserRequest("janedoe")));
    }

    @Test
    void registerUser_duplicateUsernameCaseInsensitive_throwsException() {
        userService.registerUser(buildUserRequest("caseuser"));
        RegisterUserRequest dup = buildUserRequest("CASEUSER");
        dup.setEmailId("different@example.com");
        assertThrows(DuplicateResourceException.class, () -> userService.registerUser(dup));
    }

    @Test
    void requestAdminAccess_createsPendingAdmin_notActive() {
        RegisterAdminRequest request = new RegisterAdminRequest();
        request.setUserName("pendingadmin");
        request.setPassword("Passw0rd!");
        request.setEmailId("pendingadmin@example.com");
        request.setPhone("9876543210");

        User admin = userService.requestAdminAccess(request);

        assertEquals("ADMIN", admin.getRole());
        assertEquals("PENDING", admin.getAdminStatus());
    }
}
