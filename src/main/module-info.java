module com.example.pss_mss {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.controlsfx.controls;
    requires java.naming;

    opens pss to javafx.fxml;
    exports code;
}