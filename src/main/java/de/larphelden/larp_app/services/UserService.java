package de.larphelden.larp_app.services;

import de.larphelden.larp_app.dto.UserDto;
import de.larphelden.larp_app.models.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

        User findByUsername(String username);
        User findById(Long id);
        void deleteUser(Long id);
        void registerUser(UserDto userDto);
        List<User> getAllUsers();
        void grantRole(Long userId, String role);

        void updateUserRole(Long id, String roleAdmin);
        void deleteUserById(Long id);
        void removeOrganisationFromUsers(Long organisationId);
        List<User> findByNameContaining(String query);
        User save(User user);

}
