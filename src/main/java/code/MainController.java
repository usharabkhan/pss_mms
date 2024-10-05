package code;

import database.db_control;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MainController {
    @FXML
    private TextField  txt_mbrDetEmail;
    @FXML
    private Text txt_mbrDetLbl, txt_mbrDetName, txt_mbrDetUcid, txt_mbrDetSince;
    @FXML
    private DatePicker pick_mbrDetSince, pick_mbrDetTill;
    private db_control db_c;
    private ObservableList<String[]> members_db = FXCollections.observableArrayList(), events_db = FXCollections.observableArrayList();

    @FXML
    private Pane window_main;
    @FXML
    private ChoiceBox<String> pick_memberStudyYear, pick_mbrDetCitizen, pick_mbrDetYear, pick_mbrDetPayment;

    @FXML
    private TableView<String[]> members_list;
    @FXML
    private TableColumn<String[], String> ucidcol, fnamecol, lnamecol, yearstudycol, statuscol, citizencol;
    @FXML
    private Button btn_viewMembership, btn_membersView, btn_eventsView;
    @FXML
    private AnchorPane events_main, pane_mbrMain, pane_mbrDet, pane_evntTickets;
    
    @FXML
    private TextField txt_memberSearch;
    @FXML
    protected void initialize() throws SQLException {
        Platform.runLater(() -> {
                try {
//                    FXMLLoader loader1 = new FXMLLoader(getClass().getResource("/events_view.fxml"));
//                    events_main = loader1.load();
//
//
//                    // Initialize members pane stuff
//                    if (window_main != null) {
//                        window_main.getChildren().add(events_main); // Add events_main to window_main
////
////                        pane_mbrDet.setVisible(false);
//                        events_main.setVisible(false); // Initially set visibility
//                    }
                    if (ucidcol != null) {
                        ucidcol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[0]));
                        fnamecol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[1]));
                        lnamecol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[2]));
                        yearstudycol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[3]));
                        citizencol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[4]));
                        statuscol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[5]));
                        loadMembers();
                    }



                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        }

    public void showMembersView(ActionEvent event) throws SQLException {
        pane_evntTickets.setVisible(false);
        if (pane_mbrDet != null) {
            pane_mbrDet.setVisible(false);
        }
        if (pane_mbrMain != null) {
            if (!pane_mbrMain.isVisible()) {
                pane_mbrMain.setVisible(true);
                btn_viewMembership.setDisable(true);
                btn_membersView.setDisable(true);
                btn_eventsView.setDisable(false);
                loadMembers();
            }
        }
    }
    public void searchMember(ActionEvent event){
        String dataToSearch = txt_memberSearch.getText();
        int ucid = 0;
        ArrayList<String[]> members = null;
        if (!dataToSearch.contentEquals("")) {
            // Check if a UCID was entered
            try {
                ucid = Integer.parseInt(dataToSearch);
            }
            catch (Exception e){

            }
            // Search using UCID if it was entered
            members_db.clear();
            if (ucid != 0){
                db_c.getMembersLink().getFromUCID(ucid, members_db);
            }
            // else search using name
            else {
//                members = db_c.getMembersLink().getFromName(dataToSearch, members_db);
            }
        }
    }
    public void showEventsView(ActionEvent event){
        if (!pane_evntTickets.isVisible()) {
            pane_evntTickets.setVisible(true);
            pane_mbrMain.setVisible(false);
            pane_mbrDet.setVisible(false);
            btn_membersView.setDisable(false);
            btn_eventsView.setDisable(true);
        }
    }

    // Function to load all members from the database
    private void loadMembers() throws SQLException {
        members_list.setItems(members_db);
        if (ucidcol != null) {
            members_db.clear();
            db_c.getMembersLink().getList(members_db);
            members_list.refresh();
        }
    }


    public void viewMembership(ActionEvent actionEvent) throws IOException, SQLException {
        pane_mbrMain.setVisible(false);
        pane_mbrDet.setVisible(true);

        String[] selectedMember = members_list.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            // Retrieve the text from column 0 (Name)
            String ucid = selectedMember[0];
//            db_c.getMembersLink().getFromUCID(Integer.parseInt(ucid), members_db);
            ResultSet[] data = db_c.getMembersLink().getMembership(ucid);
            String fName,lName, year, payment;
            fName = data[1].getString("fName");
            lName = data[1].getString("lName");
            year = data[1].getString("year");
            payment = data[0].getString("payment");

            txt_mbrDetUcid.setText(ucid);
            pick_mbrDetTill.setValue(data[0].getDate("until").toLocalDate());
            txt_mbrDetSince.setText(data[0].getDate("since").toLocalDate().toString());
            txt_mbrDetEmail.setText(data[0].getString("email"));
            txt_mbrDetName.setText(fName + " " + lName);
            pick_mbrDetCitizen.setValue(data[1].getString("citizen"));

            pick_mbrDetYear.getSelectionModel().select(util.extractNumber(year, 5)- 1);

            if (payment.contains("transfer")){
                pick_mbrDetPayment.getSelectionModel().select(0);
            }
            else if (payment.contains("cash")){
                pick_mbrDetPayment.getSelectionModel().select(1);
            }
            else {
                pick_mbrDetPayment.getSelectionModel().select(2);
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // Set a custom StringConverter to the DatePicker
            pick_mbrDetTill.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    if (date != null) {
                        return dateFormatter.format(date);
                    } else {
                        return "";
                    }
                }

                @Override
                public LocalDate fromString(String string) {
                    if (string != null && !string.isEmpty()) {
                        return LocalDate.parse(string, dateFormatter);
                    } else {
                        return null;
                    }
                }
            });
        }
    }

    public void setDBControl(db_control db_c) throws SQLException {
        this.db_c = db_c;
    }

    /*
    Function to refresh member list when search text is cleared
     */
    public void checkMbrSearchText(KeyEvent event) throws SQLException {
        if (event.getCode() == KeyCode.BACK_SPACE) {
            if (txt_memberSearch.getText().equals("")) {
                loadMembers();
            }
        }
    }

    /*
    Function to enable View Membership button when a row is selected
     */
    public void enableViewMembership(MouseEvent mouseEvent) {
        if (btn_viewMembership.isDisabled()){
            btn_viewMembership.setDisable(false);
        }
    }

}
