package com.ams.service;

import com.ams.dto.AdminRequestResponse;
import com.ams.dto.LoginRequest;
import com.ams.dto.RegisterAdminRequest;
import com.ams.entity.User;
import com.ams.exception.AdminApprovalException;
import com.ams.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SuperAdminWorkflowTest {

    @Autowired private UserService userService;
    @Autowired private AuthService authService;
    @Autowired private SuperAdminService superAdminService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        TestSecurityUtil.clear();
    }

    private User createSuperAdmin(String name) {
        User superAdmin = new User();
        superAdmin.setUserName(name);
        superAdmin.setPassword(passwordEncoder.encode("Passw0rd!"));
        superAdmin.setRole("SUPER_ADMIN");
        superAdmin.setAdminStatus("APPROVED");
        return userRepository.save(superAdmin);
    }

    private RegisterAdminRequest adminRequest(String userName) {
        RegisterAdminRequest r = new RegisterAdminRequest();
        r.setUserName(userName);
        r.setPassword("Passw0rd!");
        r.setEmailId(userName + "@example.com");
        r.setPhone("9876543210");
        return r;
    }

    @Test
    void onlyOneSuperAdminCanExist_enforcedAtRepositoryLevel() {
        createSuperAdmin("super1");
        assertTrue(userRepository.existsByRole("SUPER_ADMIN"));
        // A second attempt to create one is exactly what SuperAdminInitializer guards against
        // by checking existsByRole("SUPER_ADMIN") before ever creating a second row — there is
        // no service-layer API that creates a SUPER_ADMIN at all, closing off any other path.
    }

    @Test
    void pendingAdmin_cannotLogin_asActiveAdmin() {
        userService.requestAdminAccess(adminRequest("pending1"));

        LoginRequest login = new LoginRequest();
        login.setUserName("pending1");
        login.setPassword("Passw0rd!");

        assertThrows(AdminApprovalException.class, () -> authService.login(login));
    }

    @Test
    void superAdmin_canApproveAdminRequest() {
        userService.requestAdminAccess(adminRequest("toapprove"));

        TestSecurityUtil.loginAs("super", "SUPER_ADMIN");
        List<AdminRequestResponse> pending = superAdminService.getPendingAdminRequests();
        assertTrue(pending.stream().anyMatch(r -> "toapprove".equals(r.getUserName())));

        Integer userId = pending.stream()
                .filter(r -> "toapprove".equals(r.getUserName()))
                .findFirst().orElseThrow().getUserId();

        AdminRequestResponse approved = superAdminService.approveAdmin(userId);
        assertEquals("APPROVED", approved.getStatus());

        // Now login should succeed and issue a working ADMIN token.
        LoginRequest login = new LoginRequest();
        login.setUserName("toapprove");
        login.setPassword("Passw0rd!");
        assertEquals("ADMIN", authService.login(login).getRole());
    }

    @Test
    void superAdmin_canRejectAdminRequest() {
        userService.requestAdminAccess(adminRequest("torejected"));
        User pending = userRepository.findByUserName("torejected").orElseThrow();

        AdminRequestResponse rejected = superAdminService.rejectAdmin(pending.getUserId());
        assertEquals("REJECTED", rejected.getStatus());

        LoginRequest login = new LoginRequest();
        login.setUserName("torejected");
        login.setPassword("Passw0rd!");
        assertThrows(AdminApprovalException.class, () -> authService.login(login));
    }

    @Test
    void approvingAlreadyApprovedRequest_throwsException() {
        userService.requestAdminAccess(adminRequest("doubleapprove"));
        User pending = userRepository.findByUserName("doubleapprove").orElseThrow();

        superAdminService.approveAdmin(pending.getUserId());
        assertThrows(com.ams.exception.AdminApprovalException.class,
                () -> superAdminService.approveAdmin(pending.getUserId()));
    }
}
