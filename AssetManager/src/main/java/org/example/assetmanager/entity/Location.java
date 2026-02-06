package org.example.assetmanager.entity;

import jakarta.persistence.*;

/**
 * Reprezentuje lokalizację, w jakiej znajdują się przedmioty.
 * <p>
 * Klasa mapowana na tabelę <code>lokalizacja</code>.
 * </p>
 */
@Entity
@Table(name = "lokalizacja")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * Nazwa lokalizacji.
     * Musi być unikalna.
     */
    @Column(unique = true, nullable = false)
    private String name;

    /**
     * Konstruktor domyślny dla Hibernate.
     */
    public Location() {
    }

    public Location(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}