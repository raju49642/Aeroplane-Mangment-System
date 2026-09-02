package com.ams.service.impl;

import com.ams.dto.RegisterAdminRequest;
import com.ams.dto.RegisterUserRequest;
import com.ams.entity.User;
import com.ams.exception.DuplicateResourceException;
import com.ams.repository.UserRepository;
import com.ams.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerUser(RegisterUserRequest request) {
        if (userRepository.existsByUserNameIgnoreCase(request.getUserName())) {
            throw new DuplicateResourceException("Username '" + request.getUserName() + "' already exists");
        }
        if (userRepository.existsByEmailIdIgnoreCase(request.getEmailId())) {
            throw new DuplicateResourceException("An account with email '" + request.getEmailId() + "' already exists");
        }

        User user = new User();
        user.setUserName(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Role is always forced to CUSTOMER for self-registration; the request has no role field at all.
        user.setRole("CUSTOMER");
        user.setCustomerCategory(request.getCustomerCategory());
        user.setPhone(request.getPhone());
        user.setEmailId(request.getEmailId());
        user.setAddress1(request.getAddress1());
        user.setAddress2(request.getAddress2());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setZipCode(request.getZipCode());
        user.setDob(request.getDob());
        user.setFavouriteSport(passwordEncoder.encode(normalizeAnswer(request.getFavouriteSport())));
        user.setFavouriteHobby(passwordEncoder.encode(normalizeAnswer(request.getFavouriteHobby())));

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User requestAdminAccess(RegisterAdminRequest request) {
        if (userRepository.existsByUserNameIgnoreCase(request.getUserName())) {
            throw new DuplicateResourceException("Username '" + request.getUserName() + "' already exists");
        }
        if (userRepository.existsByEmailIdIgnoreCase(request.getEmailId())) {
            throw new DuplicateResourceException("An account with email '" + request.getEmailId() + "' already exists");
        }

        User pendingAdmin = new User();
        pendingAdmin.setUserName(request.getUserName());
        pendingAdmin.setPassword(passwordEncoder.encode(request.getPassword()));
        pendingAdmin.setEmailId(request.getEmailId());
        pendingAdmin.setPhone(request.getPhone());
        pendingAdmin.setFavouriteSport(passwordEncoder.encode(normalizeAnswer(request.getFavouriteSport())));
        pendingAdmin.setFavouriteHobby(passwordEncoder.encode(normalizeAnswer(request.getFavouriteHobby())));
        // Role is set to ADMIN immediately so the account "is" an admin account, but adminStatus=PENDING
        // means SecurityConfig/@PreAuthorize checks for ADMIN privileges must also verify
        // adminStatus=APPROVED before granting access (see AdminApprovalGuard usage in controllers).
        pendingAdmin.setRole("ADMIN");
        pendingAdmin.setAdminStatus("PENDING");

        return userRepository.save(pendingAdmin);
    }

    private String normalizeAnswer(String value) {
        return value == null ? "not-provided" : value.trim().toLowerCase(Locale.ROOT);
    }
}
