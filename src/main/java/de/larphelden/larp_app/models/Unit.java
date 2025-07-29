package de.larphelden.larp_app.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "units")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String beschreibung;

    @Column
    private String photoPath;

    @Column(nullable = false)
    private int teilnehmerAnzahl;

    @ManyToOne
    @JoinColumn(name = "fraktion_id")
    private Fraktion fraktion;

    private int Capacity;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "unit_users",
            joinColumns = @JoinColumn(name = "unit_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();

    public int getOccupiedSeats() {
        return users.size();
    }

    public void updateTeilnehmerAnzahl() {
        this.teilnehmerAnzahl = this.users.size();
    }
}
