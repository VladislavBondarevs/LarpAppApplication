package de.larphelden.larp_app.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bezeichnung;

    @Column(length = 500)
    private String beschreibung;

    private String photoPath;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> bildergallerie;


    private LocalDateTime datumVon;
    private LocalDateTime datumBis;

    private String eventAbhangigeAnfahrt;
    private String eventAbhangigeUnterkunft;

    @ManyToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organisator_id")
    private Organisation organisator;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id")
    private List<User> users = new ArrayList<>();

    private String typ;
    private String zeitspanne;
    private String genre;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Fraktion> fraktionen = new ArrayList<>();

    @OneToMany( cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Sondercharakter> sondercharaktere = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "events_users",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> user;

    private String status = "Pending";
    @Column(name = "approved")
    private boolean approved = true;

    @Column(unique = true, nullable = false)
    private String accessCode;

    public void addUser(User user) {
        if (this.users == null) {
            this.users = new ArrayList<>();
        }
        this.users.add(user);
        user.setEvent(this);
    }

}
