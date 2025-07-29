package de.larphelden.larp_app.dto;

import de.larphelden.larp_app.models.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EventDto {
    private Long id;
    private String bezeichnung;
    private String beschreibung;
    private MultipartFile bild;
    private String bildPath;
    private MultipartFile mainImage;
    private List<MultipartFile> bildergallerie;
    private List<String> bildergalleriePaths;
    private LocalDateTime datumVon;
    private LocalDateTime datumBis;
    private String eventAbhangigeAnfahrt;
    private String eventAbhangigeUnterkunft;
    private Location location;
    private String typ;
    private String zeitspanne;
    private String genre;
    private Long organisatorId;
    private String status;
    private String accessCode;
    private List<String> userNames;

    public EventDto(Long id, String bezeichnung, String beschreibung, MultipartFile bild, String bildPath, List<MultipartFile>
            bildergallerie, List<String> bildergalleriePaths, LocalDateTime datumVon,
                    LocalDateTime datumBis, String eventAbhangigeAnfahrt, String eventAbhangigeUnterkunft, Location location, String typ,
                    String zeitspanne, String genre, Long organisatorId, String status, String accessCode) {
        this.id = id;
        this.bezeichnung = bezeichnung;
        this.beschreibung = beschreibung;
        this.bild = bild;
        this.bildPath = bildPath;
        this.bildergallerie = bildergallerie;
        this.bildergalleriePaths = bildergalleriePaths;
        this.datumVon = datumVon;
        this.datumBis = datumBis;
        this.eventAbhangigeAnfahrt = eventAbhangigeAnfahrt;
        this.eventAbhangigeUnterkunft = eventAbhangigeUnterkunft;
        this.location = location;
        this.typ = typ;
        this.zeitspanne = zeitspanne;
        this.genre = genre;
        this.organisatorId = organisatorId;
        this.status = status;
        this.accessCode = accessCode;
    }
}
