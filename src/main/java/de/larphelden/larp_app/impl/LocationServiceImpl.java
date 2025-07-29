package de.larphelden.larp_app.impl;

import de.larphelden.larp_app.controllers.UserController;
import de.larphelden.larp_app.dto.LocationDto;
import de.larphelden.larp_app.models.Location;
import de.larphelden.larp_app.repositories.LocationRepository;
import de.larphelden.larp_app.services.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    public LocationServiceImpl(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @Override
    public Location getLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + id));
    }

    @Override
    public Location createLocation(Location location) {

        return locationRepository.save(location);
    }

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Override
    public Location updateLocation(Long id, LocationDto locationDto) {
        logger.info("Updating location with ID: {}", id);

        Location location = getLocationById(id);

        location.setName(locationDto.getName());
        location.setBeschreibung(locationDto.getBeschreibung());
        location.setAdresse(locationDto.getAdresse());
        location.setTelefonnummer(locationDto.getTelefonnummer());
        location.setEmail(locationDto.getEmail());
        location.setAnfahrt(locationDto.getAnfahrt());
        location.setUnterkunft(locationDto.getUnterkunft());
        location.setAnzeigen(locationDto.isAnzeigen());

        if (locationDto.getPhotoPath() != null) {
            logger.info("Updating photoPath to: {}", locationDto.getPhotoPath());
            location.setPhotoPath(locationDto.getPhotoPath());
        }

        if (locationDto.getBildergallerie() != null && !locationDto.getBildergallerie().isEmpty()) {
            logger.info("Updating bildergallerie with paths: {}", locationDto.getBildergalleriePaths());
            location.setBildergallerie(locationDto.getBildergalleriePaths());
        }

        Location updatedLocation = locationRepository.save(location);
        logger.info("Location with ID {} updated successfully.", updatedLocation.getId());

        return updatedLocation;
    }


    @Override
    public void deleteLocation(Long id) {
        locationRepository.deleteById(id);
    }

    @Override
    public List<LocationDto> findPendingRequests() {
        List<Location> pendingLocations = locationRepository.findByStatus("PENDING");
        return pendingLocations.stream()
                .map(location -> new LocationDto(
                        location.getId(),
                        location.getName(),
                        location.getBeschreibung(),
                        location.getPhotoPath(),
                        location.getAdresse(),
                        location.getTelefonnummer(),
                        location.getEmail(),
                        location.getAnfahrt(),
                        location.getUnterkunft(),
                        location.isAnzeigen(),
                        location.getStatus()
                ))
                .collect(Collectors.toList());
    }



    @Transactional
    @Override
    public void approveRequest(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + id));
        location.setStatus("Approved");
        location.setApproved(true);
        locationRepository.save(location);
    }

    @Transactional
    @Override
    public void rejectRequest(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + id));
        location.setStatus("Rejected");
        location.setApproved(false);
        locationRepository.save(location);
    }


}
