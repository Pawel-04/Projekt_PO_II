package org.example.assetmanager.managers;

import org.example.assetmanager.util.HibernateUtil;
import org.example.assetmanager.entity.Location;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.ArrayList;
import java.util.List;

/**
 * Zarządza lokalizacjami w systemie oraz ich powiązaniami z przedmiotami.
 * <p>
 * Klasa odpowiada za spójność danych np. przy zmianie nazwy lokalizacji,
 * aktualizuje ona przypisanie we wszystkich powiązanych przedmiotach.
 * </p>
 */
public class LocationManager {

    /**
     * Pobiera listę nazw wszystkich dostępnych lokalizacji.
     *
     * @return lista nazw lokalizacji jako <code>List&lt;String&gt;</code>
     */
    public List<String> getAllNames() {
        List<String> names = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Pobieramy obiekty Location i wyciągamy z nich same nazwy
            List<Location> locations = session.createQuery("from Location", Location.class).list();
            for (Location loc : locations) {
                names.add(loc.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return names;
    }

    /**
     * Tworzy i zapisuje nową lokalizację w bazie danych.
     * Wykorzystuje transakcję do trwałego zapisania nowej encji {@link Location}.
     *
     * @param name Nazwa nowej lokalizacji do dodania.
     * @throws Exception Gdy wystąpi błąd zapisu (np. lokalizacja o tej nazwie już istnieje).
     */
    public void addLocation(String name) throws Exception {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(new Location(name));
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    /**
     * Zmienia nazwę istniejącej lokalizacji.
     * <p>
     * Operacja jest kaskadowa, wykonuje dwa zapytania:
     * 1. Aktualizuje tabelę lokalizacji.
     * 2. Aktualizuje pole 'location' we wszystkich przedmiotach, które miały starą nazwę.
     * </p>
     *
     * @param oldName obecna nazwa lokalizacji
     * @param newName nowa nazwa lokalizacji
     * @throws Exception w przypadku błędu transakcji
     */
    public void updateLocation(String oldName, String newName) throws Exception {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // 1. Aktualizacja nazwy w tabeli słownikowej
            session.createQuery("update Location set name=:n where name=:o")
                    .setParameter("n", newName)
                    .setParameter("o", oldName)
                    .executeUpdate();

            // 2. Aktualizacja przedmiotów przypisanych do tej lokalizacji
            session.createQuery("update Item set location=:n where location=:o")
                    .setParameter("n", newName)
                    .setParameter("o", oldName)
                    .executeUpdate();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    /**
     * Usuwa lokalizację, pod warunkiem, że nie jest ona używana przez żaden przedmiot.
     *
     * @param name nazwa lokalizacji do usunięcia
     * @return <code>true</code> jeśli usunięto pomyślnie, <code>false</code> jeśli lokalizacja jest przypisana do przedmiotów
     */
    public boolean deleteLocation(String name) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Long count = session.createQuery("select count(i) from Item i where i.location=:c", Long.class)
                    .setParameter("c", name)
                    .uniqueResult();

            if (count > 0) return false;

            session.createQuery("delete from Location where name=:n")
                    .setParameter("n", name)
                    .executeUpdate();

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }
}