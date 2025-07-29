package de.larphelden.larp_app.controllers;

import de.larphelden.larp_app.dto.OrganisationDto;
import de.larphelden.larp_app.models.Event;
import de.larphelden.larp_app.models.Organisation;
import de.larphelden.larp_app.models.User;
import de.larphelden.larp_app.repositories.UserRepository;
import de.larphelden.larp_app.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.security.Principal;
import java.util.List;


@Controller
public class OrganisationController {

    private final OrganisationService organisationService;
    private UserRepository userRepository;
    private EventService eventService;
    private UserService userService;
    private StorageService storageService;


    public OrganisationController(OrganisationService organisationService, UserRepository userRepository, EventService eventService, UserService userService, StorageService storageService) {
        this.organisationService = organisationService;
        this.userRepository = userRepository;
        this.eventService = eventService;
        this.userService = userService;
        this.storageService = storageService;
    }

    @GetMapping
    public String showOrganisationRegistrationForm(Model model, OrganisationDto organisationDto) {
        model.addAttribute("organisation", organisationDto);
        return "register_organisation";
    }

    @GetMapping("/organisation_request")
    public String showOrganisationRequestPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Current roles: " + authentication.getAuthorities());

        List<OrganisationDto> pendingRequests = organisationService.findAllPendingRequests();
        model.addAttribute("pendingRequests", pendingRequests);
        return "organisation_request";
    }

    @GetMapping("/pending")
    public String getPendingRequests(Model model) {
        model.addAttribute("pendingRequests", organisationService.getPendingRequests());
        return "pending_organisations";
    }

    @GetMapping("/register_organisation")
    public String showRegistrationForm(Model model) {
        model.addAttribute("organisation", new OrganisationDto());
        return "register_organisation";
    }
    @GetMapping("/organisations_list")
    public String viewOrganisations(Model model) {
        List<Organisation> organisations = organisationService.findAll();
        model.addAttribute("organisations", organisations);
        return "organisations_list";
    }

    @GetMapping("/organisation_view/{id}")
    public String viewOrganisationDetails(@PathVariable Long id, Model model) {
        Organisation organisation = organisationService.findById(id);
        if (organisation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Organisation not found");
        }
        List<User> users = userRepository.findByOrganisationId(id);

        model.addAttribute("organisation", organisation);
        model.addAttribute("users", users);

        return "organisation_view";
    }

    @GetMapping("/organisation_edit/{id}")
    public String editOrganisationForm(@PathVariable Long id, Model model) {
        Organisation organisation = organisationService.findById(id);
        if (organisation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Organisation not found");
        }
        model.addAttribute("organisation", organisation);
        return "organisation_edit";
    }
    @GetMapping("/organisation_edit/{id}/search_user")
    public String searchUser(@PathVariable Long id, @RequestParam(required = false) String query, Model model) {
        Organisation organisation = organisationService.findById(id);
        if (organisation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Organisation not found");
        }

        List<User> searchResults = query != null && !query.isBlank()
                ? userRepository.findByUsernameContaining(query)
                : List.of();

        model.addAttribute("organisation", organisation);
        model.addAttribute("searchResults", searchResults);

        return "organisation_edit";
    }

    @PostMapping("/organisation_edit/{id}")
    public String editOrganisation(@PathVariable Long id, @ModelAttribute OrganisationDto organisationDto) {
        Organisation organisation = organisationService.findById(id);
        if (organisation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Organisation not found");
        }

        organisation.setName(organisationDto.getName());
        organisation.setBeschreibung(organisationDto.getBeschreibung());
        organisation.setKontakt(organisationDto.getKontakt());
        organisation.setSocialmedia(organisationDto.getSocialmedia());
        organisation.setStatus(organisationDto.getStatus());

        organisationService.save(organisation);

        return "redirect:/organisations_list";
    }

    @PostMapping("/register_organisation")
    public String registerOrganisation(@ModelAttribute OrganisationDto organisationDto,
                                       @RequestParam("mainImage") MultipartFile mainImage,
                                       Model model, Principal principal) {
        String username = principal.getName();


        String name = organisationDto.getName().trim();
        if (name.isBlank()) {

            model.addAttribute("nameError", "Der Name der Organisation darf nicht leer sein oder nur aus Leerzeichen bestehen");
            return "register_organisation";
        }

        if (!mainImage.isEmpty()) {
            storageService.store(mainImage, "organisation/main");
            organisationDto.setPhotoPath("organisation/main/" + mainImage.getOriginalFilename());
        }

        Organisation organisation = organisationService.registerOrganisation(organisationDto, username);

        User user = userRepository.findByUsername(username);

        user.setOrganisation(organisation);
        userRepository.save(user);

        return "redirect:/organisation_request";
    }

    @PostMapping("/approve_organisation/{id}")
    public String approveOrganisation(@PathVariable Long id) {
        organisationService.approveRequest(id);
        return "redirect:/organisation_request";
    }


    @PostMapping("/organisation_edit/{id}/add_user_manual")
    public String addUserManually(@PathVariable Long id, @RequestParam String username, Model model) {
        Organisation organisation = organisationService.findById(id);
        if (organisation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Organisation not found");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setOrganisation(organisation);
            userRepository.save(user);
        } else {
            user.setOrganisation(organisation);
            userRepository.save(user);
        }

        model.addAttribute("successMessage", "User " + username + " added to the organisation.");
        return "redirect:/organisation_edit/" + id;
    }

    @PostMapping("/organisation_edit/{id}/add_existing_user")
    public String addExistingUser(@PathVariable Long id, @RequestParam Long userId) {
        Organisation organisation = organisationService.findById(id);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setOrganisation(organisation);
        userRepository.save(user);

        return "redirect:/organisation_edit/" + id;
    }

}
