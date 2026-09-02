package com.ams.service.impl;

import com.ams.config.JwtService;
import com.ams.dto.LoginRequest;
import com.ams.dto.LoginResponse;
import com.ams.dto.ForgotPasswordVerifyRequest;
import com.ams.dto.PasswordResetRequest;
import com.ams.entity.User;
import com.ams.exception.AdminApprovalException;
import com.ams.exception.InvalidCredentialsException;
import com.ams.repository.UserRepository;
import com.ams.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // A registered-but-not-yet-approved admin account exists in the users table, but must
        // not be able to log in and obtain a working ADMIN token until a SUPER_ADMIN approves it.
        if ("ADMIN".equals(user.getRole()) && !"APPROVED".equals(user.getAdminStatus())) {
            if ("REJECTED".equals(user.getAdminStatus())) {
                throw new AdminApprovalException("Your admin access request was rejected. Contact the Super Admin for details.");
            }
            throw new AdminApprovalException("Your admin access request is still pending Super Admin approval.");
        }

        String token = jwtService.generateToken(user.getUserName(), user.getRole());

        return new LoginResponse(user.getUserId(), user.getUserName(), user.getRole(), token, "Login successful");
    }

    @Override
    @Transactional(readOnly = true)
    public String verifyPasswordRecovery(ForgotPasswordVerifyRequest request) {
        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or security answers"));
        boolean sportMatches = passwordEncoder.matches(normalize(request.getFavouriteSport()), user.getFavouriteSport());
        boolean hobbyMatches = passwordEncoder.matches(normalize(request.getFavouriteHobby()), user.getFavouriteHobby());
        if (!sportMatches || !hobbyMatches) throw new InvalidCredentialsException("Invalid username or security answers");
        return jwtService.generatePasswordResetToken(user.getUserName());
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        if (!jwtService.isPasswordResetTokenValid(request.getResetToken())) {
            throw new InvalidCredentialsException("Invalid or expired password reset token");
        }
        User user = userRepository.findByUserName(jwtService.extractUsername(request.getResetToken()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired password reset token"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private String normalize(String value) { return value == null ? "" : value.trim().toLowerCase(Locale.ROOT); }
}
