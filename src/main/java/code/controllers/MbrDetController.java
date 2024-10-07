package code.controllers;

import code.util;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static code.App.*;

public class MbrDetController {
    private MainController mainController;
    private ObservableList<String[]> members_db = FXCollections.observableArrayList();
    //    @FXML
//    private Button btn_viewMembership, btn_searchMbr;
    @FXML
    private TextField txt_mbrDetEmail, txt_memberSearch;
    @FXML
    private Text txt_mbrDetLbl, txt_mbrDetName, txt_mbrDetUcid, txt_mbrDetSince;
    @FXML
    private DatePicker pick_mbrDetSince, pick_mbrDetTill;
    @FXML
    private AnchorPane pane_mbrDet;
    @FXML
    private ChoiceBox pick_mbrDetCitizen, pick_mbrDetYear, pick_mbrDetPayment;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            if (pane_mbrDet != null) {
                pane_mbrDet.setUserData(this);
            }
        });
    }

    public void viewMembership(String ucid) throws SQLException {
        pane_mbrDet.setVisible(true);

        if (ucid != null) {
            // Retrieve the text from column 0 (Name)
//            String ucid = selectedMember[0];
            ResultSet[] data = db_c.getMembersLink().getMembership(ucid);
            String fName,lName, year, payment;
            fName = data[1].getString("fName");
            lName = data[1].getString("lName");
            year = data[1].getString("year");
            payment = data[0].getString("payment");

            txt_mbrDetUcid.setText(ucid);
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

            // Deal with different date formats (non exhaustive)
            try {
                Date d1 = data[0].getDate("since");
                Date d2 = data[0].getDate("until");
                txt_mbrDetSince.setText(d1.toString());
                pick_mbrDetTill.setValue(d2.toLocalDate());
            }
            catch (Exception e){
                txt_mbrDetSince.setText(data[0].getString("since"));
                pick_mbrDetTill.setValue(LocalDate.parse(data[0].getString("until"), dateFormatter));
            }
        }
    }

    public void showMembersView() throws SQLException {
        mainController.showMembersView();
    }
    public void setMainController(MainController m){
        this.mainController = m;
    }


    /*
    Method to remove a member and their membership in database
     */
    public void removeMember(ActionEvent actionEvent) throws SQLException {
        String ucid = txt_mbrDetUcid.getText();

        boolean confirmed = mainController.getConfirmation("Are you sure you want to delete " + txt_mbrDetName.getText() + "'s data?");
        if (confirmed) {
            int result = db_c.getMembersLink().remove(Integer.parseInt(ucid));
            if (result == 1){
                mainController.setSuccess("Member data successfully deleted");
                showMembersView();
                mainController.updateHomeStats();
            }
            else{
                mainController.setError("Error: Member could not be deleted");
            }
        }
    }

    /*
    Method to update a member details in database
     */
    public void updateMember(ActionEvent actionEvent) {
        String ucid = txt_mbrDetUcid.getText();
        String email = txt_mbrDetEmail.getText();
        String year = null, citizen = null, payment = null;
        LocalDate until = pick_mbrDetTill.getValue();
        try {
            year = pick_mbrDetYear.getSelectionModel().getSelectedItem().toString();
            citizen = pick_mbrDetCitizen.getSelectionModel().getSelectedItem().toString();
            payment = pick_mbrDetPayment.getSelectionModel().getSelectedItem().toString();
            db_c.getMembersLink().update(Integer.parseInt(ucid), year, citizen, until, payment, email);
            mainController.setSuccess("Member " + txt_mbrDetName.getText() + " successfully updated");
            mainController.updateHomeStats();
        }
        catch (NullPointerException e){
            System.out.println(e.getMessage());
        }
        catch (SQLException e) {
            mainController.setError("Error: Unable to update member details");
        }
    }
}

