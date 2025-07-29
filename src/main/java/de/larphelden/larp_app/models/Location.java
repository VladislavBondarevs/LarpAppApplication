package de.larphelden.larp_app.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String beschreibung;

    private String photoPath;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> bildergallerie;

    @Column(nullable = false, length = 255)
    private String adresse;

    @Column(name = "telefonnummer")
    private String telefonnummer;

    private String email;

    @Column(length = 500)
    private String anfahrt;

    private String unterkunft;

    private boolean anzeigen;

    @Column(name = "aktualisiert_am")
    private LocalDateTime aktualisiertAm;

    private String status = "PENDING";

    @Column(name = "approved")
    private boolean approved = true;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> events;

    @PrePersist
    protected void onCreate() {
        this.aktualisiertAm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.aktualisiertAm = LocalDateTime.now();
    }
}
