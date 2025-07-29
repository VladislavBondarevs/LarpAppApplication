package de.larphelden.larp_app.services;

import de.larphelden.larp_app.dto.LocationDto;
import de.larphelden.larp_app.dto.OrganisationDto;
import de.larphelden.larp_app.models.Location;

import java.util.List;

public interface LocationService {

    List<Location> getAllLocations();
    Location getLocationById(Long id);
    Location createLocation(Location location);
    Location updateLocation(Long id, LocationDto locationDto);
    void deleteLocation(Long id);
    List<LocationDto> findPendingRequests();
    void approveRequest(Long id);
    void rejectRequest(Long id);

}
