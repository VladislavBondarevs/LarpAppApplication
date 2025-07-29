package de.larphelden.larp_app.impl;

import de.larphelden.larp_app.dto.OrganisationDto;
import de.larphelden.larp_app.models.User;
import de.larphelden.larp_app.repositories.OrganisationRepository;
import de.larphelden.larp_app.models.Organisation;
import de.larphelden.larp_app.repositories.UserRepository;
import de.larphelden.larp_app.services.OrganisationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganisationServiceImpl implements OrganisationService {

    Logger logger = LoggerFactory.getLogger(OrganisationServiceImpl.class);

    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;

    public OrganisationServiceImpl(OrganisationRepository organisationRepository, UserRepository userRepository) {
        this.organisationRepository = organisationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public Organisation registerOrganisation(OrganisationDto organisationDto, String username) {
        Organisation organisation = new Organisation();
        organisation.setName(organisationDto.getName());
        String beschreibung = organisationDto.getBeschreibung();
        if (beschreibung != null && beschreibung.length() > 255) {
            beschreibung = beschreibung.substring(0, 255);
        }
        organisation.setBeschreibung(beschreibung);
        organisation.setPhotoPath(organisationDto.getPhotoPath());
        organisation.setKontakt(organisationDto.getKontakt());
        organisation.setSocialmedia(organisationDto.getSocialmedia());
        organisation.setStatus("Pending");
        organisation.setApproved(false);

        Organisation savedOrganisation = organisationRepository.save(organisation);

        return savedOrganisation;
    }

    @Override
    public Organisation save(Organisation organisation) {
        return organisationRepository.save(organisation);
    }

    @Override
    public Organisation findById(Long id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organisation mit ID " + id + " nicht gefunden"));
    }

    @Override
    public List<Organisation> findAll() {
        return organisationRepository.findAll();
    }

    @Override
    public List<OrganisationDto> getPendingRequests() {
        List<Organisation> pendingOrganisations = organisationRepository.findByApproved(false);
        return pendingOrganisations.stream()
                .map(org -> new OrganisationDto(
                        org.getId(),
                        org.getName(),
                        org.getBeschreibung(),
                        org.getPhotoPath(),
                        org.getSocialmedia(),
                        org.getKontakt(),
                        org.getStatus()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrganisationDto> findAllPendingRequests() {
        List<Organisation> pendingOrganisations = organisationRepository.findByStatus("Pending");
        return pendingOrganisations.stream()
                .map(org -> new OrganisationDto(
                        org.getId(),
                        org.getName(),
                        org.getBeschreibung(),
                        org.getPhotoPath(),
                        org.getSocialmedia(),
                        org.getKontakt(),
                        org.getStatus()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public Organisation approveRequest(Long id) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found with ID: " + id));

        logger.debug("Approving organisation: {}", organisation.getName());

        List<User> users = organisation.getUsers();
        if (users.isEmpty()) {
            throw new IllegalArgumentException("No users associated with the organisation: " + organisation.getName());
        }

        for (User user : users) {
            user.setRole("ROLE_ORGANISATOR");
            userRepository.save(user);
            logger.info("Assigned ROLE_ORGANISATOR to user: {}", user.getUsername());
        }

        organisation.setApproved(true);
        organisation.setStatus("Approved");
        organisationRepository.save(organisation);

        logger.info("Organisation '{}' approved successfully", organisation.getName());

        return organisation;
    }

    @Override
    @Transactional
    public void deleteOrganisation(Long id) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organisation mit ID " + id + " nicht gefunden."));

        List<User> users = userRepository.findByOrganisationId(id);
        for (User user : users) {
            user.setOrganisation(null);
            userRepository.save(user);
        }

        organisationRepository.delete(organisation);
    }
}
