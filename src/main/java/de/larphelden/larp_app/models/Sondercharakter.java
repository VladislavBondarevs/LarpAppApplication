package de.larphelden.larp_app.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "sondercharaktere")
public class Sondercharakter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String beschreibung;

    private String photoPath;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false)
    private boolean anzeigen;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sondercharakter_requests", joinColumns = @JoinColumn(name = "sondercharakter_id"))
    @MapKeyJoinColumn(name = "user_id")
    @Column(name = "status")
    private Map<User, String> requests = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sondercharakter_reasons", joinColumns = @JoinColumn(name = "sondercharakter_id"))
    @MapKeyJoinColumn(name = "user_id")
    @Column(name = "reason")
    private Map<User, String> reasons = new HashMap<>();

}
