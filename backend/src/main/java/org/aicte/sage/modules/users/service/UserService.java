package org.aicte.sage.modules.users.service;

import org.aicte.sage.common.api.PageResponse;
import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.ResourceNotFoundException;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.institutions.entity.InstitutionUser;
import org.aicte.sage.modules.institutions.repository.InstitutionRepository;
import org.aicte.sage.modules.institutions.repository.InstitutionUserRepository;
import org.aicte.sage.modules.users.dto.*;
import org.aicte.sage.modules.users.entity.Role;
import org.aicte.sage.modules.users.entity.User;
import org.aicte.sage.modules.users.repository.RoleRepository;
import org.aicte.sage.modules.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InstitutionUserRepository institutionUserRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       InstitutionUserRepository institutionUserRepository,
                       InstitutionRepository institutionRepository,
                       PasswordEncoder passwordEncoder,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.institutionUserRepository = institutionUserRepository;
        this.institutionRepository = institutionRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserDto> getUsersPaginated(String query, Boolean active, Pageable pageable) {
        Page<User> page = userRepository.searchUsers(query, active, pageable);
        List<UserDto> dtos = page.getContent().stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
        return PageResponse.from(page, dtos);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toUserDto(user);
    }

    public UserDto createUser(CreateUserRequest request, UserPrincipal principal, String clientIp) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already registered");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email address '" + request.getEmail() + "' is already registered");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName()
        );
        user.setActive(true);

        Set<Role> roles = new HashSet<>();
        for (String roleCode : request.getRoles()) {
            Role role = roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown security role: " + roleCode));
            roles.add(role);
        }
        user.setRoles(roles);

        User saved = userRepository.save(user);

        // Optional institutional affiliation
        if (request.getInstitutionId() != null) {
            if (institutionRepository.existsById(request.getInstitutionId())) {
                InstitutionUser iu = new InstitutionUser(
                        request.getInstitutionId(),
                        saved.getId(),
                        "INSTITUTION_REPRESENTATIVE",
                        true
                );
                institutionUserRepository.save(iu);
            }
        }

        auditService.recordEvent(
                "USER_CREATED",
                principal.getUsername(),
                request.getInstitutionId(),
                "USER",
                saved.getId().toString(),
                "CREATE",
                clientIp,
                "New user registered: " + saved.getUsername() + " (" + saved.getFullName() + ")"
        );

        return toUserDto(saved);
    }

    public UserDto updateUser(UUID id, UpdateUserRequest request, UserPrincipal principal, String clientIp) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered by another account");
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        User updated = userRepository.save(user);

        auditService.recordEvent(
                "USER_UPDATED",
                principal.getUsername(),
                null,
                "USER",
                updated.getId().toString(),
                "UPDATE",
                clientIp,
                "User profile modified: " + updated.getUsername()
        );

        return toUserDto(updated);
    }

    public UserDto setUserStatus(UUID id, UserStatusRequest request, UserPrincipal principal, String clientIp) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (principal.getId().equals(id) && !request.getActive()) {
            throw new IllegalArgumentException("Administrators cannot suspend their own active session account");
        }

        user.setActive(request.getActive());
        User updated = userRepository.save(user);

        auditService.recordEvent(
                "USER_STATUS_CHANGED",
                principal.getUsername(),
                null,
                "USER",
                updated.getId().toString(),
                request.getActive() ? "ACTIVATE" : "SUSPEND",
                clientIp,
                "User status set to " + (request.getActive() ? "ACTIVE" : "SUSPENDED") +
                        (request.getReason() != null ? " Reason: " + request.getReason() : "")
        );

        return toUserDto(updated);
    }

    public UserDto assignRoles(UUID id, AssignRolesRequest request, UserPrincipal principal, String clientIp) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        Set<Role> roles = new HashSet<>();
        for (String roleCode : request.getRoles()) {
            Role role = roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown security role: " + roleCode));
            roles.add(role);
        }
        user.setRoles(roles);
        User updated = userRepository.save(user);

        auditService.recordEvent(
                "ROLE_ASSIGNED",
                principal.getUsername(),
                null,
                "USER",
                updated.getId().toString(),
                "ASSIGN_ROLES",
                clientIp,
                "Assigned roles: " + String.join(", ", request.getRoles())
        );

        return toUserDto(updated);
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    private UserDto toUserDto(User user) {
        List<UUID> institutionIds = institutionUserRepository.findAllByUserId(user.getId())
                .stream()
                .map(InstitutionUser::getInstitutionId)
                .collect(Collectors.toList());
        return UserDto.fromEntity(user, institutionIds);
    }
}
