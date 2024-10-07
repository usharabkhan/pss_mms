package code.controllers;

import code.database.db_control;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static code.App.db_c;
import static code.util.getInitials;
import static java.lang.System.exit;

public class MainController {
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
    private Button btn_membersView, btn_eventsView, btn_homeView;
    @FXML
    private AnchorPane events_main, pane_mbrMain, pane_mbrDet, pane_evntTickets, pane_home;
    
    @FXML
    private TextField txt_memberSearch;

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
                    showEventsView(); /// DELETE THIS
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
            yearData.getData().add(new XYChart.Data<>(yearData_db.getString("year"), yearData_db.getInt("number")));
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

    public void setMemberList(HashMap<String, String[]> list){
        main_mbrList = list;
    }

}
