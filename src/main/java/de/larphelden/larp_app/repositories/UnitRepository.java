package de.larphelden.larp_app.repositories;

import de.larphelden.larp_app.models.Unit;
import de.larphelden.larp_app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

    List<Unit> findByFraktionId(Long fraktionId);

    boolean existsByUsersContaining(User user);

    @Modifying
    @Query(value = "DELETE FROM events_units WHERE units_id = :unitId", nativeQuery = true)
    void deleteRelatedEvents(@Param("unitId") Long unitId);
}
