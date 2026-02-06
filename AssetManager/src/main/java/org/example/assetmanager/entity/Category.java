package org.example.assetmanager.entity;

import jakarta.persistence.*;

/**
 * Reprezentuje kategorię, do której mogą być przypisane przedmioty.
 * <p>
 * Klasa mapowana na tabelę <code>kategoria</code>. Nazwy kategorii muszą być unikalne.
 * Służy do grupowania majątku w statystykach.
 * </p>
 */
@Entity
@Table(name = "kategoria")
public class Category {

    /**
     * Unikalny identyfikator kategorii.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * Unikalna nazwa kategorii (np. "Elektronika", "Meble").
     * Pole nie może być puste (nullable = false).
     */
    @Column(unique = true, nullable = false)
    private String name;

    /**
     * Konstruktor domyślny dla Hibernate.
     */
    public Category() {
    }

    public Category(String name) {
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

    /**
     * Zwraca tekstową reprezentację kategorii (jej nazwę).
     * Używane przy wyświetlaniu w komponentach UI takich jak ComboBox.
     *
     * @return nazwa kategorii
     */
    @Override
    public String toString() {
        return name;
    }
}
