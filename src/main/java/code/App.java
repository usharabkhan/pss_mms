package code;

import database.db_control;
import database.db_members;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class App extends Application {
    public static db_control db_c;
    @Override
    public void start(Stage stage) throws Exception {
//        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();

        controller.setDBControl(db_c);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/pss_logo.jpg")));

        stage.setTitle("PSS Membership & Event Management");

        stage.setScene(scene);
        stage.requestFocus();
        stage.setResizable(false);
        stage.show();

        stage.setOnCloseRequest(event -> {
            // Perform actions before closing

            try {
                db_c.db_end();
            } catch (SQLException e) {}
            exit(0);
        });
    }

    public static void main(String[] args) throws SQLException {
        db_c = new db_control();
//        db_c.getMembersLink().importMembers();
//        db_c.getEventsLink().importEvents();
        launch(args);
    }
}
