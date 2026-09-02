package com.ams.service;

import com.ams.dto.AdminRequestResponse;

import java.util.List;

public interface SuperAdminService {

    List<AdminRequestResponse> getPendingAdminRequests();

    AdminRequestResponse approveAdmin(Integer userId);

    AdminRequestResponse rejectAdmin(Integer userId);
}
