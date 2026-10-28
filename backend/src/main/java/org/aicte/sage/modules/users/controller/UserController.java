package org.aicte.sage.modules.users.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.api.PageResponse;
import org.aicte.sage.common.security.SecurityUtils;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.users.dto.*;
import org.aicte.sage.modules.users.entity.Role;
import org.aicte.sage.modules.users.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Administrative User and Security Role Management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('ROLE_AICTE_ADMIN')")
    @Operation(summary = "List Users", description = "Retrieves paginated administrative and institutional user accounts")
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<UserDto> response = userService.getUsersPaginated(query, active, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('ROLE_AICTE_ADMIN')")
    @Operation(summary = "Get User Details", description = "Retrieves detailed user profile, role bindings, and institutional affiliations")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable UUID id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_WRITE') or hasRole('ROLE_AICTE_ADMIN')")
    @Operation(summary = "Create User Account", description = "Registers a new user with cryptographic password hashing and role assignments")
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            HttpServletRequest servletRequest) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
        UserDto created = userService.createUser(request, principal, servletRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("User created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_WRITE') or hasRole('ROLE_AICTE_ADMIN')")
    @Operation(summary = "Update User Account", description = "Updates user name and contact email with duplicate protection")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            HttpServletRequest servletRequest) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
        UserDto updated = userService.updateUser(id, request, principal, servletRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("User updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_ACTIVATE') or hasRole('ROLE_AICTE_ADMIN')")
    @Operation(summary = "Activate or Suspend User Account", description = "Toggles user active status with audit logging")
    public ResponseEntity<ApiResponse<UserDto>> setUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UserStatusRequest request,
            HttpServletRequest servletRequest) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
        UserDto updated = userService.setUserStatus(id, request, principal, servletRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("User status updated successfully", updated));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('ROLE_ASSIGN') or hasRole('ROLE_AICTE_ADMIN')")
    @Operation(summary = "Assign Security Roles", description = "Updates security roles assigned to the user account")
    public ResponseEntity<ApiResponse<UserDto>> assignRoles(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRolesRequest request,
            HttpServletRequest servletRequest) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
        UserDto updated = userService.assignRoles(id, request, principal, servletRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("User roles updated successfully", updated));
    }

    @GetMapping("/roles")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List Configured Roles", description = "Returns available system roles for user assignment")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        List<Role> roles = userService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.ok(roles));
    }
}
