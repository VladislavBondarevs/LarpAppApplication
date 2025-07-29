package de.larphelden.larp_app.repositories;

import de.larphelden.larp_app.dto.UserDto;
import de.larphelden.larp_app.models.Event;
import de.larphelden.larp_app.models.Organisation;
import de.larphelden.larp_app.models.Unit;
import de.larphelden.larp_app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
    List<User> findByOrganisation(Organisation organisation);
    List<User> findByOrganisationId(Long organisationId);
    @Query("SELECT u FROM User u WHERE u.username LIKE %:query%")
    List<User> findByUsernameContaining(@Param("query") String query);

    User save(UserDto userDto);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByEventId(Long eventId);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.event = null WHERE u.id = :userId")
    void removeUserFromEvent(@Param("userId") Long userId);

    @Query("SELECT e FROM Event e JOIN FETCH e.users WHERE e.id = :id")
    Optional<Event> findEventWithUsersById(@Param("id") Long id);

}