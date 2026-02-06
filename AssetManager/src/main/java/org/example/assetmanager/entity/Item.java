package org.example.assetmanager.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Reprezentuje pojedynczy przedmiot (składnik majątku) w systemie ewidencji.
 * <p>
 * Klasa jest mapowana na tabelę <code>przedmioty</code> w bazie danych.
 * Przechowuje szczegółowe informacje o zasobie, takie jak wartość, gwarancja,
 * data zakupu oraz typ własności (Prywatny/Firmowy).
 * </p>
 */
@Entity
@Table(name = "przedmioty")
public class Item {

    /**
     * Unikalny identyfikator przedmiotu generowany automatycznie przez bazę danych.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String category;
    private String location;
    private LocalDate purchaseDate;
    private double value;
    private int amount;
    private int warranty;
    private String imagePath;
    private String type;

    /**
     * Bezargumentowy konstruktor wymagany przez mechanizm Hibernate.
     */
    public Item() {
    }

    /**
     * Tworzy nowy obiekt przedmiotu z pełnym zestawem danych.
     *
     * @param name         nazwa przedmiotu
     * @param category     kategoria przedmiotu
     * @param purchaseDate data zakupu
     * @param value        wartość jednostkowa (PLN)
     * @param amount       liczba sztuk
     * @param warranty     długość gwarancji w miesiącach
     * @param imagePath    ścieżka do pliku graficznego
     * @param type         typ własności (FIRMOWY/PRYWATNY)
     */
    public Item(String name, String category, String location, LocalDate purchaseDate, double value, int amount, int warranty, String imagePath, String type) {
        this.name = name;
        this.category = category;
        this.location = location;
        this.purchaseDate = purchaseDate;
        this.value = value;
        this.amount = amount;
        this.warranty = warranty;
        this.imagePath = imagePath;
        this.type = type;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getWarranty() {
        return warranty;
    }

    public void setWarranty(int warranty) {
        this.warranty = warranty;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Oblicza datę końca gwarancji.
     * Wykorzystywane przez tabelę do wyświetlania kolumny "Koniec Gwarancji".
     */
    public LocalDate getWarrantyEndDate() {
        if (purchaseDate == null) return null;
        return purchaseDate.plusMonths(warranty);
    }
}