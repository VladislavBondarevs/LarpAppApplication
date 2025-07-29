package de.larphelden.larp_app.controllers;

import de.larphelden.larp_app.dto.UserDto;
import de.larphelden.larp_app.models.Sondercharakter;
import de.larphelden.larp_app.models.Unit;
import de.larphelden.larp_app.models.User;
import de.larphelden.larp_app.repositories.SondercharakterRepository;
import de.larphelden.larp_app.repositories.UnitRepository;
import de.larphelden.larp_app.repositories.UserRepository;
import de.larphelden.larp_app.services.EventService;
import de.larphelden.larp_app.services.StorageService;
import de.larphelden.larp_app.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.security.Principal;
import java.util.List;


@Controller
public class UserController {

    private final UserService userService;
    private final EventService eventService;
    private StorageService storageService;
    private SondercharakterRepository sondercharakterRepository;
    private UserRepository userRepository;
    private UnitRepository unitRepository;


    public UserController(UserService userService, EventService eventService, StorageService storageService, SondercharakterRepository sondercharakterRepository, UserRepository userRepository, UnitRepository unitRepository) {
        this.userService = userService;
        this.eventService = eventService;
        this.storageService = storageService;
        this.sondercharakterRepository = sondercharakterRepository;
        this.userRepository = userRepository;
        this.unitRepository = unitRepository;
    }

    @GetMapping("/login")
    public String login(Model model, UserDto userDto) {
        model.addAttribute("user", userDto);
        return "login";
    }
    @GetMapping("/register_user")
    public String save(Model model, UserDto userDto) {
        model.addAttribute("user", userDto);
        return "register_user";
    }
    @PostMapping("/register_user")
    public String registerSave(@ModelAttribute("userDto") UserDto userDto,
                               @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
                               Model model) {
        System.out.println("➡️ Registriere Benutzer: " + userDto.getUsername());

        // Überprüfung, ob der Benutzername leer ist
        if (userDto.getUsername().trim().isEmpty()) {
            model.addAttribute("usernameError", "Benutzername darf nicht leer sein");
            model.addAttribute("userDto", userDto);
            return "register_user";
        }

        try {
            // Falls ein Bild hochgeladen wurde, speichern wir es
            if (mainImage != null && !mainImage.isEmpty()) {
                storageService.store(mainImage, "users/main");
                userDto.setPhotoPath("users/main/" + mainImage.getOriginalFilename());
            }
        } catch (Exception e) {
            System.out.println(" Fehler beim Hochladen der Datei: " + e.getMessage());
            model.addAttribute("error", "Dateiupload fehlgeschlagen: " + e.getMessage());
            model.addAttribute("userDto", userDto);
            return "register_user";
        }

        try {
            // Benutzerregistrierung durchführen
            userService.registerUser(userDto);
            System.out.println("Benutzer erfolgreich registriert!");
        } catch (Exception e) {
            System.out.println("Fehler bei der Registrierung des Benutzers: " + e.getMessage());
            model.addAttribute("error", "Registrierung fehlgeschlagen: " + e.getMessage());
            model.addAttribute("userDto", userDto);
            return "register_user";
        }

        // Nach erfolgreicher Registrierung weiterleiten
        return "redirect:/register_user?success";
    }



    @GetMapping("/user_home")
    public String userHome(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        List<Sondercharakter> sondercharakterList = sondercharakterRepository.findByUser(user.getId());

        String photoPath = (user.getPhotoPath() != null && !user.getPhotoPath().isEmpty())
                ? "/uploads/main/" + user.getPhotoPath()
                : "/uploads/default.jpg";

        model.addAttribute("photo", user.getPhotoPath());
        model.addAttribute("sondercharakterList", sondercharakterList);
        return "user_home";
    }

    @PostMapping("/join_event")
    public String joinEvent(@RequestParam String accessCode, Principal principal, Model model) {
        try {
            String username = principal.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                model.addAttribute("errorMessage", "Benutzer nicht gefunden.");
                return "user_home";
            }

            eventService.registerUserToEvent(accessCode, user.getId());

            model.addAttribute("successMessage", "Sie wurden erfolgreich zum Event hinzugefügt!");
        } catch (IllegalArgumentException e) {

            model.addAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/user_home?success";
    }

    @GetMapping("/user/edit")
    public String editUserProfile(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user", user);

        return "user_edit";
    }

    @PostMapping("/user/edit")
    public String updateUserProfile(
            @RequestParam("mainImage") MultipartFile mainImage,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByUsername(principal.getName());

            if (!mainImage.isEmpty()) {
                String newPhotoPath = "users/photos/" + mainImage.getOriginalFilename();
                storageService.store(mainImage, "users/photos");
                user.setPhotoPath(newPhotoPath);
            }

            user.setUsername(username);
            user.setEmail(email);
            userService.save(user);

            redirectAttributes.addFlashAttribute("successMessage", "Profil erfolgreich aktualisiert.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Aktualisieren des Profils: " + e.getMessage());
        }

        return "redirect:/user_home";
    }
}

