package org.aicte.sage.common.security;

import org.aicte.sage.modules.institutions.entity.InstitutionUser;
import org.aicte.sage.modules.institutions.repository.InstitutionUserRepository;
import org.aicte.sage.modules.users.entity.Role;
import org.aicte.sage.modules.users.entity.User;
import org.aicte.sage.modules.users.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final InstitutionUserRepository institutionUserRepository;

    public CustomUserDetailsService(UserRepository userRepository,
                                    InstitutionUserRepository institutionUserRepository) {
        this.userRepository = userRepository;
        this.institutionUserRepository = institutionUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByIdentifierWithRoles(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + usernameOrEmail));

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.getCode()));
            if (role.getPermissions() != null) {
                role.getPermissions().forEach(perm ->
                        authorities.add(new SimpleGrantedAuthority(perm.getCode())));
            }
        }

        List<InstitutionUser> affiliations = institutionUserRepository.findAllByUserId(user.getId());
        Set<UUID> institutionIds = affiliations.stream()
                .map(InstitutionUser::getInstitutionId)
                .collect(Collectors.toSet());

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFullName(),
                user.isActive(),
                authorities,
                institutionIds
        );
    }
}
