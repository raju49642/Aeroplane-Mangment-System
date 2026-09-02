package com.ams.service.impl;

import com.ams.dto.AdminRequestResponse;
import com.ams.entity.User;
import com.ams.exception.AdminApprovalException;
import com.ams.exception.ResourceNotFoundException;
import com.ams.repository.UserRepository;
import com.ams.service.SuperAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuperAdminServiceImpl implements SuperAdminService {

    private final UserRepository userRepository;

    public SuperAdminServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRequestResponse> getPendingAdminRequests() {
        return userRepository.findByRoleAndAdminStatus("ADMIN", "PENDING").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminRequestResponse approveAdmin(Integer userId) {
        User user = findPendingAdmin(userId);
        user.setAdminStatus("APPROVED");
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public AdminRequestResponse rejectAdmin(Integer userId) {
        User user = findPendingAdmin(userId);
        user.setAdminStatus("REJECTED");
        return toResponse(userRepository.save(user));
    }

    private User findPendingAdmin(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));

        if (!"ADMIN".equals(user.getRole())) {
            throw new AdminApprovalException("User with ID " + userId + " is not an admin request");
        }
        if (!"PENDING".equals(user.getAdminStatus())) {
            throw new AdminApprovalException("Admin request for user ID " + userId + " has already been " + user.getAdminStatus());
        }
        return user;
    }

    private AdminRequestResponse toResponse(User user) {
        AdminRequestResponse response = new AdminRequestResponse();
        response.setUserId(user.getUserId());
        response.setUserName(user.getUserName());
        response.setEmailId(user.getEmailId());
        response.setPhone(user.getPhone());
        response.setStatus(user.getAdminStatus());
        return response;
    }
}
