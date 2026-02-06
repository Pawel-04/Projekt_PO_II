package org.example.assetmanager.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Klasa dostarczająca globalny obiekt {@link SessionFactory} dla Hibernate.
 * <p>
 * Odpowiada za wczytanie konfiguracji z pliku <code>hibernate.cfg.xml</code>
 * oraz zarządzanie cyklem życia połączenia z bazą danych.
 * </p>
 */
public class HibernateUtil {

    /**
     * Statyczna pole fabryki sesji, inicjalizowane przy starcie aplikacji.
     */
    private static final SessionFactory sessionFactory = buildSessionFactory();

    /**
     * Buduje fabrykę sesji na podstawie pliku konfiguracyjnego.
     *
     * @return skonfigurowany obiekt <code>SessionFactory</code>
     * @throws ExceptionInInitializerError jeśli inicjalizacja się nie powiedzie (np. brak bazy)
     */
    private static SessionFactory buildSessionFactory() {
        try {
            // Tworzy sesję z pliku hibernate.cfg.xml
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Nie udało się utworzyć sesji." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Pobiera globalną instancję fabryki sesji.
     *
     * @return obiekt {@link SessionFactory}
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Zamyka fabrykę sesji i zwalnia zasoby (połączenia z bazą danych).
     */
    public static void shutdown() {
        getSessionFactory().close();
    }
}