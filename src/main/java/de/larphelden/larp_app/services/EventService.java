package de.larphelden.larp_app.services;

import de.larphelden.larp_app.dto.EventDto;
import de.larphelden.larp_app.models.Event;
import de.larphelden.larp_app.models.User;

import java.util.List;

public interface EventService {

    Event registerEvent(EventDto eventDto, Long organisatorId);
    Event updateEvent(Long id, EventDto eventDto);
    void deleteEvent(Long id);
    Event getEventById(Long id);
    List<Event> getAllEvents();
    List<Event> getEventsByOrganisator(Long organisatorId);
    List<Event> getEventsByLocation(Long locationId);
    List<EventDto> findAllPendingEvents();
    void approveRequest(Long id);
    void registerUserToEvent(String accessCode, Long userId);
    Event save(Event event);


}
