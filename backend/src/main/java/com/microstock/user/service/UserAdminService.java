package com.microstock.user.service;

import com.microstock.common.domain.Role;
import com.microstock.common.domain.UserStatus;
import com.microstock.common.error.ApiException;
import com.microstock.common.security.SecurityUtils;
import com.microstock.security.repository.RefreshTokenRepository;
import com.microstock.user.domain.User;
import com.microstock.user.repository.UserRepository;
import com.microstock.user.web.dto.UserAdminResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Administrator user management (PRD 4.1). Guards prevent an admin from locking themselves out. */
@Service
public class UserAdminService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserAdminResponse> list() {
        return userRepository.findAll(Sort.by("username")).stream().map(UserAdminResponse::from).toList();
    }

    @Transactional
    public UserAdminResponse setStatus(UUID userId, UserStatus status) {
        User user = load(userId);
        if (status == UserStatus.DISABLED && userId.equals(SecurityUtils.currentUserId())) {
            throw ApiException.badRequest("You cannot disable your own account");
        }
        user.setStatus(status);
        if (status == UserStatus.DISABLED) {
            refreshTokenRepository.revokeAllForUser(userId); // force logout of active sessions
        }
        return UserAdminResponse.from(user);
    }

    @Transactional
    public UserAdminResponse setRole(UUID userId, Role role) {
        User user = load(userId);
        if (role != Role.ADMIN && userId.equals(SecurityUtils.currentUserId())) {
            throw ApiException.badRequest("You cannot remove your own administrator role");
        }
        user.setRole(role);
        return UserAdminResponse.from(user);
    }

    @Transactional
    public void resetPassword(UUID userId, String newPassword) {
        User user = load(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        refreshTokenRepository.revokeAllForUser(userId);
    }

    private User load(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
    }
}
