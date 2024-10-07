package code.controllers;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import static code.App.db_c;

public class MbrController {
    private MainController mainController;
    public Button btn_viewMembership;
    private ObservableList<String[]> members_db = FXCollections.observableArrayList();
//    @FXML
//    private Button btn_viewMembership, btn_searchMbr;
    @FXML
    private TextField txt_mbrAddUcid, txt_mbrAddFname, txt_mbrAddLname, txt_mbrAddEmail, txt_memberSearch;
    @FXML
    private DatePicker pick_mbrAddSince, pick_mbrAddTill;
    @FXML
    public AnchorPane pane_mbrMain;
    @FXML
    private ChoiceBox pick_mbrAddCitizen, pick_mbrAddYear, pick_mbrAddPayment;
    @FXML
    private TableView<String[]> members_list;
    @FXML
    private TableColumn<String[], String> ucidcol, fnamecol, lnamecol, yearstudycol, statuscol, citizencol;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            if (pane_mbrMain != null) {
                pane_mbrMain.setUserData(this);
            }
                    try {
                        if (ucidcol != null) {
                            ucidcol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[0]));
                            fnamecol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[1]));
                            lnamecol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[2]));
                            yearstudycol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[3]));
                            citizencol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[4]));
                            statuscol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[5]));
                            loadMembers();
                        }
                    } catch (SQLException e) {
                        System.out.println("Error: Couldn't load members from code.database");
                    }
                }
        );
    }
    public void viewMembership(ActionEvent actionEvent) throws IOException, SQLException {
        pane_mbrMain.setVisible(false);
        String[] selectedMember = members_list.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            mainController.viewMembership(selectedMember[0]);
        }
    }

    /**
     * Function to add a member with the details entered
     * @param actionEvent
     * @return true if member was added, false otherwise
     */
    public boolean addMember(ActionEvent actionEvent) {
        String ucid = txt_mbrAddUcid.getText();
        String fname = txt_mbrAddFname.getText();
        String lname = txt_mbrAddLname.getText();
        String email = txt_mbrAddEmail.getText();
        String year = null, citizen = null, payment = null;
        // Make sure year, citizenship, and payment method are selected
        try {
            year = pick_mbrAddYear.getSelectionModel().getSelectedItem().toString();
            citizen = pick_mbrAddCitizen.getSelectionModel().getSelectedItem().toString();
            payment = pick_mbrAddPayment.getSelectionModel().getSelectedItem().toString();
        }
        catch(NullPointerException e){
            return false;
        }
        LocalDate since = pick_mbrAddSince.getValue();
        LocalDate till = pick_mbrAddTill.getValue();

        // Make sure non empty values are entered
        String[] strings = {ucid, fname, lname, email, year, citizen, payment};
        for (String s : strings) {
            if (s.isEmpty()) {
                return false;
            }
        }
        // Convert UCID into an int
        try {
            // Make sure UCID is an 8 digit number
            if (ucid.length() == 8) {
                // Make sure membership from and to dates are selected
                if (since != null && till != null){
                    db_c.getMembersLink().add(Integer.parseInt(ucid), fname, lname, year, citizen, since, till, payment, email);
                    mainController.setSuccess("Member successfully added");
                    showMembersView();
                    mainController.updateHomeStats();
                } else{
                    mainController.setError("Error: Select membership validity from and to dates");
                    return false;
                }
            }
            else{
                mainController.setError("Error: Enter a valid 8 digit UCID number");
                return false;
            }
        }
        catch (Exception e){
            mainController.setError("Error: Enter a valid 8 digit UCID number");
            return false;
        }
        return true;
    }
    // Function to load all members from the code.database
    private void loadMembers() throws SQLException {
        members_list.setItems(members_db);
        if (ucidcol != null) {
            members_db.clear();
            db_c.getMembersLink().getList(members_db);
            members_list.refresh();
        }
    }

    /**
     * Function to search for members in database whose first/last name or ucid
     * partially or fully match the text entered
     * @param event
     */
    public void searchMember(ActionEvent event){
        String dataToSearch = txt_memberSearch.getText();
        int ucid = 0;
        ArrayList<String[]> members = null;
        if (!dataToSearch.contentEquals("")) {
            members_db.clear();
            db_c.getMembersLink().searchMembers(dataToSearch, members_db);
            int size = members_db.size();
            if (size == 0){
                mainController.setError("Error: No member found");
            }
            else{
                mainController.setSuccess("Successfully loaded " + size + " members");
            }
            members_list.requestFocus();
            btn_viewMembership.setDisable(true);
        }
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
            if (members_list.getSelectionModel().getSelectedItem() != null) {
                btn_viewMembership.setDisable(false);
            }
        }
    }

    public void setMainController(MainController m){
        this.mainController = m;
    }
    public void showMembersView() throws SQLException {
        if (pane_mbrMain != null) {
            btn_viewMembership.setDisable(true);
            loadMembers();
            txt_mbrAddEmail.setText("");
            txt_mbrAddFname.setText("");
            txt_mbrAddLname.setText("");
            txt_mbrAddUcid.setText("");
            pick_mbrAddCitizen.setValue(null);
            pick_mbrAddPayment.setValue(null);
            pick_mbrAddSince.setValue(null);
            pick_mbrAddTill.setValue(null);
            pick_mbrAddYear.setValue(null);
            txt_memberSearch.setText("");
        }
    }


    public void importMembers(ActionEvent actionEvent) throws SQLException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".csv files", "*.csv"));

        Window window = pane_mbrMain.getScene().getWindow();
        if (window instanceof Stage) {
            Stage stage = (Stage) window;
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                int[] nums = db_c.getMembersLink().importMembers(selectedFile.getAbsolutePath());
                mainController.setSuccess(nums[0] + " members successfully added\n" + nums[1] + " members already exist in record");
            }
        }
        loadMembers();
        mainController.updateHomeStats();
    }


}
