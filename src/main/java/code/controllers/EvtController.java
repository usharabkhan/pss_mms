package code.controllers;

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
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.opencsv.CSVReader;
import java.io.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static code.App.db_c;
import static code.App.main;
import static code.util.proper;

public class EvtController {
    public Text txt_fileName;
    public TextField txt_memberSearch;
    private MainController mainController;
    public TableColumn<String[], String> evt_colUcid, evt_colFname, evt_colLname, evt_colStatus, evt_colTicket;
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
//            evt_colLname.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[2]));
            evt_colStatus.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[2]));
            evt_colTicket.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue()[3]));

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
            try {
                importEventList();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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
                            String[] attendeeData = new String[4];
//                            System.out.println(nextLine[2]);
                            attendeeData[0] = ucid;
                            attendeeData[1] = proper(nextLine[2]);  // Full name
                            attendeeData[2] = "inactive";
                            if (activeMembers.contains(ucid)){
                                attendeeData[2] = "active";
                            }
                            attendeeData[3] = nextLine[4].contains("Non") ? "Member" : "Non-Member";          // Type of ticket purchased
                            AllAttendeeData.add(attendeeData);
                            DisplayAttendeeData.add(attendeeData);
                            count++;
                        }
                    }

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
}
