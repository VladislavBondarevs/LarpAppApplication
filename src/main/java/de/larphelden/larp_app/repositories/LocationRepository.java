package de.larphelden.larp_app.repositories;

import de.larphelden.larp_app.models.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByStatus(String status);
}
