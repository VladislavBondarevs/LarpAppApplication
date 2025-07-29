package de.larphelden.larp_app.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "organisation")
public class Organisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String beschreibung;
    @Column
    private String photoPath;
    private String kontakt;
    private String socialmedia;
    private String status = "Pending";
    @Column(name = "approved")
    private boolean approved = true;
    
    @OneToMany(mappedBy = "organisation")
    private List<User> users = new ArrayList<>();

    public Organisation() {
    }

    public Organisation(List<User> users, boolean approved, String status, String socialmedia, String kontakt,
                        String photoPath, String beschreibung, String name, Long id) {
        this.users = users;
        this.approved = approved;
        this.status = status;
        this.socialmedia = socialmedia;
        this.kontakt = kontakt;
        this.photoPath = photoPath;
        this.beschreibung = beschreibung;
        this.name = name;
        this.id = id;
    }
}
