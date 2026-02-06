module org.example.assetmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;             // Obsługa SQL
    requires org.hibernate.orm.core; // Główny Hibernate
    requires jakarta.persistence;    // Adnotacje (@Entity, @Id itp.)
    requires java.naming;

    opens org.example.assetmanager to javafx.fxml, org.hibernate.orm.core;
    exports org.example.assetmanager;
    exports org.example.assetmanager.entity;
    opens org.example.assetmanager.entity to javafx.fxml, org.hibernate.orm.core;
    exports org.example.assetmanager.managers;
    opens org.example.assetmanager.managers to javafx.fxml, org.hibernate.orm.core;
    exports org.example.assetmanager.view;
    opens org.example.assetmanager.view to javafx.fxml, org.hibernate.orm.core;
    exports org.example.assetmanager.util;
    opens org.example.assetmanager.util to javafx.fxml, org.hibernate.orm.core;
}