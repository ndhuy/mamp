package com.microstock.user.web;

import com.microstock.user.service.UserAdminService;
import com.microstock.user.web.dto.ResetPasswordRequest;
import com.microstock.user.web.dto.UpdateRoleRequest;
import com.microstock.user.web.dto.UpdateStatusRequest;
import com.microstock.user.web.dto.UserAdminResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserAdminService service;

    public UserAdminController(UserAdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserAdminResponse> list() {
        return service.list();
    }

    @PatchMapping("/{id}/status")
    public UserAdminResponse setStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
        return service.setStatus(id, request.status());
    }

    @PatchMapping("/{id}/role")
    public UserAdminResponse setRole(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
        return service.setRole(id, request.role());
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable UUID id, @Valid @RequestBody ResetPasswordRequest request) {
        service.resetPassword(id, request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
