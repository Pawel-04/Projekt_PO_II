package org.example.assetmanager.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.example.assetmanager.util.HibernateUtil;
import org.example.assetmanager.entity.Item;
import org.example.assetmanager.managers.CategoryManager;
import org.example.assetmanager.managers.ItemManager;
import org.example.assetmanager.managers.LocationManager;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Główny kontroler aplikacji.
 * Klasa odpowiada za logikę interfejsu użytkownika, obsługę zdarzeń (kliknięcia, formularze)
 * oraz komunikację z warstwą danych (CategoryManager, ItemManager).
 * Zarządza widokami: Dashboard, Tabela, Formularz, Szczegóły.
 */
public class MainController {

    // DANE I MANAGERY
    private static boolean trybFirmowy = false;
    private static Item selectedItem = null;
    private static String currentFilterMode = "ALL";
    private static boolean trybEdycji = false;
    private static String tempSciezkaDoZdjecia = null;
    private static ObservableList<String> listaKategorii = FXCollections.observableArrayList();
    private static ObservableList<String> listaLokalizacji = FXCollections.observableArrayList();
    private static ObservableList<Item> listaPrywatna = FXCollections.observableArrayList();
    private static ObservableList<Item> listaFirmowa = FXCollections.observableArrayList();
    private final CategoryManager categoryManager = new CategoryManager();
    private final LocationManager locationManager = new LocationManager();
    private final ItemManager itemManager = new ItemManager();

    /**
     * Główny kontener aplikacji, w którym podmieniane są widoki (Pulpit, Tabela, Formularz).
     * Zdefiniowany w pliku <code>MainView.fxml</code>.
     */
    @FXML
    private BorderPane mainBorderPane;
    // SIDEBAR
    @FXML
    private Button btnDashboard, btnPrivate, btnCompany, btnCategories, btnLocations;
    // DASHBOARD
    @FXML
    private Label totalItemsLabel, totalValueLabel;
    @FXML
    private PieChart categoryPieChart;
    @FXML
    private Button btnAll, btnYear, btnMonth;
    // TABELA
    @FXML
    private TextField searchField;
    @FXML
    private TableView<Item> table;
    @FXML
    private TableColumn<Item, Integer> idColumn;
    @FXML
    private TableColumn<Item, String> nameColumn;
    @FXML
    private TableColumn<Item, String> categoryColumn;
    @FXML
    private TableColumn<Item, String> locationColumn;
    @FXML
    private TableColumn<Item, Double> valueColumn;
    @FXML
    private TableColumn<Item, Integer> amountColumn;
    @FXML
    private TableColumn<Item, LocalDate> warrantyColumn;
    // PODSUMOWANIE
    @FXML
    private Label viewTotalLabel;
    @FXML
    private Label upcoming1Label, upcoming2Label, upcoming3Label;
    @FXML
    private Label expired1Label, expired2Label, expired3Label;
    // SZCZEGÓŁY
    @FXML
    private Label detailName, detailCategory, detailAmount, detailPrice, detailDate, detailWarranty, detailTotalValue;
    @FXML
    private ComboBox<String> locationBox;
    @FXML
    private Label detailLocation;
    @FXML
    private ImageView detailImageView;
    @FXML
    private Label noImageLabel;
    // FORMULARZ
    @FXML
    private TextField nazwaInput;
    @FXML
    private ComboBox<String> kategoriaBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField wartoscInput, iloscInput, gwarancjaInput;
    @FXML
    private Label filePathLabel;
    // KATEGORIE
    @FXML
    private TextField categoryInput;
    @FXML
    private ListView<String> categoryListView;
    @FXML
    private TextField locationInput;
    @FXML
    private ListView<String> locationListView;

    /**
     * Metoda inicjalizująca kontroler JavaFX.
     * Wywoływana automatycznie po załadowaniu pliku FXML. Konfiguruje:
     * Style CSS i wygląd przycisków.
     * Pobranie danych z bazy (kategorie, przedmioty).
     * Mapowanie kolumn tabeli (id, nazwa, kategoria itd.).
     * Filtrowanie i sortowanie listy przedmiotów.
     */
    @FXML
    public void initialize() {
        if (mainBorderPane != null) {
            try {
                String css = getClass().getResource("/org/example/assetmanager/style.css").toExternalForm();
                mainBorderPane.getStylesheets().add(css);
            } catch (Exception e) {
                System.out.println("Brak stylu CSS");
            }
        }
        setupButtonStyles();

        listaKategorii.clear();
        listaKategorii.addAll(categoryManager.getAllNames());

        listaLokalizacji.clear();
        listaLokalizacji.addAll(locationManager.getAllNames());
        loadDataFromDatabase();

        if (mainBorderPane != null) loadView("DashboardView.fxml");

        if (totalItemsLabel != null) {
            categoryPieChart.setAnimated(false);
            updateDashboard();
            updateFilterButtons();
        }
        if (btnDashboard != null) {
            resetSidebarStyles();
            btnDashboard.getStyleClass().add("active");
        }

        if (table != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            if (locationColumn != null) {
                locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
            }
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            warrantyColumn.setCellValueFactory(new PropertyValueFactory<>("warrantyEndDate"));

            ObservableList<Item> aktualnaLista = trybFirmowy ? listaFirmowa : listaPrywatna;

            FilteredList<Item> filteredData = new FilteredList<>(aktualnaLista, p -> true);
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filteredData.setPredicate(item -> {
                        if (newValue == null || newValue.isEmpty()) return true;
                        String lower = newValue.toLowerCase();
                        return item.getName().toLowerCase().contains(lower) ||
                                item.getCategory().toLowerCase().contains(lower);
                    });
                    updateViewStatistics(filteredData);
                });
            }
            SortedList<Item> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(table.comparatorProperty());
            table.setItems(sortedData);
            updateViewStatistics(aktualnaLista);
        }

        if (detailName != null && selectedItem != null) fillDetailsView();
        if (nazwaInput != null) setupForm();
        if (categoryListView != null) setupCategoryView();
        if (locationListView != null) setupLocationCRUDView();
    }

    /**
     * Konfiguruje style CSS dla przycisków w interfejsie.
     * Przypisuje odpowiednie klasy stylów ("sidebar-button", "filter-button")
     * do przycisków nawigacyjnych i filtrów, aby zapewnić spójny wygląd aplikacji.
     */
    private void setupButtonStyles() {
        if (btnDashboard != null) btnDashboard.getStyleClass().add("sidebar-button");
        if (btnPrivate != null) btnPrivate.getStyleClass().add("sidebar-button");
        if (btnCompany != null) btnCompany.getStyleClass().add("sidebar-button");
        if (btnCategories != null) btnCategories.getStyleClass().add("sidebar-button");
        if (btnLocations != null) btnLocations.getStyleClass().add("sidebar-button");
        if (btnAll != null) btnAll.getStyleClass().add("filter-button");
        if (btnYear != null) btnYear.getStyleClass().add("filter-button");
        if (btnMonth != null) btnMonth.getStyleClass().add("filter-button");
    }

    /**
     * Przygotowuje formularz dodawania lub edycji przedmiotu.
     * Wypełnia listę rozwijaną (ComboBox) dostępnymi kategoriami.
     * Jeśli aplikacja jest w trybie edycji, metoda uzupełnia pola tekstowe
     * danymi wybranego wcześniej przedmiotu (nazwa, cena, data, zdjęcie).
     */
    private void setupForm() {
        kategoriaBox.setItems(listaKategorii);
        locationBox.setItems(listaLokalizacji);
        if (trybEdycji && selectedItem != null) {
            nazwaInput.setText(selectedItem.getName());
            kategoriaBox.setValue(selectedItem.getCategory());
            locationBox.setValue(selectedItem.getLocation());
            datePicker.setValue(selectedItem.getPurchaseDate());
            wartoscInput.setText(String.valueOf(selectedItem.getValue()));
            iloscInput.setText(String.valueOf(selectedItem.getAmount()));
            gwarancjaInput.setText(String.valueOf(selectedItem.getWarranty()));
            if (selectedItem.getImagePath() != null) {
                tempSciezkaDoZdjecia = selectedItem.getImagePath();
                filePathLabel.setText("Wybrano plik");
            }
        } else {
            tempSciezkaDoZdjecia = null;
        }
    }

    /**
     * Konfiguruje widok zarządzania kategoriami.
     * Ładuje listę kategorii z menedżera i dodaje nasłuchiwacz do listy,
     * aby po kliknięciu w kategorię jej nazwa pojawiała się w polu edycji.
     */
    private void setupCategoryView() {
        listaKategorii.clear();
        listaKategorii.addAll(categoryManager.getAllNames());
        categoryListView.setItems(listaKategorii);
        categoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) categoryInput.setText(newVal);
        });
    }

    /**
     * Konfiguruje widok zarządzania lokalizacjami.
     * Ładuje listę lokalizacji z menedżera i dodaje nasłuchiwacz do listy,
     * aby po kliknięciu w lokalizację jej nazwa pojawiała się w polu edycji.
     */
    private void setupLocationCRUDView() {
        listaLokalizacji.clear();
        listaLokalizacji.addAll(locationManager.getAllNames());
        locationListView.setItems(listaLokalizacji);
        locationListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && locationInput != null) locationInput.setText(newVal);
        });
    }

    /**
     * Pobiera dane z bazy i rozdziela je na listy lokalne.
     * Metoda czyści obecne listy, pobiera wszystkie przedmioty przez {@link ItemManager},
     * a następnie segreguje je na listę firmową i prywatną w zależności od pola <code>type</code>.
     */
    private void loadDataFromDatabase() {
        listaPrywatna.clear();
        listaFirmowa.clear();
        List<Item> dbList = itemManager.getAllItems();
        for (Item item : dbList) {
            if ("FIRMOWY".equals(item.getType())) listaFirmowa.add(item);
            else listaPrywatna.add(item);
        }
    }

    // OBSŁUGA KATEGORII

    /**
     * Obsługuje zdarzenie dodania nowej kategorii.
     * Sprawdza, czy nazwa nie jest pusta i czy taka kategoria już nie istnieje.
     * Jeśli walidacja przebiegnie pomyślnie, zleca zapis do bazy danych.
     *
     * @param event zdarzenie kliknięcia przycisku "Dodaj"
     */
    @FXML
    public void handleAddCategory(ActionEvent event) {
        String nowaNazwa = categoryInput.getText();
        if (nowaNazwa == null || nowaNazwa.trim().isEmpty()) {
            showAlert("Błąd", "Wpisz nazwę.");
            return;
        }
        if (listaKategorii.contains(nowaNazwa)) {
            showAlert("Błąd", "Już istnieje.");
            return;
        }
        try {
            categoryManager.addCategory(nowaNazwa);
            listaKategorii.add(nowaNazwa);
            categoryInput.clear();
            showAlert("Sukces", "Dodano kategorię.");
        } catch (Exception e) {
            showAlert("Błąd", "Błąd bazy danych.");
        }
    }

    /**
     * Obsługuje edycję nazwy istniejącej kategorii.
     * Zmienia nazwę wybranej kategorii na nową wartość wpisaną w polu tekstowym.
     * Zmiana jest odzwierciedlana w bazie danych oraz na liście w interfejsie.
     *
     * @param event zdarzenie kliknięcia przycisku "Edytuj"
     */
    @FXML
    public void handleUpdateCategory(ActionEvent event) {
        String stara = categoryListView.getSelectionModel().getSelectedItem();
        String nowa = categoryInput.getText();
        if (stara == null || nowa == null || nowa.isEmpty() || stara.equals(nowa)) return;
        try {
            categoryManager.updateCategory(stara, nowa);
            int idx = listaKategorii.indexOf(stara);
            if (idx >= 0) listaKategorii.set(idx, nowa);
            showAlert("Sukces", "Zmieniono nazwę.");
        } catch (Exception e) {
            showAlert("Błąd", "Błąd edycji.");
        }
    }

    /**
     * Obsługuje usuwanie wybranej kategorii.
     * Przed usunięciem weryfikuje, czy kategoria nie jest przypisana do żadnych przedmiotów.
     * Jeśli jest używana, usuwanie zostaje zablokowane, aby zachować spójność danych.
     *
     * @param event zdarzenie kliknięcia przycisku "Usuń"
     */
    @FXML
    public void handleDeleteCategory(ActionEvent event) {
        String selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        boolean deleted = categoryManager.deleteCategory(selected);
        if (deleted) {
            listaKategorii.remove(selected);
            categoryInput.clear();
        } else {
            showAlert("Odmowa", "Kategoria jest używana!");
        }
    }

    // OBSŁUGA LOKALIZACJI

    /**
     * Obsługuje zdarzenie dodania nowej loakzlizacji.
     * Sprawdza, czy nazwa nie jest pusta i czy taka lokalizacja już nie istnieje.
     * Jeśli walidacja przebiegnie pomyślnie, zleca zapis do bazy danych.
     *
     * @param event zdarzenie kliknięcia przycisku "Dodaj"
     */
    @FXML
    public void handleAddLocation(ActionEvent event) {
        String nowa = locationInput.getText();
        if (nowa == null || nowa.trim().isEmpty()) {
            showAlert("Błąd", "Wpisz nazwę lokalizacji.");
            return;
        }
        if (listaLokalizacji.contains(nowa)) {
            showAlert("Błąd", "Taka lokalizacja już istnieje.");
            return;
        }
        try {
            locationManager.addLocation(nowa);
            listaLokalizacji.add(nowa);
            locationInput.clear();
            showAlert("Sukces", "Dodano lokalizację.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Błąd", "Błąd bazy danych.");
        }
    }


    /**
     * Obsługuje edycję nazwy istniejącej lokalizacji.
     * Zmienia nazwę wybranej lokalizacji na nową wartość wpisaną w polu tekstowym.
     * Zmiana jest odzwierciedlana w bazie danych oraz na liście w interfejsie.
     *
     * @param event zdarzenie kliknięcia przycisku "Edytuj"
     */
    @FXML
    public void handleUpdateLocation(ActionEvent event) {
        String stara = locationListView.getSelectionModel().getSelectedItem();
        String nowa = locationInput.getText();

        // Walidacja: czy coś wybrano i czy nazwa jest inna
        if (stara == null || nowa == null || nowa.trim().isEmpty() || stara.equals(nowa)) {
            return;
        }

        try {
            locationManager.updateLocation(stara, nowa);

            // Aktualizacja listy w widoku (bez ponownego pobierania z bazy)
            int idx = listaLokalizacji.indexOf(stara);
            if (idx >= 0) {
                listaLokalizacji.set(idx, nowa);
            }

            showAlert("Sukces", "Zmieniono nazwę lokalizacji.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Błąd", "Błąd edycji (może nazwa jest już zajęta?).");
        }
    }

    /**
     * Obsługuje usuwanie wybranej lokalizacji.
     * Przed usunięciem weryfikuje, czy lokalizacja nie jest przypisana do żadnych przedmiotów.
     * Jeśli jest używana, usuwanie zostaje zablokowane, aby zachować spójność danych.
     *
     * @param event zdarzenie kliknięcia przycisku "Usuń"
     */
    @FXML
    public void handleDeleteLocation(ActionEvent event) {
        String selected = locationListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        boolean deleted = locationManager.deleteLocation(selected);
        if (deleted) {
            listaLokalizacji.remove(selected);
            locationInput.clear();
        } else {
            showAlert("Błąd", "Lokalizacja jest przypisana do przedmiotów!\nUsuń je lub przenieś najpierw.");
        }
    }

    /**
     * Obsługuje proces zapisu (dodawania lub edycji) zasobu.
     * <p>
     * Wykonuje następujące kroki:
     * Pobiera dane z pól tekstowych formularza.
     * Waliduje poprawność danych (wymagana nazwa i cena).
     * Konwertuje typy danych (String na double/int).
     * Tworzy lub aktualizuje obiekt {@link Item} przy użyciu {@link ItemManager}.
     * </p>
     *
     * @param event zdarzenie wywołujące (kliknięcie przycisku do bazy)
     */
    @FXML
    public void saveNewItem(ActionEvent event) {
        try {
            // Walidacja, czy wpisano nazwę i wartość
            if (nazwaInput.getText().isEmpty() || wartoscInput.getText().isEmpty()) {
                showAlert("Błąd walidacji", "Nazwa i cena są wymagane!");
                return;
            }

            String name = nazwaInput.getText();
            // Jeśli kategoria pusta, daj "Inne"
            String category = (kategoriaBox.getValue() != null) ? kategoriaBox.getValue() : "Inne";

            String location = (locationBox.getValue() != null) ? locationBox.getValue() : "Nieznana";

            LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();

            // Konwersja liczb z zabezpieczeniem (chociaż try-catch wyżej też to łapie)
            double value = Double.parseDouble(wartoscInput.getText().replace(",", "."));

            if (value <= 0) {
                showAlert("Błąd danych", "Wartość przedmiotu musi być większa od 0!");
                return;
            }

            int warranty = 0;
            if (!gwarancjaInput.getText().isEmpty()) {
                warranty = Integer.parseInt(gwarancjaInput.getText());
            }

            int amount = 1;
            try {
                amount = Integer.parseInt(iloscInput.getText());
            } catch (Exception e) {
                amount = 1;
            }

            String type = trybFirmowy ? "FIRMOWY" : "PRYWATNY";

            String finalPath = null;

            if (trybEdycji && selectedItem != null) {
                // Domyślnie bierzemy starą ścieżkę
                finalPath = selectedItem.getImagePath();
            }

            // Jeśli użytkownik wybrał NOWY plik, nadpisujemy ścieżkę
            if (tempSciezkaDoZdjecia != null) {
                finalPath = tempSciezkaDoZdjecia;
            }

            try {
                if (trybEdycji && selectedItem != null) {
                    // EDYCJA
                    selectedItem.setName(name);
                    selectedItem.setCategory(category);
                    selectedItem.setLocation(location);
                    selectedItem.setPurchaseDate(date);
                    selectedItem.setValue(value);
                    selectedItem.setAmount(amount);
                    selectedItem.setWarranty(warranty);
                    selectedItem.setImagePath(finalPath); // Zapisujemy ustaloną ścieżkę
                    selectedItem.setType(type);

                    itemManager.updateItem(selectedItem);
                } else {
                    // NOWY PRZEDMIOT
                    Item newItem = new Item(name, category, location, date, value, amount, warranty, finalPath, type);
                    itemManager.addItem(newItem);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Błąd Bazy", "Nie udało się zapisać rekordu.");
                return;
            }

            // Reset i powrót
            trybEdycji = false;
            selectedItem = null;
            tempSciezkaDoZdjecia = null;
            goBack(event);

        } catch (NumberFormatException e) {
            showAlert("Błąd danych", "W polach liczbowych wpisz poprawne liczby (np. 12.50).");
        }
    }

    /**
     * Usuwa wybrany przedmiot z tabeli i bazy danych.
     * Wyświetla okno dialogowe z prośbą o potwierdzenie operacji.
     * Po usunięciu aktualizuje widoczną listę i statystyki.
     *
     * @param event zdarzenie kliknięcia przycisku "Usuń"
     */
    @FXML
    public void handleDelete(ActionEvent event) {
        if (table == null) return;
        Item selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Błąd", "Wybierz element.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Usunąć " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            try {
                itemManager.deleteItem(selected);
                if (trybFirmowy) listaFirmowa.remove(selected);
                else listaPrywatna.remove(selected);
                updateViewStatistics(trybFirmowy ? listaFirmowa : listaPrywatna);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Błąd", "Nie udało się usunąć.");
            }
        }
    }

    /**
     * Przełącza tryb filtrowania statystyk na "Wszystkie lata".
     *
     * @param event zdarzenie kliknięcia
     */
    @FXML
    public void filterAll(ActionEvent event) {
        setFilter("ALL");
    }

    /**
     * Przełącza tryb filtrowania statystyk na "Bieżący rok".
     *
     * @param event zdarzenie kliknięcia przycisku filtra
     */
    @FXML
    public void filterYear(ActionEvent event) {
        setFilter("YEAR");
    }

    /**
     * Przełącza tryb filtrowania statystyk na "Bieżący miesiąc".
     *
     * @param event zdarzenie kliknięcia przycisku filtra
     */
    @FXML
    public void filterMonth(ActionEvent event) {
        setFilter("MONTH");
    }

    /**
     * Ustawia aktywny tryb filtra i odświeża widok Dashboardu.
     *
     * @param mode tryb filtrowania ("ALL", "YEAR" lub "MONTH")
     */
    private void setFilter(String mode) {
        currentFilterMode = mode;
        updateFilterButtons();
        updateDashboard();
    }

    /**
     * Aktualizuje wygląd przycisków filtrów, wyróżniając aktywny tryb.
     */
    private void updateFilterButtons() {
        if (btnAll == null) return;
        btnAll.getStyleClass().remove("active");
        btnYear.getStyleClass().remove("active");
        btnMonth.getStyleClass().remove("active");

        if ("ALL".equals(currentFilterMode)) btnAll.getStyleClass().add("active");
        else if ("YEAR".equals(currentFilterMode)) btnYear.getStyleClass().add("active");
        else if ("MONTH".equals(currentFilterMode)) btnMonth.getStyleClass().add("active");
    }

    /**
     * Odświeża widok Dashboardu (Pulpitu) i przelicza statystyki.
     * <p>
     * Metoda iteruje przez listę przedmiotów, filtruje je według wybranego trybu
     * (Wszystkie/Rok/Miesiąc) i oblicza:
     * Całkowitą liczbę przedmiotów.
     * Łączną wartość majątku.
     * Dane do wykresu kołowego (wartość wg kategorii).
     * </p>
     */
    private void updateDashboard() {
        if (totalItemsLabel == null || categoryPieChart == null) return;
        ObservableList<Item> wszystkie = FXCollections.observableArrayList();
        wszystkie.addAll(listaPrywatna);
        wszystkie.addAll(listaFirmowa);
        LocalDate now = LocalDate.now();
        int biezacyRok = now.getYear();
        var biezacyMiesiac = now.getMonth();
        int count = 0;
        double val = 0;
        Map<String, Double> cats = new HashMap<>();
        for (Item item : wszystkie) {
            boolean uwzglednij = false;
            LocalDate d = item.getPurchaseDate();
            if ("ALL".equals(currentFilterMode)) uwzglednij = true;
            else if ("YEAR".equals(currentFilterMode)) {
                if (d.getYear() == biezacyRok) uwzglednij = true;
            } else if ("MONTH".equals(currentFilterMode)) {
                if (d.getYear() == biezacyRok && d.getMonth() == biezacyMiesiac) uwzglednij = true;
            }
            if (uwzglednij) {
                count += item.getAmount();
                double t = item.getValue() * item.getAmount();
                val += t;
                cats.put(item.getCategory(), cats.getOrDefault(item.getCategory(), 0.0) + t);
            }
        }
        totalItemsLabel.setText(String.valueOf(count));
        totalValueLabel.setText(String.format("%.2f PLN", val));
        ObservableList<PieChart.Data> newData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> e : cats.entrySet()) {
            if (e.getValue() > 0) {
                double percentage = (e.getValue() / val) * 100;
                String label = String.format("%s (%.1f%%)", e.getKey(), percentage);
                newData.add(new PieChart.Data(label, e.getValue()));
            }
        }
        categoryPieChart.setAnimated(false);
        String tytul = "Wszystkie";
        if ("YEAR".equals(currentFilterMode)) tytul = "Ten Rok (" + biezacyRok + ")";
        if ("MONTH".equals(currentFilterMode)) tytul = "Ten Miesiąc";
        categoryPieChart.setTitle(tytul);
        categoryPieChart.setData(FXCollections.observableArrayList());
        categoryPieChart.setLegendVisible(false);
        Platform.runLater(() -> {
            categoryPieChart.setData(newData);
            categoryPieChart.setLegendVisible(true);
        });
    }

    /**
     * Oblicza i wyświetla statystyki dla aktualnie widocznej listy przedmiotów.
     * Funkcja identyfikuje przedmioty, których gwarancja wkrótce wygaśnie (najbliższe 30 dni)
     * lub już wygasła. Sortuje je chronologicznie i wyświetla na dole.
     * Przedmioty "zagrożone" są oznaczane kolorem czerwonym.
     *
     * @param lista lista przedmiotów do analizy (prywatna lub firmowa)
     */
    private void updateViewStatistics(ObservableList<Item> lista) {
        if (viewTotalLabel == null) return;

        // 1. Obliczanie sumy wartości
        double sum = 0;
        for (Item item : lista) {
            sum += item.getValue() * item.getAmount();
        }
        viewTotalLabel.setText(String.format("%.2f PLN", sum));

        // 2. Przygotowanie dat i filtrowanie
        LocalDate today = LocalDate.now();

        // Lista wszystkich przedmiotów z obliczoną datą końca gwarancji
        List<Item> allItems = new ArrayList<>(lista);

        // A. Lista NADCHODZĄCYCH (Data końca > dzisiaj) -> Sortujemy rosnąco (najbliższe najpierw)
        List<Item> upcoming = allItems.stream()
                .filter(i -> i.getPurchaseDate().plusMonths(i.getWarranty()).isAfter(today))
                .sorted(Comparator.comparing(i -> i.getPurchaseDate().plusMonths(i.getWarranty())))
                .collect(Collectors.toList());

        // B. Lista WYGASŁYCH (Data końca <= dzisiaj) -> Sortujemy malejąco (najświeższe wygasłe najpierw)
        List<Item> expired = allItems.stream()
                .filter(i -> !i.getPurchaseDate().plusMonths(i.getWarranty()).isAfter(today))
                .sorted((i1, i2) -> {
                    LocalDate d1 = i1.getPurchaseDate().plusMonths(i1.getWarranty());
                    LocalDate d2 = i2.getPurchaseDate().plusMonths(i2.getWarranty());
                    return d2.compareTo(d1); // Odwrócona kolejność
                })
                .collect(Collectors.toList());

        // 3. Wypełnianie Labeli (czyścimy najpierw)
        upcoming1Label.setText("");
        upcoming2Label.setText("");
        upcoming3Label.setText("");
        expired1Label.setText("");
        expired2Label.setText("");
        expired3Label.setText("");

        // Wyświetlamy do 3 nadchodzących
        if (upcoming.size() > 0) setUpcomingLabel(upcoming1Label, upcoming.get(0));
        if (upcoming.size() > 1) setUpcomingLabel(upcoming2Label, upcoming.get(1));
        if (upcoming.size() > 2) setUpcomingLabel(upcoming3Label, upcoming.get(2));

        // Wyświetlamy do 3 wygasłych
        if (expired.size() > 0) setExpiredLabel(expired1Label, expired.get(0));
        if (expired.size() > 1) setExpiredLabel(expired2Label, expired.get(1));
        if (expired.size() > 2) setExpiredLabel(expired3Label, expired.get(2));
    }

    /**
     * Pomocnicza metoda formatująca etykietę dla nadchodzących końców gwarancji.
     * Jeśli do końca gwarancji pozostało mniej niż 30 dni, tekst jest wyróżniany kolorem czerwonym.
     *
     * @param l    etykieta (Label) do modyfikacji
     * @param item przedmiot, którego data jest sprawdzana
     */
    private void setUpcomingLabel(Label l, Item item) {
        LocalDate end = item.getPurchaseDate().plusMonths(item.getWarranty());
        l.setText("• " + item.getName() + " (do " + end + ")");
        if (end.minusDays(30).isBefore(LocalDate.now())) {
            l.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            l.setStyle("-fx-text-fill: #2c3e50;");
        }
    }

    /**
     * Pomocnicza metoda formatująca etykietę dla wygasłych gwarancji.
     * Ustawia styl tekstu na szary, aby wizualnie odróżnić przedmioty archiwalne.
     *
     * @param l    etykieta (Label) do modyfikacji
     * @param item przedmiot z wygasłą gwarancją
     */
    private void setExpiredLabel(Label l, Item item) {
        LocalDate end = item.getPurchaseDate().plusMonths(item.getWarranty());
        l.setText("• " + item.getName() + " (koniec: " + end + ")");
        l.setStyle("-fx-text-fill: gray;");
    }

    /**
     * Otwiera systemowe okno wyboru pliku w celu wskazania zdjęcia przedmiotu.
     *
     * @param event zdarzenie kliknięcia przycisku "Wybierz plik"
     */
    @FXML
    public void handleSelectFile(ActionEvent event) {
        FileChooser fc = new FileChooser();
        File f = fc.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (f != null && filePathLabel != null) {
            filePathLabel.setText(f.getName());
            tempSciezkaDoZdjecia = f.getAbsolutePath();
        }
    }

    /**
     * Wypełnia panel szczegółów (DetailsView) danymi zaznaczonego elementu.
     * Obsługuje ładowanie obrazu z dysku. Jeśli plik nie istnieje lub ścieżka jest pusta,
     * wyświetla stosowny komunikat ("Brak zdjęcia" lub "Plik nie istnieje").
     */
    private void fillDetailsView() {
        if (selectedItem == null) return;
        detailName.setText(selectedItem.getName());
        detailCategory.setText(selectedItem.getCategory() + "");

        if (detailLocation != null) {
            detailLocation.setText(selectedItem.getLocation() != null ? selectedItem.getLocation() : "-");
        }
        detailAmount.setText(selectedItem.getAmount() + " szt.");
        detailPrice.setText(selectedItem.getValue() + " PLN");

        if (selectedItem.getPurchaseDate() != null) {
            detailDate.setText(selectedItem.getPurchaseDate().toString());
        } else {
            detailDate.setText("-");
        }

        detailWarranty.setText(selectedItem.getWarranty() + " mies.");
        double total = selectedItem.getValue() * selectedItem.getAmount();
        detailTotalValue.setText(String.format("%.2f PLN", total));

        if (selectedItem.getImagePath() != null && !selectedItem.getImagePath().isEmpty()) {
            File file = new File(selectedItem.getImagePath());
            if (file.exists()) {
                detailImageView.setImage(new Image(file.toURI().toString()));
                detailImageView.setVisible(true);
                noImageLabel.setVisible(false);
            } else {
                detailImageView.setVisible(false);
                noImageLabel.setVisible(true);
                noImageLabel.setText("Plik nie istnieje");
            }
        } else {
            detailImageView.setVisible(false);
            noImageLabel.setVisible(true);
            noImageLabel.setText("Brak zdjęcia");
        }
    }

    /**
     * Przełącza widok na panel szczegółów wybranego przedmiotu.
     *
     * @param event zdarzenie kliknięcia przycisku "Szczegóły"
     */
    @FXML
    public void showDetails(ActionEvent event) {
        if (table != null) {
            Item s = table.getSelectionModel().getSelectedItem();
            if (s == null) {
                showAlert("Błąd", "Wybierz element.");
                return;
            }
            selectedItem = s;
            loadInCenter(event, "DetailsView.fxml");
        }
    }

    /**
     * Resetuje style CSS przycisków w menu bocznym (usuwa klasę "active").
     */
    private void resetSidebarStyles() {
        if (btnDashboard != null) btnDashboard.getStyleClass().remove("active");
        if (btnPrivate != null) btnPrivate.getStyleClass().remove("active");
        if (btnCompany != null) btnCompany.getStyleClass().remove("active");
        if (btnCategories != null) btnCategories.getStyleClass().remove("active");
        if (btnLocations != null) btnLocations.getStyleClass().remove("active");
    }

    /**
     * Nawiguje do widoku Dashboard (Pulpit) i aktualizuje menu boczne.
     *
     * @param event zdarzenie kliknięcia w menu
     */
    @FXML
    public void showDashboard(ActionEvent event) {
        loadView("DashboardView.fxml");
        resetSidebarStyles();
        if (btnDashboard != null) btnDashboard.getStyleClass().add("active");
    }

    /**
     * Nawiguje do widoku przedmiotów prywatnych.
     *
     * @param event zdarzenie kliknięcia w menu
     */
    @FXML
    public void showPrivateAssets(ActionEvent event) {
        trybFirmowy = false;
        loadView("TableView.fxml");
        resetSidebarStyles();
        if (btnPrivate != null) btnPrivate.getStyleClass().add("active");
    }

    /**
     * Nawiguje do widoku przedmiotów firmowych.
     *
     * @param event zdarzenie kliknięcia w menu
     */
    @FXML
    public void showCompanyAssets(ActionEvent event) {
        trybFirmowy = true;
        loadView("TableView.fxml");
        resetSidebarStyles();
        if (btnCompany != null) btnCompany.getStyleClass().add("active");
    }

    /**
     * Nawiguje do widoku zarządzania kategoriami.
     *
     * @param event zdarzenie kliknięcia w menu
     */
    @FXML
    public void showCategoryCRUD(ActionEvent event) {
        loadView("CategoryView.fxml");
        resetSidebarStyles();
        if (btnCategories != null) btnCategories.getStyleClass().add("active");
    }

    /**
     * Nawiguje do widoku zarządzania lokalizacjami.
     *
     * @param event zdarzenie kliknięcia w menu
     */
    @FXML
    public void showLocationCRUD(ActionEvent event) {
        loadView("LocationView.fxml");
        resetSidebarStyles();
        if (btnLocations != null) btnLocations.getStyleClass().add("active");
    }

    /**
     * Przełącza aplikację w tryb edycji i otwiera formularz dla zaznaczonego przedmiotu.
     *
     * @param event zdarzenie kliknięcia przycisku "Edytuj"
     */
    @FXML
    public void handleEdit(ActionEvent event) {
        if (table == null) return;
        Item selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Błąd", "Wybierz element.");
            return;
        }
        selectedItem = selected;
        trybEdycji = true;
        loadInCenter(event, "AddView.fxml");
    }

    /**
     * Przełącza aplikację w tryb dodawania (czyści selekcję) i otwiera pusty formularz.
     *
     * @param event zdarzenie kliknięcia przycisku "Dodaj nowy"
     */
    @FXML
    public void showAddForm(ActionEvent event) {
        trybEdycji = false;
        selectedItem = null;
        loadInCenter(event, "AddView.fxml");
    }

    /**
     * Obsługuje powrót z formularza lub szczegółów do głównej tabeli.
     * Przywraca widok listy (Prywatnej lub Firmowej) w zależności od ostatniego stanu.
     *
     * @param event zdarzenie kliknięcia przycisku "Anuluj/Powrót do listy"
     */
    @FXML
    public void goBack(ActionEvent event) {
        loadInCenter(event, "TableView.fxml");
        resetSidebarStyles();
        if (trybFirmowy && btnCompany != null) btnCompany.getStyleClass().add("active");
        else if (!trybFirmowy && btnPrivate != null) btnPrivate.getStyleClass().add("active");
    }

    /**
     * Zamyka połączenie z bazą danych i wyłącza aplikację.
     *
     * @param event zdarzenie kliknięcia przycisku "Zamknij"
     */
    @FXML
    public void closeApp(ActionEvent event) {
        HibernateUtil.shutdown();
        Platform.exit();
        System.exit(0);
    }

    /**
     * Ładuje plik FXML do centralnej części głównego BorderPane.
     * Używane do przełączania głównych widoków (Dashboard/Tabela).
     *
     * @param fxmlFile nazwa pliku FXML (np. "DashboardView.fxml")
     */
    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/assetmanager/" + fxmlFile));
            Parent view = loader.load();
            if (mainBorderPane != null) mainBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ładuje plik FXML do centrum, ale pobiera referencję do BorderPane dynamicznie ze sceny.
     * Używane, gdy wywołanie pochodzi z przycisku wewnątrz zagnieżdżonego widoku.
     *
     * @param event zdarzenie wywołujące
     * @param fxml  nazwa pliku FXML
     */
    private void loadInCenter(ActionEvent event, String fxml) {
        try {
            Node source = (Node) event.getSource();
            BorderPane borderPane = (BorderPane) source.getScene().lookup("#mainBorderPane");
            if (borderPane != null)
                borderPane.setCenter(FXMLLoader.load(getClass().getResource("/org/example/assetmanager/" + fxml)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wyświetla proste okno dialogowe z ostrzeżeniem.
     *
     * @param title   tytuł okna
     * @param content treść komunikatu
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}