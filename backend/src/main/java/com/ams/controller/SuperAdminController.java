package com.ams.controller;

import com.ams.dto.AdminRequestResponse;
import com.ams.service.SuperAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    public SuperAdminController(SuperAdminService superAdminService) {
        this.superAdminService = superAdminService;
    }

    @GetMapping("/admin-requests")
    public ResponseEntity<List<AdminRequestResponse>> getPendingAdminRequests() {
        return ResponseEntity.ok(superAdminService.getPendingAdminRequests());
    }

    @PutMapping("/admin-requests/{id}/approve")
    public ResponseEntity<AdminRequestResponse> approve(@PathVariable Integer id) {
        return ResponseEntity.ok(superAdminService.approveAdmin(id));
    }

    @PutMapping("/admin-requests/{id}/reject")
    public ResponseEntity<AdminRequestResponse> reject(@PathVariable Integer id) {
        return ResponseEntity.ok(superAdminService.rejectAdmin(id));
    }
}
