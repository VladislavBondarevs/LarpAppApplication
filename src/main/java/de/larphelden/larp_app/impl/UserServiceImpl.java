package de.larphelden.larp_app.impl;

import de.larphelden.larp_app.dto.UserDto;
import de.larphelden.larp_app.models.Organisation;
import de.larphelden.larp_app.models.User;
import de.larphelden.larp_app.repositories.OrganisationRepository;
import de.larphelden.larp_app.repositories.UserRepository;
import de.larphelden.larp_app.services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganisationRepository organisationRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, OrganisationRepository organisationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organisationRepository = organisationRepository;
    }

    @Override
    public void registerUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());

        String role = userDto.getRole();
        if (role == null || role.isEmpty()) {
            user.setRole("ROLE_USER");
        } else {

            if (!List.of("ROLE_USER", "ROLE_ADMIN", "ROLE_ORGANISATOR").contains(role)) {
                throw new IllegalArgumentException("Invalid role: " + role);
            }
            user.setRole(role);
        }
        user.setPhotoPath(userDto.getPhotoPath());
        userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer mit der ID " + id + " ist nicht gefunden"));
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Benutzer mit der ID " + id + " ist nicht gefunden");
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void updateUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(newRole);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }


    @Transactional
    @Override
    public void grantRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (!role.equals(user.getRole())) {
            user.setRole(role);
            userRepository.save(user);
            System.out.println("User role updated to " + role + " for user with ID: " + userId);
        } else {
            System.out.println("User already has " + role + " for user with ID: " + userId);
        }
    }

    @Override
    public void removeOrganisationFromUsers(Long organisationId) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found with ID: " + organisationId));
        List<User> users = userRepository.findByOrganisation(organisation);
        for (User user : users) {
            user.setOrganisation(null);
            userRepository.save(user);
        }
    }
    public List<User> findByNameContaining(String query) {
        return userRepository.findByUsernameContaining(query);
    }
    @Override
    public User save(User user) {
       return userRepository.save(user);
    }
}

