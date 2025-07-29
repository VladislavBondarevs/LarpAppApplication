package de.larphelden.larp_app.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;
    @Column(nullable = false)
    private String role = "USER";
    private String photoPath;

    @ManyToOne
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToMany(mappedBy = "users")
    private Set<Fraktion> fraktionen = new HashSet<>();

    @ManyToMany(mappedBy = "users")
    private Set<Unit> units = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Sondercharakter> sondercharakterList;

    public User(String username, String password, String email, String role, String photoPath) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.photoPath = photoPath;
    }
    public User() {
    }

    public void removeFraktion(Fraktion fraktion) {
        this.fraktionen.remove(fraktion);
        fraktion.getUsers().remove(this);
    }

    public void removeUnit(Unit unit) {
        this.units.remove(unit);
        unit.getUsers().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

