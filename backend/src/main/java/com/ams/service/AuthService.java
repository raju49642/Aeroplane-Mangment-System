package com.ams.service;

import com.ams.dto.LoginRequest;
import com.ams.dto.LoginResponse;
import com.ams.dto.ForgotPasswordVerifyRequest;
import com.ams.dto.PasswordResetRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);
    String verifyPasswordRecovery(ForgotPasswordVerifyRequest request);
    void resetPassword(PasswordResetRequest request);
}
