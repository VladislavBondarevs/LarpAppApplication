package de.larphelden.larp_app.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "fraktionen")
public class Fraktion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String beschreibung;

    @Column
    private String photoPath;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Unit> units = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "fraktion_users",
            joinColumns = @JoinColumn(name = "fraktion_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();
}
