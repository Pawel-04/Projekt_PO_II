package org.example.assetmanager.managers;

import org.example.assetmanager.util.HibernateUtil;
import org.example.assetmanager.entity.Item;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa zarządzająca operacjami CRUD na obiektach typu {@link Item}.
 * <p>
 * Wykorzystuje Hibernate do komunikacji z bazą danych. Każda operacja modyfikująca dane
 * (dodawanie, edycja, usuwanie) jest wykonywana w ramach bezpiecznej transakcji.
 * </p>
 */
public class ItemManager {

    /**
     * Pobiera listę wszystkich przedmiotów z bazy danych.
     *
     * @return lista obiektów {@link Item} lub pusta lista w przypadku błędu
     */
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            items = session.createQuery("from Item", Item.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Zapisuje nowy przedmiot w bazie danych.
     * <p>
     * Metoda otwiera transakcję, utrwala obiekt i zatwierdza zmiany (commit).
     * W przypadku błędu następuje wycofanie zmian (rollback).
     * </p>
     *
     * @param item Obiekt przedmiotu pobrany z formularza, który ma zostać zapisany.
     * @throws Exception Gdy wystąpi błąd połączenia z bazą lub naruszenie więzów integralności.
     */
    public void addItem(Item item) throws Exception {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(item);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    /**
     * Aktualizuje dane istniejącego przedmiotu.
     *
     * @param item Obiekt {@link Item} ze zmodyfikowanymi polami.
     * @throws Exception Gdy aktualizacja się nie powiedzie.
     */
    public void updateItem(Item item) throws Exception {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(item);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    /**
     * Usuwa wskazany przedmiot z bazy danych.
     *
     * @param item Obiekt {@link Item} przeznaczony do usunięcia.
     * @throws Exception Gdy wystąpi błąd podczas usuwania.
     */
    public void deleteItem(Item item) throws Exception {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(item);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }
}