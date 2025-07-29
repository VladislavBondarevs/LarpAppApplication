package de.larphelden.larp_app.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class LocationDto {
    private Long id;
    private String name;
    private String beschreibung;
    private MultipartFile bild;
    private List<MultipartFile> bildergallerie;
    private List<String> bildergalleriePaths;
    private String photoPath;
    private String adresse;
    private String telefonnummer;
    private String email;
    private String anfahrt;
    private String unterkunft;
    private boolean anzeigen;
    private String status;

    public LocationDto(Long id, String name, String beschreibung, String photoPath, String adresse,
                       String telefonnummer, String email, String anfahrt, String unterkunft,
                       boolean anzeigen, String status) {
        this.id = id;
        this.name = name;
        this.beschreibung = beschreibung;
        this.photoPath = photoPath;
        this.adresse = adresse;
        this.telefonnummer = telefonnummer;
        this.email = email;
        this.anfahrt = anfahrt;
        this.unterkunft = unterkunft;
        this.anzeigen = anzeigen;
        this.status = status;
    }

    public LocationDto(){

    }
}
