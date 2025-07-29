package de.larphelden.larp_app.controllers;

import de.larphelden.larp_app.dto.LocationDto;
import de.larphelden.larp_app.models.Location;
import de.larphelden.larp_app.services.LocationService;
import de.larphelden.larp_app.services.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Controller
public class LocationController {

    private final LocationService locationService;
    private StorageService storageService;

    public LocationController(LocationService locationService, StorageService storageService) {
        this.locationService = locationService;
        this.storageService = storageService;
    }


    @GetMapping("/location_list")
    public String listLocations(Model model) {
        List<Location> locations = locationService.getAllLocations();
        model.addAttribute("locations", locations);
        return "location_list";
    }

    @GetMapping("/location_view/{id}")
    public String viewLocation(@PathVariable Long id, Model model) {
        Location location = locationService.getLocationById(id);
        model.addAttribute("location", location);
        model.addAttribute("bildergallerie", location.getBildergallerie());

        return "location_view";
    }

    @GetMapping("/register_location")
    public String createLocationForm(Model model) {
        model.addAttribute("locationDto", new LocationDto());
        return "register_location";
    }

    @PostMapping("/register_location")
    public String createLocation(@ModelAttribute LocationDto locationDto,
                                 @RequestParam("bildergallerie") List<MultipartFile> bildergallerie,
                                 @RequestParam("mainImage") MultipartFile mainImage,
                                 Model model) {
        try {
            Location location = new Location();

            if (!mainImage.isEmpty()) {
                storageService.store(mainImage, "location/main");
                location.setPhotoPath("location/main/" + mainImage.getOriginalFilename());
            } else {
                location.setPhotoPath("location/main/default-placeholder.png");
            }

            List<String> galleryPaths = new ArrayList<>();
            if (bildergallerie != null && !bildergallerie.isEmpty()) {
                for (MultipartFile file : bildergallerie) {
                    if (!file.isEmpty()) {
                        storageService.store(file, "location/gallery");
                        galleryPaths.add("location/gallery/" + file.getOriginalFilename());
                    }
                }
            }
            location.setBildergallerie(galleryPaths);

            location.setName(locationDto.getName());
            location.setBeschreibung(locationDto.getBeschreibung());
            location.setAdresse(locationDto.getAdresse());
            location.setTelefonnummer(locationDto.getTelefonnummer());
            location.setEmail(locationDto.getEmail());
            location.setAnfahrt(locationDto.getAnfahrt());
            location.setUnterkunft(locationDto.getUnterkunft());
            location.setAnzeigen(locationDto.isAnzeigen());
            location.setStatus("PENDING");
            location.setApproved(false);

            locationService.createLocation(location);

            return "redirect:/location_request";

        } catch (Exception e) {
            model.addAttribute("error", "Fehler beim Speichern der Location: " + e.getMessage());
            return "redirect:/location_request";
        }

    }

    // Methode zur Überprüfung des Dateityps
    private boolean isValidImageType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null &&
                (contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jfif"));
    }

    @GetMapping("/location_edit/{id}")
    public String editLocationForm(@PathVariable Long id, Model model) {
        Location location = locationService.getLocationById(id);
        model.addAttribute("location", location);
        return "location_edit";
    }

    @PostMapping("/location_edit/{id}")
    public String updateLocation(
            @PathVariable Long id,
            @ModelAttribute LocationDto locationDto,
            @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestParam(value = "bildergallerie", required = false) List<MultipartFile> bildergallerie,
            @RequestParam(value = "existingGalleryImages", required = false) List<String> existingGalleryImages,
            RedirectAttributes redirectAttributes) {

        try {
            if (mainImage != null && !mainImage.isEmpty()) {
                String mainImagePath = "location/main/" + mainImage.getOriginalFilename();
                storageService.store(mainImage, "location/main");
                locationDto.setPhotoPath(mainImagePath);
            } else {
                locationDto.setPhotoPath(locationService.getLocationById(id).getPhotoPath());
            }

            List<String> updatedGalleryPaths = new ArrayList<>();
            if (existingGalleryImages != null && !existingGalleryImages.isEmpty()) {

                updatedGalleryPaths.addAll(existingGalleryImages);
            }
            if (bildergallerie != null && !bildergallerie.isEmpty()) {

                for (MultipartFile file : bildergallerie) {
                    if (!file.isEmpty()) {
                        String galleryPath = "location/gallery/" + file.getOriginalFilename();
                        storageService.store(file, "location/gallery");
                        updatedGalleryPaths.add(galleryPath);
                    }
                }
            }
            locationDto.setBildergalleriePaths(updatedGalleryPaths);

            locationService.updateLocation(id, locationDto);

            redirectAttributes.addFlashAttribute("successMessage", "Location erfolgreich aktualisiert.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Aktualisieren der Location: " + e.getMessage());
        }
        return "redirect:/location_edit/" + id;
    }



    @PostMapping("/location_delete/{id}")
    public String deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return "redirect:/location_list";
    }

    @GetMapping("/location_request")
    public String showLocationRequestPage(Model model) {
        List<LocationDto> pendingLocations = locationService.findPendingRequests();
        model.addAttribute("pendingLocations", pendingLocations);
        return "location_request";
    }


    @PostMapping("/approve_location/{id}")
    public String approveLocation(@PathVariable Long id) {
        locationService.approveRequest(id);
        return "redirect:/location_request";
    }

    @PostMapping("/reject_location/{id}")
    public String rejectLocation(@PathVariable Long id) {
        locationService.rejectRequest(id);
        return "redirect:/location_request";
    }

    @PostMapping("/location_view/{id}")
    public String viewLocationForm(@PathVariable Long id, Model model) {
        Location location = locationService.getLocationById(id);

        if (location == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Location nicht gefunden");
        }
        List<String> gallery = location.getBildergallerie();
        if (gallery == null || gallery.isEmpty()) {
            gallery = List.of("default-placeholder.png");
        }

        model.addAttribute("location", location);
        model.addAttribute("bildergallerie", location.getBildergallerie());
        return "location_view";
    }

}
