package de.larphelden.larp_app.repositories;

import de.larphelden.larp_app.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganisatorId(Long organisatorId);
    List<Event> findByLocationId(Long locationId);
    List<Event> findByStatus(String status);
    Event findByAccessCode(String accessCode);

    @Query("SELECT e FROM Event e JOIN FETCH e.users WHERE e.id = :id")
    Optional<Event> findByIdWithUsers(@Param("id") Long id);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.location")
    List<Event> findAllWithLocations();

}
