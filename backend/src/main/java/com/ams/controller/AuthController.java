package com.ams.controller;

import com.ams.dto.LoginRequest;
import com.ams.dto.LoginResponse;
import com.ams.dto.ForgotPasswordVerifyRequest;
import com.ams.dto.PasswordResetRequest;
import com.ams.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<java.util.Map<String, String>> verifyPasswordRecovery(@Valid @RequestBody ForgotPasswordVerifyRequest request) {
        return ResponseEntity.ok(java.util.Map.of("resetToken", authService.verifyPasswordRecovery(request)));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
