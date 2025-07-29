package de.larphelden.larp_app.controllers;

import de.larphelden.larp_app.dto.EventDto;
import de.larphelden.larp_app.models.*;
import de.larphelden.larp_app.repositories.*;
import de.larphelden.larp_app.services.*;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
@Controller
public class EventController {

    private final EventService eventService;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final EventRepository eventRepository;
    private final UserService userService;
    private final UnitRepository unitRepository;
    private final FraktionRepository fraktionRepository;
    private final SondercharakterRepository sondercharakterRepository;
    private final StorageService storageService;
    private final OrganisationService organisationService;


    public EventController(EventService eventService, UserRepository userRepository, LocationRepository locationRepository,
                           LocationService locationService, EventRepository eventRepository, UserService userService,
                           UnitRepository unitRepository, FraktionRepository fraktionRepository,
                           SondercharakterRepository sondercharakterRepository, StorageService storageService,
                           OrganisationService organisationService) {
        this.eventService = eventService;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.locationService = locationService;
        this.eventRepository = eventRepository;
        this.userService = userService;
        this.unitRepository = unitRepository;
        this.fraktionRepository = fraktionRepository;
        this.sondercharakterRepository = sondercharakterRepository;
        this.storageService = storageService;

        this.organisationService = organisationService;
    }

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/event_view/{id}/users")
    public String getEventUsers(@PathVariable Long id, @RequestParam(required = false) String query, Model model) {
        Event event = eventService.getEventById(id);

        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }

        List<User> users = event.getUsers();
        if (users == null) {
            users = Collections.emptyList();
        }

        List<User> filteredUsers = (query != null && !query.isBlank())
                ? users.stream()
                .filter(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList())
                : users;

        String photoPath = "/uploads/events/main/default.jpg";
        if (event.getPhotoPath() != null && !event.getPhotoPath().isEmpty()) {
            Path filePath = Paths.get("uploads/events/main" + event.getPhotoPath());
            if (Files.exists(filePath)) {
                photoPath = "/uploads/events/main" + event.getPhotoPath();
            }
        }
        List<String> gallery = event.getBildergallerie();
        if (gallery == null || gallery.isEmpty()) {
            gallery = List.of("default-placeholder.png");
        }

        model.addAttribute("event", event);
        model.addAttribute("photoPath", photoPath);
        model.addAttribute("bildergallerie", event.getBildergallerie());
        model.addAttribute("users", filteredUsers);

        return "event_view";
    }

    @GetMapping("/approve_event/{id}")
    public String showApproveEvent(@PathVariable Long id, Model model) {
        try {
            Event event = eventService.getEventById(id);
            model.addAttribute("event", event);
            return "event_list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Event mit ID " + id + " nicht gefunden.");
            return "errorPage";
        }
    }

    @GetMapping("/event_request")
    public String showEventRequestPage(Model model) {
        List<EventDto> pendingRequests = eventService.findAllPendingEvents();
        model.addAttribute("event_request", pendingRequests);
        return "event_request";
    }

    @GetMapping("/register_event")
    public String showRegisterEventForm(Model model) {
        model.addAttribute("eventDto", new EventDto());
        List<Location> locations = locationRepository.findAll();
        if (locations.isEmpty()) {
            model.addAttribute("locationError", "Keine verfügbaren Locations gefunden.");
        } else {
            model.addAttribute("locations", locations);
        }
        return "register_event";
    }

    @GetMapping("/event_list")
    public String showEventList(Model model) {
        List<Event> events = eventService.getAllEvents();

        model.addAttribute("events", events);

        return "event_list";
    }

    @GetMapping("/event-pending")
    public String listPendingEvents(Model model) {
        List<EventDto> pendingEvents = eventService.findAllPendingEvents();
        model.addAttribute("events", pendingEvents);
        return "event_request";
    }

    @GetMapping("/pending_events")
    public String getPendingEvents(Model model) {
        model.addAttribute("pendingEvents", eventService.findAllPendingEvents());
        return "pending_events";
    }

    @GetMapping("/event_edit/{id}")
    public String editEvent(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }

        List<Organisation> organisations = organisationService.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String datumVon = event.getDatumVon().format(formatter);
        String datumBis = event.getDatumBis().format(formatter);

        List<User> users = event.getUsers();

        model.addAttribute("event", event);
        model.addAttribute("users", users);
        model.addAttribute("datumVon", datumVon);
        model.addAttribute("datumBis", datumBis);
        model.addAttribute("organisations", organisations);
        model.addAttribute("locations", locationService.getAllLocations());

        return "event_edit";
    }

    @PostMapping("/event_edit/{id}")
    public String updateEvent(
            @PathVariable("id") Long id,
            @RequestParam("startdatum") String startdatum,
            @RequestParam("enddatum") String enddatum,
            @RequestParam("bezeichnung") String bezeichnung,
            @RequestParam("beschreibung") String beschreibung,
            @RequestParam("typ") String typ,
            @RequestParam("zeitspanne") String zeitspanne,
            @RequestParam("genre") String genre,
            Model model) {

        Event event = eventService.getEventById(id);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        event.setBezeichnung(bezeichnung);
        event.setBeschreibung(beschreibung);
        event.setTyp(typ);
        event.setZeitspanne(zeitspanne);
        event.setGenre(genre);

        eventService.save(event);

        return "redirect:/event_edit/" + id;
    }

    @PostMapping("/register_event")
    public String registerEvent(@ModelAttribute EventDto eventDto,
                                @RequestParam("bildergallerie") List<MultipartFile> bildergallerie,
                                @RequestParam("mainImage") MultipartFile mainImage,
                                Model model, Principal principal) {
        try {
            // 1. Benutzer abrufen
            System.out.println("Schritt 1: Benutzerprüfung starten.");
            String username = principal.getName();
            User user = userRepository.findByUsername(username);
            if (user == null || (user.getOrganisation() == null && user.getRole().equals("ROLE_ORGANISATOR"))) {
                if (user.getRole().equals("ROLE_ADMIN")) {
                    System.out.println("Admin erstellt das Event ohne Organisation.");
                } else {
                    System.out.println("Benutzer hat keine Organisation. Abbruch.");
                    model.addAttribute("errorMessage", "Sie sind keinem Organisator zugeordnet.");
                    return "register_event";
                }
            }
            Organisation organisator = user.getOrganisation();
            if (organisator != null) {
                System.out.println("Organisation erfolgreich gefunden: " + organisator.getName());
            }

            // 4. Daten prüfen
            System.out.println("DatumVon: " + eventDto.getDatumVon() + ", DatumBis: " + eventDto.getDatumBis());
            if (eventDto.getDatumVon() == null || eventDto.getDatumBis() == null) {
                model.addAttribute("dateError", "Bitte geben Sie gültige Daten ein.");
                System.out.println("Fehler: Datum fehlt.");
                return "register_event";
            }
            if (eventDto.getDatumVon().isAfter(eventDto.getDatumBis())) {
                model.addAttribute("dateError", "Das Startdatum kann nicht nach dem Enddatum liegen.");
                System.out.println("Fehler: Startdatum liegt nach Enddatum.");
                return "register_event";
            }

            // 5. Event erstellen
            System.out.println("Event wird erstellt.");
            Event event = new Event();
            event.setBezeichnung(eventDto.getBezeichnung());
            event.setBeschreibung(eventDto.getBeschreibung());
            event.setDatumVon(eventDto.getDatumVon());
            event.setDatumBis(eventDto.getDatumBis());
            event.setLocation(eventDto.getLocation());
            event.setGenre(eventDto.getGenre());
            event.setTyp(eventDto.getTyp());
            event.setZeitspanne(eventDto.getZeitspanne());
            event.setEventAbhangigeAnfahrt(eventDto.getEventAbhangigeAnfahrt());
            event.setEventAbhangigeUnterkunft(eventDto.getEventAbhangigeUnterkunft());
            event.setBildergallerie(eventDto.getBildergalleriePaths());
            event.setOrganisator(organisator);
            event.setAccessCode(UUID.randomUUID().toString());

            String name = eventDto.getBezeichnung() != null ? eventDto.getBezeichnung().trim() : "";
            if (name.isEmpty()) {
                System.out.println("Fehler: Eventname ist leer.");
                model.addAttribute("nameError", "Der Name des Events darf nicht leer sein oder nur aus Leerzeichen bestehen.");
                return "register_event";
            }

            event.setBezeichnung(eventDto.getBezeichnung());

            if (!mainImage.isEmpty()) {
                storageService.store(mainImage, "events/main");
                event.setPhotoPath("events/main/" + mainImage.getOriginalFilename());
            }

            List<String> galleryPaths = new ArrayList<>();
            for (MultipartFile file : bildergallerie) {
                if (!file.isEmpty()) {
                    storageService.store(file, "events/gallery");
                    galleryPaths.add("events/gallery/" + file.getOriginalFilename());
                }
            }
            event.setBildergallerie(galleryPaths);

            // Speichern
            System.out.println("Event wird gespeichert: " + event.getBezeichnung());

            eventRepository.save(event);
            System.out.println("Event erfolgreich gespeichert: " + event.getBezeichnung());

            // Erfolgreiche Registrierung
            model.addAttribute("successMessage", "Event erfolgreich registriert: " + event.getBezeichnung());
            return "redirect:/event_request?success";

        } catch (Exception e) {
            System.out.println("Fehler bei der Registrierung: " + e.getMessage());
            model.addAttribute("errorMessage", "Fehler bei der Registrierung des Events: " + e.getMessage());
            return "register_event";
        }
    }

    @PostMapping("/event_edit/{id}/location")
    public String updateEventLocation(@PathVariable Long id, @RequestParam String locationId, Model model) {
        try {
            Event event = eventService.getEventById(id);

            if ("none".equals(locationId)) {
                event.setLocation(null);
            } else {
                Location location = locationRepository.findById(Long.parseLong(locationId))
                        .orElseThrow(() -> new IllegalArgumentException("Location mit ID " + locationId + " nicht gefunden"));

                event.setLocation(location);
            }

            eventService.save(event);

            model.addAttribute("successMessage", "Location erfolgreich aktualisiert.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Fehler beim Aktualisieren der Location: " + e.getMessage());
        }
        return "redirect:/event_edit/" + id;
    }

    @PostMapping({"/approve_event/{id}", "/events/{id}/approve"})
    public String approveEvent(@PathVariable Long id, Model model) {
        try {
            Event event = eventService.getEventById(id);
            event.setStatus("Approved");
            eventService.save(event);
            model.addAttribute("successMessage", "Event erfolgreich genehmigt.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Fehler bei der Genehmigung des Events: " + e.getMessage());
        }
        return "redirect:/event_list";
    }

    @PostMapping("/register-user")
    public String registerUserToEvent(@RequestParam String accessCode, @RequestParam Long userId, Model model) {
        try {
            eventService.registerUserToEvent(accessCode, userId);
            model.addAttribute("successMessage", "Benutzer erfolgreich zum Event hinzugefügt.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Fehler bei der Registrierung des Benutzers: " + e.getMessage());
        }
        return "redirect:/events_list";
    }

    @Transactional
    @PostMapping("/event_edit/{id}/add_user_manual")
    public String addUserManuallyToEvent(@PathVariable Long id, @RequestParam String username, Model model) {
        Event event = eventService.getEventById(id);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }

        User user = userRepository.findByUsername(username);

        if (user == null) {

            user = new User();
            user.setUsername(username);
            userRepository.save(user);
        } else {

            user = userRepository.getOne(user.getId());
        }

        event.addUser(user);
        eventService.save(event);

        return "redirect:/event_edit/" + event.getId();
    }

    @GetMapping("/event_view/{id}")
    public String viewEvent(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event nicht gefunden");
        }
        List<User> users = event.getUsers();
        model.addAttribute("users", users);

        String photoPath = (event.getPhotoPath() != null && !event.getPhotoPath().isEmpty())
                ? "/uploads/" + event.getPhotoPath()
                : "/uploads/default.jpg";
        model.addAttribute("photoPath", photoPath);

        List<String> gallery = event.getBildergallerie();
        if (gallery == null || gallery.isEmpty()) {
            gallery = List.of("default-placeholder.png");
        }
        Organisation organisation = event.getOrganisator();

        List<Location> locations = locationService.getAllLocations();
        model.addAttribute("locations", locations);
        model.addAttribute("bildergallerie", gallery);
        model.addAttribute("organisation", organisation);
        model.addAttribute("event", event);
        return "event_view";
    }

    //Fraktionen, Unit, Sondercharakter Creation

    @Transactional
    @GetMapping("/event_edit/{eventId}/add_unit")
    public String showAddUnitForm(@PathVariable Long eventId,
                                  @RequestParam(required = false) Long fraktionId,
                                  Model model) {
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }

        Fraktion fraktion = null;
        if (fraktionId != null) {
            fraktion = fraktionRepository.findById(fraktionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fraktion not found"));
        }


        model.addAttribute("unit", new Unit());
        model.addAttribute("event", event);
        model.addAttribute("fraktion", fraktion);
        model.addAttribute("fraktionen", fraktionRepository.findAll());

        return "add_unit";
    }

    @PostMapping("/event_edit/{eventId}/add_unit")
    public String addUnitToEvent(@PathVariable Long eventId,
                                 @RequestParam String unitName,
                                 @RequestParam Integer unitCapacity,
                                 @RequestParam(required = false) Long fraktionId,
                                 @RequestParam("beschreibung") String beschreibung,
                                 @RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        Unit unit = new Unit();
        unit.setName(unitName);
        unit.setCapacity(unitCapacity);

        if (beschreibung != null && !beschreibung.isBlank()) {
            unit.setBeschreibung(beschreibung);
        }

        if (fraktionId != null) {
            Fraktion fraktion = fraktionRepository.findById(fraktionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fraktion not found"));
            unit.setFraktion(fraktion);
        }

        if (!file.isEmpty()) {
            try {
                String subdirectory = "unit";
                String filename = file.getOriginalFilename();

                storageService.store(file, subdirectory);
                unit.setPhotoPath("uploads/" + subdirectory + "/" + filename);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Fehler beim Hochladen der Datei!");
                return "redirect:/event_edit/" + eventId;
            }
        }

        unitRepository.save(unit);
//        event.getUnits().add(unit);
        eventRepository.save(event);

        redirectAttributes.addFlashAttribute("success", "Unit successfully added and linked to Fraktion!");
        return "redirect:/event_edit/" + eventId;
    }

    @GetMapping("/event_edit/{id}/add_fraktion")
    public String showAddFraktionForm(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }

        model.addAttribute("fraktion", new Fraktion());
        model.addAttribute("event", event);
        return "add_fraktion";
    }

    @PostMapping("/event_edit/{id}/add_fraktion")
    public String addFraktionToEvent(@PathVariable Long id,
                                     @ModelAttribute Fraktion fraktion,
                                     @RequestParam("file") MultipartFile file,
                                     RedirectAttributes redirectAttributes) {
        Event event = eventService.getEventById(id);
        if (event == null) {
            redirectAttributes.addFlashAttribute("error", "Event not found!");
            return "redirect:/event_edit/" + id;
        }
        if (!file.isEmpty()) {
            try {
                String subdirectory = "fraktion";
                String filename = file.getOriginalFilename();

                storageService.store(file, subdirectory);
                fraktion.setPhotoPath("uploads/" + subdirectory + "/" + filename);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Fehler beim Hochladen der Datei!");
                return "redirect:/event_edit/" + id;
            }
        }

        fraktion.setEvent(event);
        fraktionRepository.save(fraktion);

        redirectAttributes.addFlashAttribute("success", "Fraktion successfully added!");
        return "redirect:/event_edit/" + id;
    }

    @Transactional
    @GetMapping("/event_edit/{id}/add_sondercharakter")
    public String showAddSondercharakterForm(@PathVariable Long id,
                                             Model model) {

        Event event = eventService.getEventById(id);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }

        model.addAttribute("sondercharakter", new Sondercharakter());
        model.addAttribute("event", event);

        return "add_sondercharakter";
    }

    @PostMapping("/event_edit/{id}/add_sondercharakter")
    public String addSondercharakterToEvent(
            @PathVariable Long id,
            @RequestParam(required = false) boolean anzeigen,
            @RequestParam("file") MultipartFile file,
            @ModelAttribute Sondercharakter sondercharakter,
            RedirectAttributes redirectAttributes,
            Model model) {

        Event event = eventService.getEventById(id);
        if (event == null) {
            redirectAttributes.addFlashAttribute("error", "Event nicht gefunden!");
            return "redirect:/event_edit/" + id;
        }

        sondercharakter.setEvent(event);

        if (!file.isEmpty()) {
            try {
                String subdirectory = "sondercharakter";
                String filename = file.getOriginalFilename();

                storageService.store(file, subdirectory);
                sondercharakter.setPhotoPath("uploads/" + subdirectory + "/" + filename);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Fehler beim Hochladen der Datei!");
                return "redirect:/event_edit/" + id;
            }
        }

        sondercharakter.setAnzeigen(anzeigen);
        sondercharakterRepository.save(sondercharakter);

        redirectAttributes.addFlashAttribute("success", "Sondercharakter erfolgreich hinzugefügt!");
        return "redirect:/event_edit/" + id;
    }

    @GetMapping("/event_edit/{eventId}/fraktion_info")
    public String showFraktionInfo(@PathVariable Long eventId, Model model, Principal principal) {
        Event event = eventRepository.findById(eventId)
                .orElse(null);
        if (event == null) {
            return "redirect:/events";
        }
        model.addAttribute("event", event);

        List<Fraktion> fraktionen = fraktionRepository.findByEventId(eventId);

        if (fraktionen == null || fraktionen.isEmpty()) {
            model.addAttribute("fraktionen", Collections.emptyList());
            model.addAttribute("infoMessage", "Keine Fraktion vorhanden.");
        } else {
            model.addAttribute("fraktionen", fraktionen);
        }

        Map<Long, List<Unit>> fraktionUnits = new HashMap<>();
        Map<Long, Boolean> userJoinedUnits = new HashMap<>();
        Map<Long, Integer> unitOccupiedSeats = new HashMap<>();
        String username = principal.getName();
        User user = userRepository.findByUsername(username);

        for (Fraktion fraktion : fraktionen) {
            List<Unit> units = unitRepository.findByFraktionId(fraktion.getId());
            fraktionUnits.put(fraktion.getId(), units);

            for (Unit unit : units) {
                userJoinedUnits.put(unit.getId(), unit.getUsers().contains(user));
                unitOccupiedSeats.put(unit.getId(), unit.getUsers().size());
            }
        }

        model.addAttribute("fraktionUnits", fraktionUnits);
        model.addAttribute("userJoinedUnits", userJoinedUnits);
        model.addAttribute("unitOccupiedSeats", unitOccupiedSeats);

        return "infos/fraktion_info";
    }

    @PostMapping("/unit/{unitId}/join")
    public String joinUnit(@PathVariable Long unitId, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Benutzer nicht gefunden");
        }

        System.out.println("Benutzer gefunden: " + user.getUsername() + ", ID: " + user.getId());

        boolean isAlreadyInUnit = unitRepository.existsByUsersContaining(user);
        if (isAlreadyInUnit) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Sie können nur einem Unit gleichzeitig beitreten.");
        }

        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit nicht gefunden"));

        Fraktion fraktion = unit.getFraktion();
        if (fraktion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fraktion für diesen Unit nicht gefunden");
        }

        Event event = fraktion.getEvent();
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event für diese Fraktion nicht gefunden");
        }

        System.out.println("Event gefunden: " + event.getBezeichnung() + ", ID: " + event.getId());
        System.out.println("Event-Teilnehmer IDs: " + event.getUsers().stream().map(User::getId).toList());

        if (!event.getUsers().contains(user)) {
            System.out.println("Benutzer ist kein Teilnehmer des Events.");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Sie können diesem Unit nicht beitreten, da Sie kein Teilnehmer des Events sind.");
        }

        if (unit.getUsers().size() >= unit.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dieser Unit hat bereits die maximale Anzahl an Mitgliedern erreicht.");
        }

        checkUnitBalance(unit, fraktion);

        unit.getUsers().add(user);
        unit.updateTeilnehmerAnzahl();
        unitRepository.save(unit);

        fraktion.getUsers().add(user);
        fraktionRepository.save(fraktion);

        Long eventId = event.getId();
        return "redirect:/event_edit/" + eventId + "/fraktion_info";
    }

    private void checkUnitBalance(Unit unit, Fraktion fraktion) {
        List<Unit> allUnitsInFraktion = unitRepository.findByFraktionId(fraktion.getId());

        int maxParticipants = allUnitsInFraktion.stream()
                .mapToInt(u -> u.getUsers().size())
                .max()
                .orElse(0);

        int minParticipants = allUnitsInFraktion.stream()
                .mapToInt(u -> u.getUsers().size())
                .min()
                .orElse(0);

        int maxAllowedDifference = 3;
        if (unit.getUsers().size() >= maxParticipants && maxParticipants - minParticipants > maxAllowedDifference) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dieser Unit hat bereits zu viele Mitglieder. Bitte wählen Sie einen anderen Unit, um die Balance zu halten.");
        }
    }

    @GetMapping("/event_edit/{eventId}/sondercharakter_info")
    public String showSondercharakterInfo(@PathVariable Long eventId, Model model) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event nicht gefunden"));

        List<Sondercharakter> sondercharakterList = sondercharakterRepository.findByEventId(eventId);

        model.addAttribute("event", event);
        model.addAttribute("sondercharaktere", sondercharakterList);

        return "infos/sondercharakter_info";
    }

    @GetMapping("/unit/{unitId}/members")
    public String getUnitMembers(@PathVariable Long unitId, Model model) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found"));

        Set<User> users = unit.getUsers();

        model.addAttribute("unit", unit);
        model.addAttribute("users", users);

        return "unit_members";
    }

    @PostMapping("/event/{eventId}/sondercharakter/{sondercharakterId}/join")
    public String requestJoinSondercharakter(@PathVariable Long eventId,
                                             @PathVariable Long sondercharakterId,
                                             @RequestParam("reason") String reason,
                                             Principal principal,
                                             RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event nicht gefunden"));

        if (!event.getUsers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Sie können keinen Sondercharakter beantragen, da Sie kein Teilnehmer des Events sind.");
        }

        Sondercharakter sondercharakter = sondercharakterRepository.findById(sondercharakterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sondercharakter nicht gefunden"));

        if (sondercharakter.getRequests().containsKey(user)) {
            redirectAttributes.addFlashAttribute("infoMessage",
                    "Sie haben bereits eine Anfrage für diesen Sondercharakter gestellt.");
            return "redirect:/event_edit/" + eventId + "/sondercharakter_info";
        }

        sondercharakter.getRequests().put(user, reason + " - Pending");
        sondercharakterRepository.save(sondercharakter);

        redirectAttributes.addFlashAttribute("successMessage",
                "Ihre Anfrage für den Sondercharakter wurde erfolgreich gestellt.");
        return "redirect:/event_edit/" + eventId + "/sondercharakter_info";
    }

    @GetMapping("/event_edit/{eventId}/unit_info/{unitId}")
    public String showUnitInfo(@PathVariable Long eventId, @PathVariable Long unitId, Model model, Principal principal) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit mit ID " + unitId + " nicht gefunden."));

        if (unit.getFraktion() == null || unit.getFraktion().getEvent() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fraktion oder Event ist nicht vorhanden.");
        }

        Event event = unit.getFraktion().getEvent();

        if (!event.getId().equals(eventId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unit gehört nicht zu diesem Event.");
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username);

        boolean userJoinedUnit = unit.getUsers().contains(user);

        int occupiedSeats = unit.getUsers() != null ? unit.getUsers().size() : 0;

        List<User> unitUsers = unit.getUsers() != null ? new ArrayList<>(unit.getUsers()) : new ArrayList<>();

        System.out.println("Unit loaded: " + unit.getName());
        System.out.println("Event ID: " + eventId);
        System.out.println("Unit Users Count: " + occupiedSeats);

        model.addAttribute("unit", unit);
        model.addAttribute("event", event);
        model.addAttribute("userJoinedUnit", userJoinedUnit);
        model.addAttribute("occupiedSeats", occupiedSeats);
        model.addAttribute("unitUsers", unitUsers);

        return "infos/unit_info";
    }
}