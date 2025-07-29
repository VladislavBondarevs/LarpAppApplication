package de.larphelden.larp_app.services;

import de.larphelden.larp_app.dto.OrganisationDto;
import de.larphelden.larp_app.models.Organisation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrganisationService {
    Organisation registerOrganisation(OrganisationDto organisationDto, String username);
    Organisation findById(Long id);
    List<Organisation> findAll();
    void deleteOrganisation(Long organisationId);
    Organisation approveRequest(Long id);
    List<OrganisationDto> getPendingRequests();
    List<OrganisationDto> findAllPendingRequests();
    Organisation save(Organisation organisation);

}


