package de.larphelden.larp_app.repositories;

import de.larphelden.larp_app.dto.OrganisationDto;
import de.larphelden.larp_app.models.Organisation;
import de.larphelden.larp_app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    List<Organisation> findByStatus(String status);
    List<Organisation> findByApproved(boolean approved);
    Organisation findByName(String name);

}
