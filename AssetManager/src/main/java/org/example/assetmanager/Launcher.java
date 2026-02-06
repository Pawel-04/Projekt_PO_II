package org.example.assetmanager;

/**
 * Klasa startowa (Wrapper) aplikacji.
 * <p>
 * Służy jako punkt wejścia dla pliku JAR. Jej zadaniem jest wywołanie głównej klasy
 * {@link MainApp}, co pozwala ominąć problemy z ładowaniem modułów JavaFX
 * przy uruchamianiu aplikacji jako "Fat Jar".
 * </p>
 */
public class Launcher {

    /**
     * Główna metoda startowa programu.
     * Przekazuje sterowanie do metody main w klasie {@link MainApp}.
     *
     * @param args argumenty wiersza poleceń przekazane przy uruchomieniu
     */
    public static void main(String[] args) {
        MainApp.main(args);
    }
}