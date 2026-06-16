package com.fcnami.backend.Service;

import com.fcnami.backend.Model.AdminUser;
import com.fcnami.backend.Repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * CustomUserDetailsService implements UserDetailsService to load user-specific data.
 * It is used by Spring Security to authenticate administrators.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    /**
     * Locates the user based on the email.
     *
     * Precondition: An email string is provided.
     * Postcondition: Returns a UserDetails object if the user is found.
     * Side-effect: Throws UsernameNotFoundException if the user is not found.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AdminUser adminUser = adminUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found with email: " + email));

        return User.builder()
                .username(adminUser.getEmail())
                .password(adminUser.getPasswordHash())
                .roles(adminUser.getRole())
                .build();
    }
}
