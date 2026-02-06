package org.example.assetmanager.managers;

import org.example.assetmanager.util.HibernateUtil;
import org.example.assetmanager.entity.Category;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.ArrayList;
import java.util.List;

/**
 * Zarządza kategoriami w systemie oraz ich powiązaniami z przedmiotami.
 * <p>
 * Klasa odpowiada za spójność danych np. przy zmianie nazwy kategorii,
 * aktualizuje ona przypisanie we wszystkich powiązanych przedmiotach.
 * </p>
 */
public class CategoryManager {

    /**
     * Pobiera listę nazw wszystkich dostępnych kategorii.
     *
     * @return lista nazw kategorii jako <code>List&lt;String&gt;</code>
     */
    public List<String> getAllNames() {
        List<String> names = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Category> categories = session.createQuery("from Category", Category.class).list();
            for (Category c : categories) {
                names.add(c.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return names;
    }

    /**
     * Tworzy i zapisuje nową kategorię w bazie danych.
     * Wykorzystuje transakcję do trwałego zapisania nowej encji {@link Category}.
     *
     * @param name Nazwa nowej kategorii do dodania.
     * @throws Exception Gdy wystąpi błąd zapisu (np. kategoria o tej nazwie już istnieje).
     */
    public void addCategory(String name) throws Exception {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(new Category(name));
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    /**
     * Zmienia nazwę istniejącej kategorii.
     * <p>
     * Operacja jest kaskadowa, wykonuje dwa zapytania:
     * 1. Aktualizuje tabelę kategorii.
     * 2. Aktualizuje pole 'category' we wszystkich przedmiotach, które miały starą nazwę.
     * </p>
     *
     * @param oldName obecna nazwa kategorii
     * @param newName nowa nazwa kategorii
     * @throws Exception w przypadku błędu transakcji
     */
    public void updateCategory(String oldName, String newName) throws Exception {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.createQuery("update Category set name=:n where name=:o")
                    .setParameter("n", newName).setParameter("o", oldName).executeUpdate();

            session.createQuery("update Item set category=:n where category=:o")
                    .setParameter("n", newName).setParameter("o", oldName).executeUpdate();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    /**
     * Usuwa kategorię, pod warunkiem, że nie jest ona używana przez żaden przedmiot.
     *
     * @param name nazwa kategorii do usunięcia
     * @return <code>true</code> jeśli usunięto pomyślnie, <code>false</code> jeśli kategoria jest przypisana do przedmiotów
     */
    public boolean deleteCategory(String name) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Long count = session.createQuery("select count(i) from Item i where i.category=:c", Long.class)
                    .setParameter("c", name)
                    .uniqueResult();

            if (count > 0) return false;

            session.createQuery("delete from Category where name=:n")
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