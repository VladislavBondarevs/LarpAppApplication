package de.larphelden.larp_app.controllers;

import de.larphelden.larp_app.dto.UserDto;
import de.larphelden.larp_app.models.*;
import de.larphelden.larp_app.repositories.*;
import de.larphelden.larp_app.services.EventService;
import de.larphelden.larp_app.services.LocationService;
import de.larphelden.larp_app.services.OrganisationService;
import de.larphelden.larp_app.services.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    private final UserService userService;
    private final OrganisationService organisationService;
    private final LocationService locationService;
    private final EventService eventService;
    private final UserRepository userRepository;
    private UnitRepository unitRepository;
    private SondercharakterRepository sondercharakterRepository;
    private EventRepository eventRepository;
    private FraktionRepository fraktionRepository;

    public AdminController(UserService userService, OrganisationService organisationService, LocationService locationService,
                           EventService eventService, UserRepository userRepository, UnitRepository unitRepository,
                           SondercharakterRepository sondercharakterRepository, EventRepository eventRepository, FraktionRepository fraktionRepository) {
        this.userService = userService;
        this.organisationService = organisationService;
        this.locationService = locationService;
        this.eventService = eventService;
        this.userRepository = userRepository;
        this.unitRepository = unitRepository;
        this.sondercharakterRepository = sondercharakterRepository;
        this.eventRepository = eventRepository;
        this.fraktionRepository = fraktionRepository;
    }
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @PersistenceContext
    private EntityManager entityManager;


    @Secured("ROLE_ADMIN")
    @GetMapping("/admin_dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("pendingOrganisations", organisationService.getPendingRequests());
        model.addAttribute("pendingEvent", eventService.findAllPendingEvents());
        model.addAttribute("pendingLocation", locationService.findPendingRequests());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("pendingSondercharakter", sondercharakterRepository.findPendingRequests());
        return "admin_dashboard";
    }

    @PostMapping("/grant-admin/{id}")
    public String grantAdmin(@PathVariable Long id) {
        userService.grantRole(id, "ROLE_ADMIN");
        System.out.println("Granted ADMIN role to user with ID: " + id);
        return "redirect:/admin_dashboard";
    }

    @PostMapping("/grant-organisator/{id}")
    public String grantOrganisator(@PathVariable Long id) {
        userService.grantRole(id, "ROLE_ORGANISATOR");
        System.out.println("Granted ORGANISATOR role to user with ID: " + id);
        return "redirect:/admin_dashboard";
    }

    @PostMapping("/grant-user/{id}")
    public String grantUser(@PathVariable Long id) {
        userService.grantRole(id, "ROLE_USER");
        System.out.println("Granted USER role to user with ID: " + id);
        return "redirect:/admin_dashboard";
    }

    @PostMapping("/approve-organisation/{id}")
    public String approveOrganisation(@PathVariable Long id) {
        organisationService.approveRequest(id);
        return "redirect:/admin_dashboard";
    }

    @PostMapping("/approve-location/{id}")
    public String approveLocation(@PathVariable Long id) {
        locationService.approveRequest(id);
        return "redirect:/admin_dashboard";
    }

    @PostMapping("/approve-event/{id}")
    public String approveEvent(@PathVariable Long id) {
        eventService.approveRequest(id);
        return "redirect:/admin_dashboard";
    }
    @PostMapping("/approve-sondercharakter/{id}")
    public String approveSondercharakter(@PathVariable Long id) {
        eventService.approveRequest(id);
        return "redirect:/admin_dashboard";
    }

    @PostMapping("/create-user")
    public String createUser(@ModelAttribute UserDto userDto) {
        userService.registerUser(userDto);
        return "redirect:/admin_dashboard";
    }

    @PostMapping("/event_view/{id}/remove_user")
    public String removeUserFromEvent(@PathVariable Long id, @RequestParam Long userId, RedirectAttributes redirectAttributes) {
        Event event = eventService.getEventById(id);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event nicht gefunden");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer mit ID " + userId + " nicht gefunden."));

        event.getUsers().remove(user);
        user.setEvent(null);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Benutzer erfolgreich entfernt.");
        return "redirect:/event_view/" + id;
    }

    @PostMapping("/delete-organisation/{id}")
    public String deleteOrganisation(@PathVariable Long id) {
        organisationService.deleteOrganisation(id);
        userService.removeOrganisationFromUsers(id);
        return "redirect:/organisations_list";
    }

    @PostMapping("/unit/{unitId}/delete")
    @Transactional
    public String deleteUnit(@PathVariable("unitId") Long unitId, RedirectAttributes redirectAttributes) {
        Logger logger = LoggerFactory.getLogger(this.getClass());

        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> {
                    logger.error("Unit with ID {} not found.", unitId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Einheit nicht gefunden");
                });

        if (!unit.getUsers().isEmpty()) {
            logger.info("Clearing associated users for Unit with ID: {}", unitId);
            unit.getUsers().clear();
            unitRepository.save(unit);
        }

        Fraktion fraktion = unit.getFraktion();
        unit.setFraktion(null);
        unitRepository.save(unit);
        unitRepository.delete(unit);
        unitRepository.flush();


        try {
            logger.info("Calling deleteById for Unit ID: {}", unitId);
            unitRepository.deleteById(unitId);
            logger.info("Successfully deleted Unit with ID: {}", unitId);
            redirectAttributes.addFlashAttribute("successMessage", "Einheit erfolgreich gelöscht.");
        } catch (Exception e) {
            logger.error("Failed to delete Unit with ID: {}", unitId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Löschen der Einheit: " + e.getMessage());
        }

        if (fraktion != null && fraktion.getEvent() != null) {
            return "redirect:/event_edit/" + fraktion.getEvent().getId() + "/fraktion_info";
        } else {
            return "redirect:/events";
        }
    }

    @Transactional
    @PostMapping("/unit/{unitId}/remove_member")
    public String removeMemberFromUnit(@PathVariable Long unitId, @RequestParam Long userId, RedirectAttributes redirectAttributes) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!unit.getUsers().contains(user)) {
            redirectAttributes.addFlashAttribute("error", "User is not a member of this unit!");
            return "redirect:/event_edit/" + unit.getFraktion().getEvent().getId() + "/fraktion_info";
        }

        unit.getUsers().remove(user);
        unitRepository.save(unit);

        user.getUnits().remove(unit);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "User successfully removed from unit!");
        return "redirect:/event_edit/" + unit.getFraktion().getEvent().getId() + "/fraktion_info";
    }

    @GetMapping("/admin/sondercharakter/requests")
    public String getSondercharakterRequests(@RequestParam(required = false) Long sondercharakterId,
                                             Model model) {
        if (sondercharakterId != null) {
            Sondercharakter sondercharakter = sondercharakterRepository.findById(sondercharakterId)
                    .orElseThrow(() -> new IllegalArgumentException("Sondercharakter mit ID " + sondercharakterId + " nicht gefunden"));

            model.addAttribute("sondercharakter", sondercharakter);
            model.addAttribute("sondercharakterId", sondercharakterId);
            model.addAttribute("message", "Sondercharakter mit ID " + sondercharakterId + " erfolgreich geladen.");
        } else {
            List<Sondercharakter> pendingSondercharakter = sondercharakterRepository.findAll().stream()
                    .filter(sc -> !sc.getRequests().isEmpty())
                    .collect(Collectors.toList());

            model.addAttribute("pendingSondercharakter", pendingSondercharakter);
            model.addAttribute("message", "Alle ausstehenden Sondercharakter-Anfragen erfolgreich geladen.");
        }

        return "admin_dashboard";
    }

    @PostMapping("/sondercharakter/{sondercharakterId}/delete")
    public String deleteSondercharakter(@PathVariable Long sondercharakterId, RedirectAttributes redirectAttributes) {
        Sondercharakter sondercharakter = sondercharakterRepository.findById(sondercharakterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Sondercharakter с ID " + sondercharakterId + " nicht gefunden."));

        if (sondercharakter.getEvent() == null) {
            return "redirect:/events";
        }
        try {
            sondercharakterRepository.deleteById(sondercharakterId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/event_list";
    }

    @PostMapping("/approve-request/{sondercharakterId}/{userId}")
    public String approveSondercharakter(
                                 @PathVariable Long sondercharakterId,
                                 @PathVariable Long userId,
                                 RedirectAttributes redirectAttributes) {

        Sondercharakter sondercharakter = sondercharakterRepository.findById(sondercharakterId)
                .orElseThrow(() -> new IllegalArgumentException("Sondercharakter mit ID " + sondercharakterId + " nicht gefunden"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User mit ID " + userId + " nicht gefunden"));

        boolean requestExists = sondercharakter.getRequests().keySet().stream()
                .anyMatch(existingUser -> existingUser.getId().equals(userId));

        if (!requestExists) {
            redirectAttributes.addFlashAttribute("error", "Anfrage nicht gefunden.");
            return "redirect:/admin_dashboard";
        }

        sondercharakter.setUser(user);
        sondercharakter.getRequests().put(user, "APPROVED");
        sondercharakterRepository.save(sondercharakter);

        redirectAttributes.addFlashAttribute("success", "Anfrage wurde genehmigt.");
        return "redirect:/admin_dashboard";
    }

    @PostMapping("/reject-request/{sondercharakterId}/{userId}")
    public String rejectSondercharakter(
            @PathVariable Long sondercharakterId,
            @PathVariable Long userId,
            RedirectAttributes redirectAttributes) {

        Sondercharakter sondercharakter = sondercharakterRepository.findById(sondercharakterId)
                .orElseThrow(() -> new IllegalArgumentException("Sondercharakter mit ID " + sondercharakterId + " nicht gefunden"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User mit ID " + userId + " nicht gefunden"));

        boolean requestExists = sondercharakter.getRequests().keySet().stream()
                .anyMatch(existingUser -> existingUser.getId().equals(userId));

        if (!requestExists) {
            redirectAttributes.addFlashAttribute("error", "Anfrage nicht gefunden.");
            return "redirect:/admin_dashboard";
        }
        sondercharakter.getRequests().put(user, "REJECTED");
        sondercharakterRepository.save(sondercharakter);

        redirectAttributes.addFlashAttribute("success", "Anfrage wurde abgelehnt.");
        return "redirect:/admin_dashboard";
    }

    @PostMapping("/delete-request/{sondercharakterId}/{userId}")
    public String deleteSondercharakterRequest(
            @PathVariable Long sondercharakterId,
            @PathVariable Long userId,
            RedirectAttributes redirectAttributes) {

        Sondercharakter sondercharakter = sondercharakterRepository.findById(sondercharakterId)
                .orElseThrow(() -> new IllegalArgumentException("Sondercharakter mit ID " + sondercharakterId + " nicht gefunden"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User mit ID " + userId + " nicht gefunden"));

        boolean requestExists = sondercharakter.getRequests().containsKey(user);

        if (!requestExists) {
            redirectAttributes.addFlashAttribute("error", "Anfrage nicht gefunden.");
            return "redirect:/admin_dashboard";
        }

        sondercharakter.getRequests().remove(user);
//        sondercharakter.getRequests().put(user, "Deleted");
        sondercharakterRepository.save(sondercharakter);

        redirectAttributes.addFlashAttribute("success", "Anfrage wurde erfolgreich gelöscht.");
        return "redirect:/admin_dashboard";
    }

    @PostMapping("/fraktion/{fraktionId}/delete")
    @Transactional
    public String deleteFraktion(@PathVariable("fraktionId") Long fraktionId, RedirectAttributes redirectAttributes) {
        Logger logger = LoggerFactory.getLogger(this.getClass());

        Fraktion fraktion = fraktionRepository.findById(fraktionId)
                .orElseThrow(() -> {
                    logger.error("Fraktion with ID {} not found.", fraktionId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Fraktion nicht gefunden");
                });
        try {
            if (!fraktion.getUnits().isEmpty()) {
                logger.info("Deleting associated Units for Fraktion ID: {}", fraktionId);
                for (Unit unit : fraktion.getUnits()) {
                    unit.setFraktion(null);
                    unitRepository.save(unit);
                    unitRepository.delete(unit);
                }
            }
            logger.info("Deleting Fraktion with ID: {}", fraktionId);
            fraktionRepository.delete(fraktion);
            redirectAttributes.addFlashAttribute("successMessage", "Fraktion erfolgreich gelöscht.");
        } catch (Exception e) {
            logger.error("Failed to delete Fraktion with ID: {}", fraktionId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Löschen der Fraktion: " + e.getMessage());
        }
        return "redirect:/event_list";
    }

    @GetMapping("/event/{eventId}/delete")
    public String showDeleteEvent(@PathVariable Long eventId, Model model) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event mit ID " + eventId + " nicht gefunden"));

        model.addAttribute("event", event);
        return "event/event_list";
    }

    @PostMapping("/event/{eventId}/delete")
    @Transactional
    public String deleteEvent(@PathVariable Long eventId, RedirectAttributes redirectAttributes) {
        try {
            eventService.deleteEvent(eventId);
            redirectAttributes.addFlashAttribute("successMessage", "Event erfolgreich gelöscht.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Löschen des Events: " + e.getMessage());
        }
        return "redirect:/event_list";
    }

}
