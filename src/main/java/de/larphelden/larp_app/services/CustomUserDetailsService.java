package de.larphelden.larp_app.services;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.larphelden.larp_app.models.User;
import de.larphelden.larp_app.repositories.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Username or Password not found");
        }

        return new CustomUserDetails(
                user.getUsername(),
                user.getPassword(),
                authorities(user)
        );
    }

    public Collection<? extends GrantedAuthority> authorities(User user) {
        String role = user.getRole().toUpperCase();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

}