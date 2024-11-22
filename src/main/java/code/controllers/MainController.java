package code.controllers;

import code.database.db_control;
import code.util;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import javafx.event.ActionEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static code.App.db_c;
import static code.util.getInitials;
import static java.lang.System.exit;
import static java.lang.System.setErr;

public class MainController {
    public AnchorPane pane_addMbr;
    private HashMap<String, String[]> main_mbrList;
    @FXML
    public HBox hbox_mbrs;
    public BarChart home_mbrNationality, home_mbrYears;
    @FXML
    public Text txt_totalMbr, txt_inactMbr,txt_actMbr;
    private db_control db_c;

    @FXML
    private Pane window_main;

    @FXML
    private Button btn_membersView, btn_eventsView, btn_homeView, btn_closeAddMbr;
    @FXML
    private AnchorPane events_main, pane_mbrMain, pane_mbrDet, pane_evntTickets, pane_home;
    
    @FXML
    private TextField txt_mbrAddUcid, txt_mbrAddFname, txt_mbrAddLname, txt_mbrAddEmail;
    @FXML
    private DatePicker pick_mbrAddSince, pick_mbrAddTill;
    @FXML
    private ChoiceBox pick_mbrAddCitizen, pick_mbrAddYear, pick_mbrAddPayment;
    private MbrController mbrController;
    private MbrDetController mbrDetController;
    private EvtController evtController;

    @FXML
    protected void initialize() throws SQLException {
        Platform.runLater(() -> {
                try {
                    if (pane_mbrMain != null) {
                        // Retrieve the controller from the included pane
                        mbrController = (MbrController) pane_mbrMain.getUserData();
                        mbrDetController = (MbrDetController) pane_mbrDet.getUserData();
                        evtController = (EvtController) pane_evntTickets.getUserData();
                        mbrController.setMainController(this);
                        mbrDetController.setMainController(this);
                        evtController.setMainController(this);
                        if (mbrDetController == null || mbrController == null || evtController == null){
                            setError("Member/detail/event controller null");
                            exit(0);
                        }
                    }

                    updateHomeStats();
//                    showEventsView(); /// DELETE THIS
//                    openNewWindow();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        }
    public void setError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("");
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        alert.showAndWait();
    }
    public void setSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);  // No header
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        alert.showAndWait();
    }
    public boolean getConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /*
    Function to display home pane
     */
    @FXML
    public void showHomeView() throws SQLException {
        pane_evntTickets.setVisible(false);
        pane_mbrDet.setVisible(false);
        pane_mbrMain.setVisible(false);
        pane_home.setVisible(true);
        btn_membersView.setDisable(false);
        btn_eventsView.setDisable(false);
        btn_homeView.setDisable(true);
    }
    /*
    Function to display members pane
     */
    @FXML
    public void showMembersView() throws SQLException {
        pane_evntTickets.setVisible(false);
        pane_mbrDet.setVisible(false);
        pane_home.setVisible(false);
        if (pane_mbrMain != null) {
            if (!pane_mbrMain.isVisible()) {
                pane_mbrMain.setVisible(true);
                btn_membersView.setDisable(true);
                btn_eventsView.setDisable(false);
                btn_homeView.setDisable(false);
                mbrController.showMembersView();
            }
        }
    }

    /*
    Function to display events pane
     */
    @FXML
    public void showEventsView(){
        if (!pane_evntTickets.isVisible()) {
            pane_home.setVisible(false);
            pane_evntTickets.setVisible(true);
            pane_mbrMain.setVisible(false);
            pane_mbrDet.setVisible(false);
            btn_membersView.setDisable(false);
            btn_homeView.setDisable(false);
            btn_eventsView.setDisable(true);
            evtController.showEventsView();
        }
    }

    public void viewMembership(String ucid) throws SQLException, IOException {
        mbrDetController.viewMembership(ucid);
    }

    public void setDBControl(db_control db_c) throws SQLException {
        this.db_c = db_c;
    }

    public VBox createMemberDisplay(String fullName) {
        // Get initials from the full name
        String initials = getInitials(fullName);

        // Create a Circle for the member avatar
        Circle circle = new Circle(40);  // Radius of 40
        circle.setId("mbrCircle");

        // Create a Text for the initials
        Text initialsText = new Text(initials);
        initialsText.setId("mbrInitials");
        initialsText.setFont(new Font(24));
        initialsText.setFill(Color.WHITE); // Set text color to white for contrast

        // Use a StackPane to place the initials on top of the circle
        StackPane circleContainer = new StackPane();
        circleContainer.setId("mbrIconPane");
        circleContainer.getChildren().addAll(circle, initialsText);
        circleContainer.setAlignment(Pos.CENTER);

        // Create a Text for the full name
        Text fullNameText = new Text(fullName);

        // Create a VBox to hold the circle and the name
        VBox memberBox = new VBox(circleContainer, fullNameText);
        memberBox.setAlignment(Pos.CENTER);
        memberBox.setSpacing(10); // Space between circle and name

        return memberBox;
    }

    /*
    Method to update statistics being displayed on the home page
     */
    public void updateHomeStats() throws SQLException {
        // Initialize member numbers
        int[] mbrNums = db_c.getMembersLink().getMembersNumbers();
        txt_totalMbr.setText(Integer.toString(mbrNums[0] + mbrNums[1]) + " total members");
        txt_actMbr.setText(Integer.toString(mbrNums[0]) + " active members");
        txt_inactMbr.setText(Integer.toString(mbrNums[1]) + " inactive members");

        // Initialize nationality bar chart
        XYChart.Series<String, Integer> natData = new XYChart.Series<>();
        ResultSet natData_db = db_c.getMembersLink().getMembersByNationality();

        while (natData_db.next()){
            natData.getData().add(new XYChart.Data<>(natData_db.getString("citizen"), natData_db.getInt("number")));
        }
        home_mbrNationality.getData().clear();
        home_mbrNationality.getData().add(natData);

        // Initialize year bar chart
        XYChart.Series<String, Integer> yearData = new XYChart.Series<>();
        ResultSet yearData_db = db_c.getMembersLink().getMembersByYear();

        while (yearData_db.next()){
            yearData.getData().add(new XYChart.Data<>(util.extractYear(yearData_db.getString("year")), yearData_db.getInt("number")));
        }
        home_mbrYears.getData().clear();
        home_mbrYears.getData().add(yearData);

        String[][] recentMbrs = db_c.getMembersLink().getRecentFiveMembers();
        // Show recent members added
        hbox_mbrs.setSpacing(20); // Space between members
        hbox_mbrs.setAlignment(Pos.CENTER);
        hbox_mbrs.getChildren().clear();
        // Add the top 5 members to the HBox
        for (int i = 0; i < 5; i++) {
            if (recentMbrs[i][0] == null){
                break;
            }
            VBox memberDisplay = createMemberDisplay(recentMbrs[i][1] + " " + recentMbrs[i][2]);
            hbox_mbrs.getChildren().add(memberDisplay);
            memberDisplay.setUserData(recentMbrs[i][0]);
            memberDisplay.setOnMouseClicked((MouseEvent event) -> {
                System.out.println(memberDisplay.getUserData());
                try {
                    viewMembership(memberDisplay.getUserData().toString());
                    pane_home.setVisible(false);
                } catch (Exception e) {}
            });
        }
    }

    public boolean showAddMbrWindow(String[] data) throws SQLException {
        // Member already in DB, but inactive
        if (!data[0].isBlank()){
            ResultSet[] memberDetsFromDb = db_c.getMembersLink().getMembership(data[0]);
            if (memberDetsFromDb[0].next()){
                setError("Error: Member data already exists in database,\n manually update their status");
                return false;
            }

            // Prefill fields if data provided
            txt_mbrAddUcid.setText(data[0]);
            txt_mbrAddFname.setText(data[1]);
            txt_mbrAddLname.setText(data[2]);
            txt_mbrAddEmail.setText(data[3]);
            System.out.println(data[0]);
        }
        // Create a new stage (popup window)
        Stage addMbrPopup = new Stage();
        Pane main_pane = (Pane) pane_addMbr.getParent();
        main_pane.getChildren().remove(pane_addMbr);

        // Set modality to block interaction with other windows
        addMbrPopup.initModality(Modality.APPLICATION_MODAL);
        addMbrPopup.setTitle("Add Member");

        // Set close button's action
        btn_closeAddMbr.setOnAction(e -> addMbrPopup.close());

        Scene scene = new Scene(pane_addMbr);
        addMbrPopup.setScene(scene);
        pane_addMbr.setVisible(true);

        // Show the popup
        addMbrPopup.showAndWait();

        // Reset fields
        txt_mbrAddEmail.setText("");
        txt_mbrAddFname.setText("");
        txt_mbrAddLname.setText("");
        txt_mbrAddUcid.setText("");
        pick_mbrAddCitizen.setValue(null);
        pick_mbrAddPayment.setValue(null);
        pick_mbrAddSince.setValue(null);
        pick_mbrAddTill.setValue(null);
        pick_mbrAddYear.setValue(null);

        // Add pane back to the root
        main_pane.getChildren().add(pane_addMbr);
        pane_addMbr.setVisible(false);
        return false;
    }
    /**
     * Function to add a member with the details entered
     * @return true if member was added, false otherwise
     */
    public boolean addMember(ActionEvent event) {
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
                    int success = db_c.getMembersLink().add(Integer.parseInt(ucid), fname, lname, year, citizen, since, till, payment, email);
                    if (success == 19){
                        setError("Error: Member with UCID " + ucid + " already exists.");
                        return false;
                    }
                    else if (success == 1){
                        setSuccess("Member successfully added");
                        // Close the popup window
                        Node source = (Node) event.getSource();
                        // Get the stage from the button
                        Stage stage = (Stage) source.getScene().getWindow();
                        stage.close();
//                        showMembersView();
                        updateHomeStats();
                    }
                    else {
                        setError("Error: Unable to add member");
                        return false;
                    }

                } else{
                    setError("Error: Select membership validity from and to dates");
                    return false;
                }
            }
            else{
                setError("Error: Enter a valid 8 digit UCID number");
                return false;
            }
        }
        catch (Exception e){
            setError("Error: Enter a valid 8 digit UCID number");
            return false;
        }
        return true;
    }

}
