package org.example.assetmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Główna klasa uruchomieniowa aplikacji "System Zarządzania Majątkiem".
 * Klasa dziedziczy po {@link Application} i stanowi punkt wejścia do aplikacji JavaFX.
 * Odpowiada za konfigurację głównego okna (Stage), załadowanie pliku widoku (FXML)
 * oraz ustawienie parametrów startowych sceny.
 */
public class MainApp extends Application {

    /**
     * Główna metoda wejściowa programu (entry point).
     * Wywołuje metodę {@link #launch(String...)}, która inicjuje cykl życia aplikacji JavaFX.
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Metoda startowa aplikacji JavaFX.
     * Jest wywoływana po zainicjowaniu systemu. Tworzy główną scenę na podstawie
     * pliku <code>MainView.fxml</code>, ustawia tytuł okna oraz jego wymiary,
     * a następnie wyświetla aplikację użytkownikowi.
     *
     * @param stage główne okno aplikacji dostarczone przez platformę JavaFX
     * @throws IOException rzucany w przypadku niepowodzenia ładowania pliku FXML (np. brak pliku)
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 700);

        stage.setTitle("System Zarządzania Majątkiem - Ewidencja");
        stage.setScene(scene);
        stage.show();
    }
}