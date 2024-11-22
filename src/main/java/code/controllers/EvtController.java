package code.controllers;

import code.util;
import com.opencsv.exceptions.CsvValidationException;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.opencsv.CSVReader;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static code.App.db_c;
import static code.App.main;
import static code.util.extractEmail;
import static code.util.proper;

public class EvtController {
    public Text txt_fileName;
    public TextField txt_memberSearch;
    private MainController mainController;
    public TableColumn<String[], String> evt_colUcid, evt_colFname, evt_colEmail, evt_colStatus, evt_colTicket;
    public TableView<String[]> evt_attendeeList;
    public Button btn_addAsMbr, btn_importEventList, btn_searchMbr;
    public CheckBox chk_mbrShow, chk_nonMbrShow;
    public AnchorPane pane_evntTickets;

    ObservableList<String[]> AllAttendeeData = FXCollections.observableArrayList();
    ObservableList<String[]> DisplayAttendeeData = FXCollections.observableArrayList();

    @FXML
    public void initialize(){
        Platform.runLater(() ->{
            if (pane_evntTickets != null) {
                pane_evntTickets.setUserData(this);
            }
            // Initialize column types
            evt_colUcid.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[0]));
            evt_colFname.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[1]));
            evt_colStatus.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[2]));
            evt_colTicket.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[3]));
            evt_colEmail.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[4]));

            evt_attendeeList.setRowFactory(tv -> new TableRow<>() {
                @Override
                protected void updateItem(String[] item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setStyle(""); // Reset style for empty rows
                        setGraphic(null);
                    } else {
                        // Check the status and apply formatting
                        String status = item[2];
                        String ticketType = item[3];
                        if ("inactive".equalsIgnoreCase(status) && "Member".equalsIgnoreCase(ticketType)) {
                            setId("ticket-mismatch-row");
                        } else {
                            setId("");
                        }
                    }
                }
            });
//            try {
//                importEventList();
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
        });
    }

    HashMap<String, String[]> AllAttendees = new HashMap();
    public void importEventList() throws SQLException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".csv files", "*.csv"));
        fileChooser.setInitialFileName("C:\\Users\\ushar\\Desktop\\Study\\Project\\pss_mss\\src\\main\\resources");

        Window window = pane_evntTickets.getScene().getWindow();
        if (window instanceof Stage) {
            Stage stage = (Stage) window;
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                String path = selectedFile.getAbsolutePath();
                // Get a list of active UCIDs
                Set<String> activeMembers = db_c.getMembersLink().getActiveUcids();


                try (CSVReader reader = new CSVReader(new FileReader(path))) {
                    String[] nextLine;
                    int count = 0;
                    reader.readNext();

                    while ((nextLine = reader.readNext()) != null) {
                        String ucid = nextLine[17];
                        if (!ucid.isBlank()) {
                            String[] attendeeData = new String[5];
//                            System.out.println(nextLine[2]);
                            attendeeData[0] = ucid;
                            attendeeData[1] = proper(nextLine[2]);  // Full name
                            attendeeData[2] = "inactive";
                            if (activeMembers.contains(ucid)){
                                attendeeData[2] = "active";
                            }
                            attendeeData[3] = util.proper(nextLine[4]).contains("Non") ? "Member" : "Non-Member";          // Type of ticket purchased
                            attendeeData[4] = extractEmail(nextLine[3]);
                            System.out.println(attendeeData[0]);
                            AllAttendeeData.add(attendeeData);
                            DisplayAttendeeData.add(attendeeData);
                            count++;
                        }
                    }
                    System.out.println(count);
                    evt_attendeeList.getItems().clear();
                    evt_attendeeList.setItems(DisplayAttendeeData);
                    mainController.setSuccess("Successfully loaded event file " + selectedFile.getName());
                    txt_fileName.setText("Analyzing event file: " + selectedFile.getName());

                } catch (CsvValidationException | IOException e) {
                    mainController.setError("Error: Could not load event file");
                }
            }
        }
    }


    public void setMainController(MainController m){
        this.mainController = m;
    }

    public void searchMember(ActionEvent actionEvent) {
        String txt = txt_memberSearch.getText();
        if (!txt.isBlank()) {
            DisplayAttendeeData.clear();
            for (String[] d : AllAttendeeData) {
                if (d[0].toLowerCase().contains(txt.toLowerCase()) || d[1].toLowerCase().contains(txt.toLowerCase())){
                    DisplayAttendeeData.add(d);
                }
            }
        }
    }

    /*
        Function to refresh member list when search text is cleared
         */
    public void checkMbrSearchText(KeyEvent event) throws SQLException {
        if (event.getCode() == KeyCode.BACK_SPACE) {
            if (txt_memberSearch.getText().equals("")) {
                DisplayAttendeeData.clear();
                DisplayAttendeeData.addAll(AllAttendeeData);
            }
        }
    }
    public void showMembers(ActionEvent actionEvent) {
        // display members only
        chk_nonMbrShow.setSelected(false);
        if (DisplayAttendeeData.size() != AllAttendeeData.size()) {
            DisplayAttendeeData.clear();
            DisplayAttendeeData.addAll(AllAttendeeData);
        }
        if (chk_mbrShow.isSelected()) {
            DisplayAttendeeData.removeIf(item -> item[2].equalsIgnoreCase("inactive"));
        }
    }

    public void showNonMembers(ActionEvent actionEvent) {
        // display non-members only
        chk_mbrShow.setSelected(false);
        // all members were being displayed, display non members only
        if (DisplayAttendeeData.size() != AllAttendeeData.size()) {
            DisplayAttendeeData.clear();
            DisplayAttendeeData.addAll(AllAttendeeData);
        }
        if (chk_nonMbrShow.isSelected()) {
            // case 2: members were being displayed, display non members
            DisplayAttendeeData.removeIf(item -> item[2].equalsIgnoreCase("active"));
        }

    }

    /**
     * Method to add attendee as member if record doesn't exist
     * Provides data to prefill ucid, name, and email
     * @param actionEvent
     * @throws SQLException
     */
    public void addAsMember(ActionEvent actionEvent) throws SQLException {
        String[] row = evt_attendeeList.getSelectionModel().getSelectedItem();
        if (row != null && !row[0].isBlank()){
            String[] data = new String[4];
            data[0] = row[0]; // ucid
            data[3] = row[4]; // email
            String[] nameSplit = row[1].split("\\s");
            if (nameSplit.length == 2){
                data[1] = nameSplit[0]; // First name
                data[2] = nameSplit[1]; // Last name
            }
            else if(nameSplit.length > 2){
                data[1] = nameSplit[0] + " " + nameSplit[1]; // First name is two parts
                data[2] = nameSplit[2];
                for (int i = 3; i < nameSplit.length; i ++){
                    data[2] = data[2] + " " + nameSplit[i];
                }
                data[2].strip();
            }
            mainController.showAddMbrWindow(data);

            ResultSet[] temp = db_c.getMembersLink().getMembership(data[0]);
            if (temp[1].next()){
                // update status to active
                if (temp[1].getString("status").equalsIgnoreCase("active")) {
                    row[2] = "active";
                    evt_attendeeList.refresh();
                }
            }
        }
    }


    public void toggleAddAsMbrBtn(MouseEvent mouseEvent) {
        String[] row = evt_attendeeList.getSelectionModel().getSelectedItem();
        btn_addAsMbr.setDisable(row == null || row[0].isBlank()
                || !row[2].equalsIgnoreCase("inactive"));
    }

    public void showEventsView() {
        btn_addAsMbr.setDisable(true);
    }
}
