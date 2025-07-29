package de.larphelden.larp_app.repositories;

import de.larphelden.larp_app.models.Fraktion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FraktionRepository extends JpaRepository<Fraktion, Long> {

    List<Fraktion> findByEventId(Long eventId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Fraktion f WHERE f.event.id = :eventId")
    void deleteAllByEventId(@Param("eventId") Long eventId);


}
