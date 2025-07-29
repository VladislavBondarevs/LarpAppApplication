package de.larphelden.larp_app.dto;

import de.larphelden.larp_app.models.Organisation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class OrganisationDto {
    private Long id;
    private String name;
    private String beschreibung;
    private MultipartFile bild;
    private String photoPath;
    private String kontakt;
    private String socialmedia;
    private String status;


    public OrganisationDto(Long id,String name, String beschreibung, String bildPath, String socialmedia, String kontakt, String status) {
        this.id = id;
        this.name = name;
        this.beschreibung = beschreibung;
        this.photoPath = bildPath;
        this.socialmedia = socialmedia;
        this.kontakt = kontakt;
        this.status = status;
    }

}
