package de.larphelden.larp_app.impl;

import de.larphelden.larp_app.dto.EventDto;
import de.larphelden.larp_app.models.*;
import de.larphelden.larp_app.repositories.*;
import de.larphelden.larp_app.services.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);
    private final EventRepository eventRepository;
    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;

    public EventServiceImpl(EventRepository eventRepository, OrganisationRepository organisationRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.organisationRepository = organisationRepository;
        this.userRepository = userRepository;
    }

    @Autowired
    private FraktionRepository fraktionRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private SondercharakterRepository sondercharakterRepository;

    @Override
    public Event save(Event event) {
        return eventRepository.save(event);
    }


    /**
     * Registrierung eines neuen Events
     */
    @Transactional
    @Override
    public Event registerEvent(EventDto eventDto, Long organisatorId) {
        // Organisation anhand der ID suchen
        logger.info("Schritt 1: Organisation mit ID {} abrufen.", organisatorId);
        Organisation organisation = organisationRepository.findById(organisatorId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation mit ID " + organisatorId + " nicht gefunden"));

        // Neues Event-Objekt erstellen
        logger.info("Schritt 2: Neues Event-Objekt erstellen.");
        Event event = new Event();

        return eventRepository.save(event);
    }

    private String generateAccessCode() {
        return UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }

    /**
     * Aktualisierung eines bestehenden Events
     */
    @Transactional
    @Override
    public Event updateEvent(Long id, EventDto eventDto) {
        // Event finden
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event mit ID " + id + " nicht gefunden"));

        // Event-Daten aktualisieren
        event.setBezeichnung(eventDto.getBezeichnung());
        event.setBeschreibung(eventDto.getBeschreibung());

        // Verarbeitung des Hauptbildes
        if (eventDto.getBild() != null && !eventDto.getBild().isEmpty()) {
            String uploadResult = processSingleFile(eventDto.getBild(), "uploads/events/",
                    "Ungültiger Dateityp für das Hauptbild. Nur JPEG, PNG und JFIF sind erlaubt.");
            if (uploadResult != null) {
                event.setPhotoPath(uploadResult);
            } else {
                throw new IllegalArgumentException("Fehler beim Hochladen des Hauptbildes.");
            }
        }

        event.setBildergallerie(eventDto.getBildergalleriePaths());
        event.setDatumVon(eventDto.getDatumVon());
        event.setDatumBis(eventDto.getDatumBis());
        event.setEventAbhangigeAnfahrt(eventDto.getEventAbhangigeAnfahrt());
        event.setEventAbhangigeUnterkunft(eventDto.getEventAbhangigeUnterkunft());
        event.setTyp(eventDto.getTyp());
        event.setZeitspanne(eventDto.getZeitspanne());
        event.setGenre(eventDto.getGenre());
        event.setLocation(eventDto.getLocation());
        event.setPhotoPath(eventDto.getBildPath());

        // Log-Ausgabe
        logger.info("Event aktualisiert: {}", event.getBezeichnung());

        // Event speichern und zurückgeben
        return eventRepository.save(event);
    }

    /**
     * Löschen eines Events
     */
    @Transactional
    @Override
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event mit ID " + eventId + " nicht gefunden"));

        if (!event.getSondercharaktere().isEmpty()) {
            for (Sondercharakter sondercharakter : event.getSondercharaktere()) {
                sondercharakterRepository.delete(sondercharakter);
            }
        }

        if (!event.getFraktionen().isEmpty()) {
            for (Fraktion fraktion : event.getFraktionen()) {
                fraktionRepository.delete(fraktion);
            }
        }

        eventRepository.delete(event);
    }


    /**
     * Ein einzelnes Event abrufen
     */
    @Override
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event mit ID " + id + " nicht gefunden"));
    }

    /**
     * Alle Events abrufen
     */
    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAllWithLocations();
    }

    /**
     * Events nach Organisator abrufen
     */
    @Override
    public List<Event> getEventsByOrganisator(Long organisatorId) {
        return eventRepository.findByOrganisatorId(organisatorId);
    }

    /**
     * Events nach Standort abrufen
     */
    @Override
    public List<Event> getEventsByLocation(Long locationId) {
        return eventRepository.findByLocationId(locationId);
    }

    /**
     * Alle "Pending"-Events abrufen
     */
    @Override
    public List<EventDto> findAllPendingEvents() {
        return eventRepository.findByStatus("Pending").stream()
                .map(event -> {
                    EventDto dto = new EventDto();
                    dto.setId(event.getId());
                    dto.setBezeichnung(event.getBezeichnung());
                    dto.setBeschreibung(event.getBeschreibung());
                    dto.setBildPath(event.getPhotoPath());
                    dto.setDatumVon(event.getDatumVon());
                    dto.setDatumBis(event.getDatumBis());
                    dto.setEventAbhangigeAnfahrt(event.getEventAbhangigeAnfahrt());
                    dto.setEventAbhangigeUnterkunft(event.getEventAbhangigeUnterkunft());
                    dto.setTyp(event.getTyp());
                    dto.setZeitspanne(event.getZeitspanne());
                    dto.setGenre(event.getGenre());
                    dto.setLocation(event.getLocation() != null ? event.getLocation() : null);
                    dto.setOrganisatorId(event.getOrganisator() != null ? event.getOrganisator().getId() : null);
                    dto.setStatus(event.getStatus());
                    dto.setAccessCode(event.getAccessCode());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Event genehmigen
     */
    @Transactional
    @Override
    public void approveRequest(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event mit ID " + id + " nicht gefunden"));

        event.setStatus("Approved");
        eventRepository.save(event);
        logger.info("Event approved: {}", event.getBezeichnung());
    }

    @Transactional
    @Override
    public void registerUserToEvent(String accessCode, Long userId) {

        Event event = eventRepository.findByAccessCode(accessCode);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));

        user.setEvent(event);
        userRepository.save(user);

    }

    /**
     * Verarbeitet eine einzelne Datei und speichert sie im angegebenen Verzeichnis.
     *
     * @param file Die hochzuladende Datei
     * @param uploadDir Das Zielverzeichnis für die Datei
     * @param errorMessage Fehlermeldung für ungültige Dateitypen
     * @return Der Pfad der hochgeladenen Datei
     */
    public String processSingleFile(MultipartFile file, String uploadDir, String errorMessage) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e) {

            return null;
        }
    }

}
