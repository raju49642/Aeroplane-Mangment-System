package com.ams.controller;

import com.ams.dto.RegisterAdminRequest;
import com.ams.entity.User;
import com.ams.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Public endpoint: submits a request to become an ADMIN. This creates a
     * PENDING account with NO admin privileges until a SUPER_ADMIN approves it
     * (see SuperAdminController).
     */
    @PostMapping("/register-request")
    public ResponseEntity<User> requestAdminAccess(@Valid @RequestBody RegisterAdminRequest request) {
        User pendingAdmin = userService.requestAdminAccess(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pendingAdmin);
    }
}
