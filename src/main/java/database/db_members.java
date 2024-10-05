package database;

import code.util;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class db_members {
    private Connection db_conn = null;
    public db_members(Connection db_conn){
        this.db_conn = db_conn;
    }

    public void importMembers(){
        String CSV_FILE_PATH = "src/main/resources/membershipform.csv";
        String line;

        List<String[]> data = new ArrayList<>(); // To store parsed data

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            // Read the header line (if necessary)
            String header = br.readLine(); // Uncomment if you want to read the header

            while ((line = br.readLine()) != null) {
                // Use split method to parse the line
                String[] values = line.split(",");
                data.add(values); // Add parsed line to data list
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int count = 0;
        for (String row[] : data) {
            if ((row[7].strip().equalsIgnoreCase("Paid"))) {
                // Get
                int ucid = Integer.parseInt(row[4]);
                String email  = "";
                String payment = "";
                // Make first and last name proper
                String fname = row[1].substring(0,1).toUpperCase() + row[1].substring(1).toLowerCase();
                String lname = row[2].substring(0,1).toUpperCase() + row[2].substring(1).toLowerCase();
                // Extract email
                email = util.extractEmail(row[3]).toLowerCase();
                if (row.length > 9) {
                    payment = row[8];
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss");

                count += add(ucid, util.proper(fname), util.proper(lname), row[5], row[6], LocalDate.parse(row[0], formatter),
                        LocalDate.now().plusYears(1), payment, email);
            }
        }

        System.out.println(count + " members added.");
    }
    public boolean getList(ObservableList<String[]> result) throws SQLException {
        String query = "SELECT * FROM members";
        ResultSet list = null;

        try (Statement stmt = db_conn.createStatement()){
            list = stmt.executeQuery(query);
            int columnCount = list.getMetaData().getColumnCount();
            while (list.next()){
                String[] row = new String[columnCount]; // Create an array to hold the row data
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = list.getString(i); // Store each column value in the array
                }
                result.add(row);
            }
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return true;
    }

    /**
     * Add a member to the database
     * @param ucid ucid of the member to be added
     * @param fName first name
     * @param lName last name
     * @param year year of study (1 - 5 (graduate))
     * @param since date first joined as member
     * @param until date membership is valid until
     * @return return 1 if successfully added, 0 otherwise
     */
    public int add(int ucid, String fName, String lName, String year, String citizen,
                   LocalDate since, LocalDate until, String payment, String email){
        String member_query = "INSERT OR REPLACE INTO members (ucid, fName, lName, year, citizen, status) VALUES (?, ?, ?, ?, ?, ?) ";
        String membership_query = "INSERT OR REPLACE INTO memberships (ucid, since, until, payment, email) VALUES (?, ?, ?, ?, ?) ";
        try (PreparedStatement stmt1 = db_conn.prepareStatement(member_query)) {

            // Set parameters for the prepared statement
            stmt1.setInt(1, ucid);
            stmt1.setString(2, fName);
            stmt1.setString(3, lName);
            stmt1.setString(4, year);
            stmt1.setString(5, citizen);
            // Validity status set to inactive by default
            stmt1.setString(6, "inactive");

            PreparedStatement stmt2 = null;
            // Change status to active if membership is valid in future
            if (until.isAfter(LocalDate.now())){
                stmt1.setString(6, "active");
                // Add row to the memberships table
                stmt2 = db_conn.prepareStatement(membership_query);
                stmt2.setInt(1, ucid);
                stmt2.setDate(2, Date.valueOf(since));
                stmt2.setDate(3, Date.valueOf(until));
                stmt2.setString(4, payment);
                stmt2.setString(5, email);
            }
            // Execute statement
            stmt1.executeUpdate();
            // Execute statement if valid membership
            if (stmt2 != null){
                stmt2.executeUpdate();
            }

            return 1; // Return success

        } catch (SQLException e) {
            if (e.getErrorCode() == 19) { // check specific error code for duplicate keys
                System.out.println("Member with UCID " + ucid + " already exists.");
            } else {
                e.printStackTrace(); // handle other exceptions
            }
            return 0; // Return an error
        }
    }

    /**
     * Remove a member from the database
     * @param ucid the ucid of the member to be removed
     * @return return 1 if success, 0 otherwise
     */
    public int remove(int ucid){
        String member_query = "DELETE FROM members WHERE ucid = ?";
        String membership_query = "DELETE FROM membership WHERE ucid = ?";
        try {
            PreparedStatement pstmt = db_conn.prepareStatement(member_query);
            pstmt.setInt(1, ucid);
            int isDeleted = pstmt.executeUpdate();
            // If a member with the UCID existed
            if (isDeleted > 0){
                // Delete their membership
                pstmt = db_conn.prepareStatement(membership_query);
                pstmt.setInt(1, ucid);
                pstmt.executeUpdate();
                // Return success
                return 1;
            }
            // Else return failure
            else{
                return 0;
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return 0;
    }

    // Perhaps when you view a membership,
    // you should be able to edit the member details there
    public static int update(){
        return 0;
    }

    /**
     * Perform a search such that either first or last name is partially matched
     * @param name the string to search for
     * @return the set of members found, null otherwise
     */

    public ResultSet getFromName(String name){
        String query = "SELECT * FROM members WHERE fName LIKE ? OR lName LIKE ?";
        ResultSet members = null;
        try{
            PreparedStatement pstmt = db_conn.prepareStatement(query);
            pstmt.setString(1, "%" + name + "%");
            pstmt.setString(2, "%" + name + "%");
            members = pstmt.executeQuery();
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return members;
    }

    /**
     * Perform a search by member UCID
     * @param ucid the ucid to search for
     * @return the member found, null otherwse
     */

    public boolean getFromUCID(int ucid, ObservableList<String[]> matches){
        String query = "SELECT * FROM members WHERE ucid LIKE ?";
        ResultSet list = null;
        try {

            PreparedStatement pstmt = db_conn.prepareStatement(query);
            pstmt.setString(1, "%" + ucid + "%");
            list = pstmt.executeQuery();

            int columnCount = list.getMetaData().getColumnCount();
            while (list.next()) {
                String[] row = new String[columnCount]; // Create an array to hold the row data
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = list.getString(i); // Store each column value in the array
                }
                matches.add(row);
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return true;
    }

    public ResultSet[] getMembership(String ucid){
        ResultSet[] result = new ResultSet[2];
        String query1 = "SELECT * FROM memberships WHERE ucid = ?";
        String query2 = "SELECT * FROM members WHERE ucid = ?";
        try{
            PreparedStatement pstmt = db_conn.prepareStatement(query1);
            pstmt.setString(1, ucid);
            result[0] = pstmt.executeQuery();
            pstmt = db_conn.prepareStatement(query2);
            pstmt.setString(1, ucid);
            result[1] = pstmt.executeQuery();
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return result;
    }
}
