package code.controllers;

import code.util;
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
    private TextField txt_memberSearch;
    @FXML
    public AnchorPane pane_mbrMain;
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
                            yearstudycol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(util.extractYear(cellData.getValue()[3])));
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


    public void showAddMbrWindow(ActionEvent actionEvent) throws SQLException {
        mainController.showAddMbrWindow(new String[]{""});
    }
}
