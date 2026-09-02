package com.ams.service;

import com.ams.dto.RegisterAdminRequest;
import com.ams.dto.RegisterUserRequest;
import com.ams.entity.User;

public interface UserService {

    User registerUser(RegisterUserRequest request);

    /**
     * Creates a PENDING admin account. This does NOT grant admin privileges;
     * a SUPER_ADMIN must approve it via SuperAdminService first.
     */
    User requestAdminAccess(RegisterAdminRequest request);
}
