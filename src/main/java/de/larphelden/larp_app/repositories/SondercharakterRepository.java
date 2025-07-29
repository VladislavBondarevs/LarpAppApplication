package de.larphelden.larp_app.repositories;

import de.larphelden.larp_app.models.Sondercharakter;
import de.larphelden.larp_app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SondercharakterRepository extends JpaRepository<Sondercharakter, Long> {

    List<Sondercharakter> findByEventId(Long eventId);

    @Query("SELECT s FROM Sondercharakter s LEFT JOIN FETCH s.requests WHERE s.event.id = :eventId")
    List<Sondercharakter> findAllWithRequestsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT s FROM Sondercharakter s")
    List<Sondercharakter> findPendingRequests();


    @Query("SELECT s FROM Sondercharakter s WHERE s.user.id = :userId")
    List<Sondercharakter> findByUser(@Param("userId") Long userId);

//    List<Sondercharakter> findByUser(Long userId);
}
